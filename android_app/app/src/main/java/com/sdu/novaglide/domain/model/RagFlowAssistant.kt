package com.sdu.novaglide.domain.model

/**
 * 表示RAGFlow助手的领域模型
 *
 * @property id 助手的唯一ID
 * @property name 助手名称
 * @property description 助手描述
 * @property createTime 创建时间
 */
data class RagFlowAssistant(
    val id: String,
    val name: String,
    val description: String = "",
    val createTime: String = ""
)
