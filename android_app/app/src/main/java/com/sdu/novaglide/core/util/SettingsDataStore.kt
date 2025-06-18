package com.sdu.novaglide.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// 单例DataStore扩展
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings") 