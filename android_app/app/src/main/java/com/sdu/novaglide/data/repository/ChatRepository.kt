package com.sdu.novaglide.data.repository

import com.sdu.novaglide.domain.model.ChatMessage
import com.sdu.novaglide.domain.model.DocumentReference
import kotlinx.coroutines.flow.Flow

/**
 * 聊天仓库接口
 * 定义与聊天相关的数据操作方法
 */
interface ChatRepository {
    /**
     * 发送聊天消息到DeepSeek API
     * @param messages 聊天消息历史
     * @param userMessage 用户输入的消息
     * @param useStream 是否使用流式输出
     * @return 流式响应（如果启用流式输出）或完整响应
     */
    suspend fun sendMessageToDeepSeek(
        messages: List<ChatMessage>,
        userMessage: String,
        useStream: Boolean = false
    ): Flow<String>
    
    /**
     * 发送查询到RAGFlow
     * @param query 用户查询文本
     * @param conversationId 对话ID（用于继续对话，可选）
     * @param assistantId 助手ID
     * @param useStream 是否使用流式输出
     * @return 包含回答和文档引用的结果流
     */
    suspend fun sendQueryToRagFlow(
        query: String,
        conversationId: String? = null,
        assistantId: String,
        useStream: Boolean = false
    ): Flow<Pair<String, List<DocumentReference>?>>
    
    /**
     * 获取RAGFlow的所有助手
     * @return 助手ID和名称的映射
     */
    suspend fun getRagFlowAssistants(): Map<String, String>
    
    /**
     * 获取RAGFlow的知识库列表
     * @return 知识库ID和名称的映射
     */
    suspend fun getRagFlowKnowledgeBases(): Map<String, String>
    
    /**
     * 检查API密钥和设置是否已配置
     * @return 是否已配置所有必要的设置
     */
    suspend fun isApiConfigured(): Boolean
    
    /**
     * 获取共享聊天历史
     * @param sharedId 共享ID
     * @param authToken 认证令牌
     * @return 聊天历史数据流
     */
    suspend fun getSharedChat(
        sharedId: String,
        authToken: String
    ): Flow<List<com.sdu.novaglide.data.remote.dto.ragflow.RagFlowChatHistoryItem>>
} 