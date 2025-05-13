package com.sdu.novaglide.domain.model

/**
 * 文档引用模型
 * 用于RAGFlow的文档引用
 */
data class DocumentReference(
    val documentId: String,         // 文档ID
    val documentName: String,       // 文档名称
    val documentType: String,       // 文档类型
    val content: String,            // 引用内容
    val pageNumber: Int? = null,    // 页码（如果适用）
    val confidence: Float? = null,  // 置信度
    val source: String              // 来源
) 