package com.sdu.novaglide.ui.features.qna

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.core.constants.ApiConstants
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.data.repository.ChatRepository
import com.sdu.novaglide.domain.model.ChatMessage
import com.sdu.novaglide.domain.model.DocumentReference
import com.sdu.novaglide.domain.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 问答界面视图模型
 */
class QnaViewModel(
    private val chatRepository: ChatRepository,
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {

    // 消息历史状态流
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // 当前对话ID（RagFlow）
    private var currentConversationId: String? = null

    // 正在加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // API配置状态
    private val _isApiConfigured = MutableStateFlow(false)
    val isApiConfigured: StateFlow<Boolean> = _isApiConfigured.asStateFlow()

    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // 检查API配置状态
        viewModelScope.launch {
            _isApiConfigured.value = chatRepository.isApiConfigured()
        }
    }

    /**
     * 发送消息到DeepSeek
     * @param userInput 用户输入
     */
    fun sendMessageToDeepSeek(userInput: String) {
        if (userInput.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            
            // 添加用户消息
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = userInput,
                role = MessageRole.USER
            )
            _messages.value = _messages.value + userMessage

            // 创建加载中的助手消息
            val loadingMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "",
                role = MessageRole.ASSISTANT,
                isLoading = true
            )
            _messages.value = _messages.value + loadingMessage

            try {
                // 发送请求
                var responseContent = ""
                chatRepository.sendMessageToDeepSeek(
                    messages = _messages.value.filterNot { it.isLoading },
                    userMessage = userInput,
                    useStream = true
                ).collect { content ->
                    responseContent = content
                    
                    // 更新消息列表，替换加载中的消息
                    _messages.value = _messages.value.map { message ->
                        if (message.id == loadingMessage.id) {
                            message.copy(content = content, isLoading = false)
                        } else {
                            message
                        }
                    }
                }
                
                // 确保最终消息已更新
                _messages.value = _messages.value.map { message ->
                    if (message.id == loadingMessage.id) {
                        message.copy(content = responseContent, isLoading = false)
                    } else {
                        message
                    }
                }
            } catch (e: Exception) {
                // 显示错误消息
                _errorMessage.value = e.message ?: "发送消息失败"
                
                // 更新加载消息为错误消息
                _messages.value = _messages.value.map { message ->
                    if (message.id == loadingMessage.id) {
                        message.copy(content = "发送消息时出错: ${e.message}", isLoading = false)
                    } else {
                        message
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 发送查询到RagFlow
     * @param query 用户查询
     */
    fun sendQueryToRagFlow(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            
            // 添加用户消息
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = query,
                role = MessageRole.USER
            )
            _messages.value = _messages.value + userMessage

            // 创建加载中的助手消息
            val loadingMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "",
                role = MessageRole.ASSISTANT,
                isLoading = true
            )
            _messages.value = _messages.value + loadingMessage

            try {
                // 尝试获取助手ID，如果没有配置，使用默认助手
                val ragFlowApiKey = apiKeyStore.ragFlowApiKey.first()
                
                if (ragFlowApiKey.isNullOrEmpty()) {
                    throw IllegalStateException("RagFlow API密钥未配置")
                }
                
                // 尝试获取助手列表并使用第一个助手
                val assistants = chatRepository.getRagFlowAssistants()
                val assistantId = assistants.keys.firstOrNull() ?: throw IllegalStateException("未找到RagFlow助手")
                
                var finalContent = ""
                var finalReferences: List<DocumentReference>? = null
                
                // 发送请求
                chatRepository.sendQueryToRagFlow(
                    query = query,
                    conversationId = currentConversationId,
                    assistantId = assistantId,
                    useStream = true
                ).collect { (content, references) ->
                    finalContent = content
                    finalReferences = references
                    
                    // 更新消息列表，替换加载中的消息
                    _messages.value = _messages.value.map { message ->
                        if (message.id == loadingMessage.id) {
                            message.copy(
                                content = content,
                                isLoading = false,
                                references = references ?: emptyList()
                            )
                        } else {
                            message
                        }
                    }
                }
                
                // 确保最终消息已更新
                _messages.value = _messages.value.map { message ->
                    if (message.id == loadingMessage.id) {
                        message.copy(
                            content = finalContent,
                            isLoading = false,
                            references = finalReferences ?: emptyList()
                        )
                    } else {
                        message
                    }
                }
            } catch (e: Exception) {
                // 显示错误消息
                _errorMessage.value = e.message ?: "发送查询失败"
                
                // 更新加载消息为错误消息
                _messages.value = _messages.value.map { message ->
                    if (message.id == loadingMessage.id) {
                        message.copy(content = "发送查询时出错: ${e.message}", isLoading = false)
                    } else {
                        message
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
} 