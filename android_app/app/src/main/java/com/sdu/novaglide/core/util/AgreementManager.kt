package com.sdu.novaglide.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.agreementDataStore: DataStore<Preferences> by preferencesDataStore(name = "agreement_prefs")

class AgreementManager(private val context: Context) {
    companion object {
        private val USER_AGREEMENT_ACCEPTED = booleanPreferencesKey("user_agreement_accepted")
        private val PRIVACY_POLICY_ACCEPTED = booleanPreferencesKey("privacy_policy_accepted")
        private val FIRST_TIME_LAUNCH = booleanPreferencesKey("first_time_launch")
    }

    /**
     * 检查是否为首次启动应用
     */
    suspend fun isFirstTimeLaunch(): Boolean {
        return context.agreementDataStore.data
            .map { preferences -> 
                preferences[FIRST_TIME_LAUNCH] ?: true 
            }
            .first()
    }

    /**
     * 检查用户是否已同意所有协议
     */
    suspend fun hasAcceptedAllAgreements(): Boolean {
        return context.agreementDataStore.data
            .map { preferences -> 
                val userAgreementAccepted = preferences[USER_AGREEMENT_ACCEPTED] ?: false
                val privacyPolicyAccepted = preferences[PRIVACY_POLICY_ACCEPTED] ?: false
                userAgreementAccepted && privacyPolicyAccepted
            }
            .first()
    }

    /**
     * 用户同意所有协议
     */
    suspend fun acceptAllAgreements() {
        context.agreementDataStore.edit { preferences ->
            preferences[USER_AGREEMENT_ACCEPTED] = true
            preferences[PRIVACY_POLICY_ACCEPTED] = true
            preferences[FIRST_TIME_LAUNCH] = false
        }
    }

    /**
     * 重置协议状态（用于测试）
     */
    suspend fun resetAgreements() {
        context.agreementDataStore.edit { preferences ->
            preferences[USER_AGREEMENT_ACCEPTED] = false
            preferences[PRIVACY_POLICY_ACCEPTED] = false
            preferences[FIRST_TIME_LAUNCH] = true
        }
    }

    /**
     * 检查是否需要显示协议同意页面
     * 当首次启动且未同意所有协议时需要显示
     */
    suspend fun shouldShowAgreementScreen(): Boolean {
        return isFirstTimeLaunch() || !hasAcceptedAllAgreements()
    }
} 