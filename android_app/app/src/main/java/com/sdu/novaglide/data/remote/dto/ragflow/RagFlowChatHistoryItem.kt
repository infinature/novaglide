package com.sdu.novaglide.data.remote.dto.ragflow

/**
 * RAGFlow聊天历史项目数据类
 * 用于表示共享聊天中的一条消息
 */
data class RagFlowChatHistoryItem(
    val role: String, // "user" 或 "assistant"
    val content: String, // 消息内容
    val documents: List<RagFlowDocument>? = null, // 引用的文档，仅assistant消息可能有
    val timestamp: Long? = null // 消息时间戳
)
