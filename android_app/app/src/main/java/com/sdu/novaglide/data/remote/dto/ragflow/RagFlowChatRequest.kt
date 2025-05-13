package com.sdu.novaglide.data.remote.dto.ragflow

import com.google.gson.annotations.SerializedName

/**
 * RAGFlow聊天请求数据模型
 */
data class RagFlowChatRequest(
    @SerializedName("assistant_id")
    val assistantId: String,                // 助手ID
    
    @SerializedName("conversation_id")
    val conversationId: String? = null,     // 对话ID（用于继续对话）
    
    @SerializedName("query")
    val query: String,                      // 用户查询文本
    
    @SerializedName("stream")
    val stream: Boolean = false,            // 是否启用流式输出
    
    @SerializedName("sys_prompt")
    val systemPrompt: String? = null,       // 系统提示词（可选）
    
    @SerializedName("similarity_threshold")
    val similarityThreshold: Double = 0.2,  // 相似度阈值
    
    @SerializedName("top_n")
    val topN: Int = 10,                     // 提取的最大文档数量
    
    @SerializedName("keyword_similarity_weight")
    val keywordSimilarityWeight: Double = 0.7, // 关键词相似度权重
    
    @SerializedName("multi_turn_optimization")
    val multiTurnOptimization: Boolean = true, // 是否启用多轮优化
    
    @SerializedName("use_knowledge_graph")
    val useKnowledgeGraph: Boolean = false  // 是否使用知识图谱
) 