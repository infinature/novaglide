package com.sdu.novaglide.domain.model

import java.util.Date

/**
 * 聊天消息领域模型
 */
data class ChatMessage(
    val id: String,                  // 消息唯一ID
    val content: String,             // 消息内容
    val role: MessageRole,           // 消息角色（用户或助手）
    val timestamp: Date = Date(),    // 消息时间戳
    val references: List<DocumentReference> = emptyList(), // 引用的文档(RAGFlow特有)
    val isLoading: Boolean = false   // 是否正在加载中
)

/**
 * 消息角色枚举
 */
enum class MessageRole {
    USER,       // 用户
    ASSISTANT   // 助手
} 