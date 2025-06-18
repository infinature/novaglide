package com.sdu.novaglide.ui.features.profile

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.core.util.settingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * 设置ViewModel
 */
class SettingsViewModel(
    private val context: Context
) : ViewModel() {
    
    // 设置键
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        private val AUTO_SYNC_KEY = booleanPreferencesKey("auto_sync")
        private val FONT_SIZE_KEY = floatPreferencesKey("font_size")
        private val WIFI_ONLY_KEY = booleanPreferencesKey("wifi_only")
        private val ANALYTICS_KEY = booleanPreferencesKey("analytics_enabled")
        private val PERSONALIZATION_KEY = booleanPreferencesKey("personalization_enabled")
        private val NOTIFICATION_START_HOUR_KEY = intPreferencesKey("notification_start_hour")
        private val NOTIFICATION_END_HOUR_KEY = intPreferencesKey("notification_end_hour")
    }
    
    // 设置状态
    val isDarkMode: StateFlow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[DARK_MODE_KEY] ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    val notificationsEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[NOTIFICATIONS_KEY] ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val autoSync: StateFlow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[AUTO_SYNC_KEY] ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val fontSize: StateFlow<Float> = context.settingsDataStore.data
        .map { preferences -> preferences[FONT_SIZE_KEY] ?: 1f }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1f
        )
    
    val wifiOnly: StateFlow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[WIFI_ONLY_KEY] ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val analyticsEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[ANALYTICS_KEY] ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val personalizationEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[PERSONALIZATION_KEY] ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val notificationStartHour: StateFlow<Int> = context.settingsDataStore.data
        .map { preferences -> preferences[NOTIFICATION_START_HOUR_KEY] ?: 9 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 9
        )
    
    val notificationEndHour: StateFlow<Int> = context.settingsDataStore.data
        .map { preferences -> preferences[NOTIFICATION_END_HOUR_KEY] ?: 21 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 21
        )
    
    // 缓存大小状态
    private val _cacheSize = MutableStateFlow("计算中...")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()
    
    // 清理结果状态
    private val _clearCacheResult = MutableStateFlow<String?>(null)
    val clearCacheResult: StateFlow<String?> = _clearCacheResult.asStateFlow()
    
    init {
        calculateCacheSize()
    }
    
    /**
     * 设置深色模式
     */
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[DARK_MODE_KEY] = enabled
            }
        }
    }
    
    /**
     * 设置通知开关
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[NOTIFICATIONS_KEY] = enabled
            }
        }
    }
    
    /**
     * 设置自动同步
     */
    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[AUTO_SYNC_KEY] = enabled
            }
        }
    }
    
    /**
     * 设置字体大小
     */
    fun setFontSize(size: Float) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[FONT_SIZE_KEY] = size.coerceIn(0.8f, 1.2f)
            }
        }
    }
    
    /**
     * 设置仅WiFi下载
     */
    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[WIFI_ONLY_KEY] = enabled
            }
        }
    }
    
    /**
     * 设置数据分析
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[ANALYTICS_KEY] = enabled
            }
        }
    }
    
    /**
     * 设置个性化推荐
     */
    fun setPersonalizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[PERSONALIZATION_KEY] = enabled
            }
        }
    }
    
    /**
     * 设置通知时间
     */
    fun setNotificationTime(startHour: Int, endHour: Int) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[NOTIFICATION_START_HOUR_KEY] = startHour.coerceIn(0, 23)
                preferences[NOTIFICATION_END_HOUR_KEY] = endHour.coerceIn(0, 23)
            }
        }
    }
    
    /**
     * 计算缓存大小
     */
    fun calculateCacheSize() {
        viewModelScope.launch {
            try {
                val cacheDir = context.cacheDir
                val externalCacheDir = context.externalCacheDir
                
                val internalSize = calculateDirectorySize(cacheDir)
                val externalSize = externalCacheDir?.let { calculateDirectorySize(it) } ?: 0L
                
                val totalSize = internalSize + externalSize
                _cacheSize.value = formatFileSize(totalSize)
            } catch (e: Exception) {
                _cacheSize.value = "计算失败"
            }
        }
    }
    
    /**
     * 清理缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                val cacheDir = context.cacheDir
                val externalCacheDir = context.externalCacheDir
                
                var deletedSize = 0L
                
                // 清理内部缓存
                cacheDir.listFiles()?.forEach { file ->
                    deletedSize += calculateDirectorySize(file)
                    deleteRecursively(file)
                }
                
                // 清理外部缓存
                externalCacheDir?.listFiles()?.forEach { file ->
                    deletedSize += calculateDirectorySize(file)
                    deleteRecursively(file)
                }
                
                _clearCacheResult.value = "已清理 ${formatFileSize(deletedSize)}"
                calculateCacheSize() // 重新计算缓存大小
                
                // 3秒后清除结果消息
                kotlinx.coroutines.delay(3000)
                _clearCacheResult.value = null
                
            } catch (e: Exception) {
                _clearCacheResult.value = "清理失败: ${e.message}"
                kotlinx.coroutines.delay(3000)
                _clearCacheResult.value = null
            }
        }
    }
    
    /**
     * 恢复默认设置
     */
    fun restoreDefaultSettings() {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }
    
    /**
     * 计算目录大小
     */
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        try {
            if (directory.isFile) {
                size = directory.length()
            } else if (directory.isDirectory) {
                directory.listFiles()?.forEach { file ->
                    size += calculateDirectorySize(file)
                }
            }
        } catch (e: Exception) {
            // 忽略权限错误
        }
        return size
    }
    
    /**
     * 递归删除文件或目录
     */
    private fun deleteRecursively(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    deleteRecursively(child)
                }
            }
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "${sizeInBytes}B"
            sizeInBytes < 1024 * 1024 -> "${sizeInBytes / 1024}KB"
            sizeInBytes < 1024 * 1024 * 1024 -> "%.1fMB".format(sizeInBytes / (1024.0 * 1024.0))
            else -> "%.2fGB".format(sizeInBytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
    
    /**
     * ViewModel工厂
     */
    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 