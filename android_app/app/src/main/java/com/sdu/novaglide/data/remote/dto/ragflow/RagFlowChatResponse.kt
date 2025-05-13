package com.sdu.novaglide.data.remote.dto.ragflow

import com.google.gson.annotations.SerializedName

/**
 * RAGFlow聊天响应数据模型
 */
data class RagFlowChatResponse(
    val code: Int,                          // 响应代码（0表示成功）
    val message: String,                    // 响应消息
    val data: RagFlowResponseData?          // 响应数据
)

/**
 * RAGFlow响应数据
 */
data class RagFlowResponseData(
    @SerializedName("assistant_id")
    val assistantId: String,                // 助手ID
    
    @SerializedName("conversation_id")
    val conversationId: String,             // 对话ID
    
    val answer: String,                     // 回答内容
    
    @SerializedName("retrieval_documents")
    val retrievalDocuments: List<RagFlowDocument>? = null, // 检索到的文档
    
    @SerializedName("quoted_documents")
    val quotedDocuments: List<RagFlowDocument>? = null,    // 引用的文档
    
    @SerializedName("status")
    val status: String                      // 状态
)

/**
 * RAGFlow文档模型
 */
data class RagFlowDocument(
    @SerializedName("id")
    val id: String,                         // 文档ID
    
    @SerializedName("file_path")
    val filePath: String? = null,           // 文件路径
    
    @SerializedName("file_name")
    val fileName: String,                   // 文件名
    
    @SerializedName("file_size")
    val fileSize: Long? = null,             // 文件大小
    
    @SerializedName("file_type")
    val fileType: String,                   // 文件类型
    
    @SerializedName("page_num")
    val pageNum: Int? = null,               // 页码
    
    @SerializedName("segment_id")
    val segmentId: String? = null,          // 段落ID
    
    @SerializedName("content")
    val content: String,                    // 内容
    
    @SerializedName("score")
    val score: Double? = null,              // 相关度得分
    
    @SerializedName("metadata")
    val metadata: Map<String, Any>? = null  // 元数据
)

/**
 * RAGFlow流式响应数据模型
 */
data class RagFlowStreamResponse(
    val code: Int,                          // 响应代码
    val message: String,                    // 响应消息
    val data: RagFlowStreamData?            // 流式响应数据
)

/**
 * RAGFlow流式响应数据
 */
data class RagFlowStreamData(
    @SerializedName("conversation_id")
    val conversationId: String,             // 对话ID
    
    @SerializedName("content")
    val content: String? = null,            // 内容片段
    
    @SerializedName("status")
    val status: String,                     // 状态："running" 或 "finished"
    
    @SerializedName("quoted_documents")
    val quotedDocuments: List<RagFlowDocument>? = null, // 引用的文档（仅在结束时出现）
    
    @SerializedName("retrieval_documents")
    val retrievalDocuments: List<RagFlowDocument>? = null // 检索到的文档（仅在结束时出现）
) 