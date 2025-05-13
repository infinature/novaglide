package com.sdu.novaglide.ui.features.qna

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.core.constants.ApiConstants
import com.sdu.novaglide.core.util.ApiKeyStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "ApiSettingsViewModel"

/**
 * API设置视图模型
 */
class ApiSettingsViewModel(
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {

    // DeepSeek API密钥
    private val _deepSeekApiKey = MutableStateFlow<String>("")
    val deepSeekApiKey: StateFlow<String> = _deepSeekApiKey.asStateFlow()

    // RagFlow API密钥
    private val _ragFlowApiKey = MutableStateFlow<String>("")
    val ragFlowApiKey: StateFlow<String> = _ragFlowApiKey.asStateFlow()

    // RagFlow服务器URL
    private val _ragFlowServerUrl = MutableStateFlow<String>("")
    val ragFlowServerUrl: StateFlow<String> = _ragFlowServerUrl.asStateFlow()

    // 是否正在保存
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // 保存是否成功
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // 设置信息或警告消息
    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    init {
        // 加载现有设置
        loadSettings()
    }

    /**
     * 加载现有设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始加载API设置")
                
                // 获取DeepSeek API密钥
                apiKeyStore.deepSeekApiKey.first()?.let { apiKey ->
                    Log.d(TAG, "已加载DeepSeek API密钥，长度: ${apiKey.length}")
                    _deepSeekApiKey.value = apiKey
                }
                
                // 获取RagFlow API密钥
                apiKeyStore.ragFlowApiKey.first()?.let { apiKey ->
                    Log.d(TAG, "已加载RagFlow API密钥，长度: ${apiKey.length}")
                    _ragFlowApiKey.value = apiKey
                }
                
                // 获取RagFlow服务器URL
                apiKeyStore.ragFlowServerUrl.first()?.let { url ->
                    Log.d(TAG, "已加载RagFlow服务器URL: $url")
                    _ragFlowServerUrl.value = url
                }
                
                Log.d(TAG, "API设置加载完成")
            } catch (e: Exception) {
                Log.e(TAG, "加载API设置时出错", e)
                _infoMessage.value = "加载设置时出错: ${e.message}"
            }
        }
    }

    /**
     * 更新DeepSeek API密钥
     */
    fun updateDeepSeekApiKey(apiKey: String) {
        _deepSeekApiKey.value = apiKey
    }

    /**
     * 更新RagFlow API密钥
     */
    fun updateRagFlowApiKey(apiKey: String) {
        _ragFlowApiKey.value = apiKey
    }

    /**
     * 更新RagFlow服务器URL
     */
    fun updateRagFlowServerUrl(url: String) {
        _ragFlowServerUrl.value = url
    }

    /**
     * 清除信息消息
     */
    fun clearInfoMessage() {
        _infoMessage.value = null
    }
    
    /**
     * 验证DeepSeek API密钥格式
     */
    private fun isValidDeepSeekApiKey(apiKey: String): Boolean {
        // DeepSeek API密钥通常很长，至少应该有10个字符
        if (apiKey.isBlank() || apiKey.length < 10) {
            return false
        }
        
        // 避免使用任何"Bearer "前缀
        if (apiKey.startsWith("Bearer ", ignoreCase = true)) {
            return false
        }
        
        return true
    }

    /**
     * 保存设置
     */
    fun saveSettings() {
        viewModelScope.launch {
            _isSaving.value = true
            
            try {
                Log.d(TAG, "开始保存API设置")
                
                // 验证DeepSeek API密钥
                if (_deepSeekApiKey.value.isNotBlank() && !isValidDeepSeekApiKey(_deepSeekApiKey.value)) {
                    _infoMessage.value = "DeepSeek API密钥格式无效，请确保输入正确的API密钥"
                    _isSaving.value = false
                    return@launch
                }
                
                // 保存DeepSeek API密钥
                Log.d(TAG, "保存DeepSeek API密钥，长度: ${_deepSeekApiKey.value.length}")
                apiKeyStore.saveDeepSeekApiKey(_deepSeekApiKey.value)
                
                // 保存RagFlow API密钥
                Log.d(TAG, "保存RagFlow API密钥，长度: ${_ragFlowApiKey.value.length}")
                apiKeyStore.saveRagFlowApiKey(_ragFlowApiKey.value)
                
                // 保存RagFlow服务器URL
                val serverUrl = _ragFlowServerUrl.value.trim()
                Log.d(TAG, "保存RagFlow服务器URL: $serverUrl")
                apiKeyStore.saveRagFlowServerUrl(serverUrl)
                
                // 显示保存成功
                _saveSuccess.value = true
                _infoMessage.value = "设置已保存。API密钥已安全加密存储。"
                Log.d(TAG, "API设置保存成功")
                
                // 3秒后隐藏成功消息
                kotlinx.coroutines.delay(3000)
                _saveSuccess.value = false
            } catch (e: Exception) {
                Log.e(TAG, "保存API设置时出错", e)
                _infoMessage.value = "保存设置时出错: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
} 