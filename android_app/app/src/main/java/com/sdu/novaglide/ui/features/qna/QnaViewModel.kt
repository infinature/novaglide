package com.sdu.novaglide.ui.features.qna

import android.util.Log
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart

/**
 * 问答界面视图模型
 */
class QnaViewModel(
    private val chatRepository: ChatRepository,
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {

    private val TAG = "QnaViewModel"

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
    private val _errorMessage = MutableStateFlow<String>("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

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
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            
            // 打印配置信息以便调试
            try {
                val apiKey = apiKeyStore.ragFlowApiKey.first()
                val serverUrl = apiKeyStore.ragFlowServerUrl.first()
                Log.d(TAG, "RAGFlow配置 - API密钥: ${apiKey?.take(5)}*** (长度: ${apiKey?.length ?: 0}), 服务器URL: $serverUrl")
            } catch (e: Exception) {
                Log.e(TAG, "读取RAGFlow配置出错", e)
            }
            
            Log.d(TAG, "准备发送RagFlow查询: $query")

            // 创建用户消息
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
                
                // 尝试获取助手列表
                Log.d(TAG, "正在获取RagFlow助手列表...")
                val assistants = chatRepository.getRagFlowAssistants()
                
                if (assistants.isEmpty()) {
                    Log.e(TAG, "助手列表为空！")
                    _errorMessage.value = "无法连接到RAGFlow服务器或未找到助手。请检查网络连接和服务器配置。"
                    _messages.value = _messages.value.map { message ->
                        if (message.id == loadingMessage.id) {
                            message.copy(content = "无法连接到RAGFlow服务器或未找到助手。请检查网络连接和服务器配置。", isLoading = false)
                        } else {
                            message
                        }
                    }
                    return@launch
                }
                
                Log.d(TAG, "获取到${assistants.size}个RagFlow助手: ${assistants.values.joinToString(", ")}")
                
                // 查找目标助手
                val targetNames = listOf(
                    "HPC编写小助手", 
                    "HPC编程小助手", 
                    "HPC小助手", 
                    "HPC助手",
                    "HPC",
                    "编写小助手"
                )
                
                val assistantId = assistants.entries.firstOrNull { entry ->
                    targetNames.any { name -> entry.value.contains(name, ignoreCase = true) }
                }?.key ?: assistants.keys.firstOrNull()
                
                if (assistantId == null) {
                    Log.e(TAG, "未能从助手列表中选择助手")
                    _errorMessage.value = "无法选择RAGFlow助手。请检查服务器配置。"
                    _messages.value = _messages.value.map { message ->
                        if (message.id == loadingMessage.id) {
                            message.copy(content = "无法选择RAGFlow助手。请检查服务器配置。", isLoading = false)
                        } else {
                            message
                        }
                    }
                    return@launch
                }
                
                val selectedAssistantName = assistants[assistantId] ?: "(未知助手)"
                Log.d(TAG, "选择的助手: $selectedAssistantName (ID: $assistantId)")
                
                var finalContent = ""
                var finalReferences: List<DocumentReference>? = null
                
                // 保存会话关联
                currentConversationId?.let {
                    Log.d(TAG, "使用现有会话ID: $it")
                }
                
                // 发送请求
                Log.d(TAG, "发送查询到助手: $selectedAssistantName, 查询: $query")
                chatRepository.sendQueryToRagFlow(
                    query = query,
                    conversationId = currentConversationId,
                    assistantId = assistantId,
                    useStream = true
                ).collect { (content, references) ->
                    Log.d(TAG, "收到流式响应片段，长度: ${content.length}, 引用文档数: ${references?.size ?: 0}")
                    finalContent = content
                    
                    // 如果RAGFlow没有返回引用，尝试从内容中提取
                    finalReferences = if (references.isNullOrEmpty()) {
                        Log.d(TAG, "RAGFlow未返回引用文档，尝试从内容中提取引文")
                        val extractedRefs = extractReferencesFromContent(content)
                        if (extractedRefs.isNotEmpty()) {
                            Log.d(TAG, "成功从内容中提取到${extractedRefs.size}个引用")
                            extractedRefs
                        } else {
                            Log.d(TAG, "内容中未找到引文格式")
                            emptyList()
                        }
                    } else {
                        Log.d(TAG, "使用RAGFlow返回的${references.size}个引用文档")
                        references
                    }
                    
                    // 更新消息列表，替换加载中的消息
                    Log.d(TAG, "收到流式响应更新，内容长度：${content.length}，最终引用数量：${finalReferences?.size ?: 0}")
                    _messages.value = _messages.value.map { message ->
                        if (message.id == loadingMessage.id) {
                            val updatedMessage = message.copy(
                                content = content,
                                isLoading = false,
                                references = finalReferences ?: emptyList()
                            )
                            Log.d(TAG, "更新后的消息内容：${updatedMessage.content.take(50)}..., 引用数量：${updatedMessage.references.size}")
                            updatedMessage
                        } else {
                            message
                        }
                    }
                    
                    // 确保StateFlow触发更新
                    _messages.value = ArrayList(_messages.value)
                }
                
                // 确保最终消息已更新
                Log.d(TAG, "完成流式响应接收，最终内容长度：${finalContent.length}，最终引用数量：${finalReferences?.size ?: 0}")
                _messages.value = _messages.value.map { message ->
                    if (message.id == loadingMessage.id) {
                        val finalMessage = message.copy(
                            content = finalContent,
                            isLoading = false,
                            references = finalReferences ?: emptyList()
                        )
                        Log.d(TAG, "最终消息内容更新：${finalMessage.content.take(50)}..., 最终引用数量：${finalMessage.references.size}")
                        finalMessage
                    } else {
                        message
                    }
                }
                
                // 确保StateFlow触发更新
                _messages.value = ArrayList(_messages.value)
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
        _errorMessage.value = ""
    }
    
    /**
     * 加载共享聊天
     * @param sharedId 共享ID
     * @param authToken 认证令牌
     */
    fun loadSharedChat(sharedId: String, authToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _messages.value = emptyList() // 清空现有消息
            
            try {
                Log.d(TAG, "开始加载共享聊天: $sharedId")
                
                // 构建请求体
                val request = mapOf<String, Any>(
                    "shared_id" to sharedId,
                    "auth" to authToken
                )
                
                // 使用RagFlow API获取共享聊天历史
                chatRepository.getSharedChat(sharedId, authToken)
                    .onStart { 
                        Log.d(TAG, "开始请求共享聊天")
                    }
                    .catch { e ->
                        Log.e(TAG, "获取共享聊天失败", e)
                        _errorMessage.value = "获取共享聊天失败: ${e.message}"
                    }
                    .collect { chatHistory ->
                        // 解析聊天历史并转换为消息列表
                        val convertedMessages = chatHistory.map { historyItem ->
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                content = historyItem.content,
                                role = if (historyItem.role == "user") MessageRole.USER else MessageRole.ASSISTANT,
                                references = historyItem.documents?.map { doc ->
                                    DocumentReference(
                                        documentId = doc.id,
                                        documentName = doc.fileName,
                                        documentType = doc.fileType,
                                        content = doc.content,
                                        pageNumber = doc.pageNum,
                                        confidence = doc.score?.toFloat(),
                                        source = doc.filePath ?: doc.fileName
                                    )
                                } ?: emptyList()
                            )
                        }
                        
                        _messages.value = convertedMessages
                        Log.d(TAG, "共享聊天加载完成，共${convertedMessages.size}条消息")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "加载共享聊天异常", e)
                _errorMessage.value = "加载共享聊天失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 从AI回答内容中提取引文并生成模拟的DocumentReference
     * 用于处理RAGFlow不返回quoted_documents的情况
     */
    private fun extractReferencesFromContent(content: String): List<DocumentReference> {
        val references = mutableListOf<DocumentReference>()
        
        // 匹配各种引文格式
        val patterns = listOf(
            Regex("""##(\d+)\$\$"""),  // ##数字$$
            Regex("""\[\[(\d+)\]\]"""), // [[数字]]
            Regex("""#(\d+)#"""),       // #数字#
            Regex("""\$\$(\d+)\$\$""")  // $$数字$$
        )
        
        val foundNumbers = mutableSetOf<Int>()
        
        patterns.forEach { pattern ->
            pattern.findAll(content).forEach { matchResult ->
                val numberStr = matchResult.groupValues[1]
                val number = numberStr.toIntOrNull()
                if (number != null && !foundNumbers.contains(number)) {
                    foundNumbers.add(number)
                    
                    val reference = DocumentReference(
                        documentId = "ref_$number", // 使用引文编号作为文档ID
                        documentName = "引用文档 $number",
                        documentType = "pdf",
                        content = "这是第${number}号引用文档的内容摘要",
                        pageNumber = null,
                        confidence = 0.8f,
                        source = "RAGFlow知识库"
                    )
                    references.add(reference)
                    Log.d(TAG, "生成模拟引用: 编号[$number] -> 文档ID[${reference.documentId}]")
                }
            }
        }
        
        // 按编号排序
        references.sortBy { it.documentId.removePrefix("ref_").toIntOrNull() ?: 0 }
        
        Log.d(TAG, "从内容中提取到${references.size}个引用: ${foundNumbers.sorted()}")
        return references
    }
} 