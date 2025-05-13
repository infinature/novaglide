package com.sdu.novaglide.core.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sdu.novaglide.core.constants.ApiConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val TAG = "ApiKeyStore"

/**
 * API密钥存储工具类
 * 使用DataStore安全地存储API密钥和服务器URL
 */
class ApiKeyStore(val context: Context) {

    companion object {
        internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "api_settings")
        
        private val DEEPSEEK_API_KEY = stringPreferencesKey(ApiConstants.PREF_DEEPSEEK_API_KEY)
        private val RAGFLOW_API_KEY = stringPreferencesKey(ApiConstants.PREF_RAGFLOW_API_KEY)
        private val RAGFLOW_SERVER_URL = stringPreferencesKey(ApiConstants.PREF_RAGFLOW_SERVER_URL)
    }
    
    init {
        Log.d(TAG, "初始化ApiKeyStore，Context: ${context.javaClass.simpleName}")
    }
    
    // 获取DeepSeek API密钥
    val deepSeekApiKey: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "读取DeepSeek API密钥时发生错误", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DEEPSEEK_API_KEY]
        }
    
    // 获取RAGFlow API密钥
    val ragFlowApiKey: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "读取RAGFlow API密钥时发生错误", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[RAGFLOW_API_KEY]
        }
    
    // 获取RAGFlow服务器URL
    val ragFlowServerUrl: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "读取RAGFlow服务器URL时发生错误", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[RAGFLOW_SERVER_URL]
        }
    
    // 保存DeepSeek API密钥
    suspend fun saveDeepSeekApiKey(apiKey: String) {
        try {
            Log.d(TAG, "保存DeepSeek API密钥")
            context.dataStore.edit { preferences ->
                preferences[DEEPSEEK_API_KEY] = apiKey
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存DeepSeek API密钥失败", e)
            throw e
        }
    }
    
    // 保存RAGFlow API密钥
    suspend fun saveRagFlowApiKey(apiKey: String) {
        try {
            Log.d(TAG, "保存RAGFlow API密钥")
            context.dataStore.edit { preferences ->
                preferences[RAGFLOW_API_KEY] = apiKey
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存RAGFlow API密钥失败", e)
            throw e
        }
    }
    
    // 保存RAGFlow服务器URL
    suspend fun saveRagFlowServerUrl(url: String) {
        try {
            Log.d(TAG, "保存RAGFlow服务器URL: $url")
            context.dataStore.edit { preferences ->
                preferences[RAGFLOW_SERVER_URL] = url
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存RAGFlow服务器URL失败", e)
            throw e
        }
    }
} 