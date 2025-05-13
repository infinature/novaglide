package com.sdu.novaglide.data.remote.api

import com.sdu.novaglide.data.remote.dto.ragflow.RagFlowChatRequest
import com.sdu.novaglide.data.remote.dto.ragflow.RagFlowChatResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * RAGFlow API服务接口
 */
interface RagFlowApiService {
    /**
     * 获取所有助手列表
     * @param apiKey RAGFlow API密钥
     * @return 助手列表响应
     */
    @GET("api/v1/assistants")
    suspend fun getAssistants(
        @Header("X-API-KEY") apiKey: String
    ): Response<Map<String, Any>>

    /**
     * 聊天请求
     * @param apiKey RAGFlow API密钥
     * @param request 聊天请求
     * @return 聊天响应
     */
    @POST("api/v1/chat")
    suspend fun chat(
        @Header("X-API-KEY") apiKey: String,
        @Body request: RagFlowChatRequest
    ): RagFlowChatResponse

    /**
     * 流式聊天请求
     * 注意：stream字段需要设置为true
     * @param apiKey RAGFlow API密钥
     * @param request 聊天请求（需要设置stream=true）
     * @return 流式响应体
     */
    @Streaming
    @POST("api/v1/chat")
    suspend fun streamChat(
        @Header("X-API-KEY") apiKey: String,
        @Body request: RagFlowChatRequest
    ): Response<ResponseBody>

    /**
     * 获取对话历史
     * @param apiKey RAGFlow API密钥
     * @param conversationId 对话ID
     * @return 对话历史响应
     */
    @GET("api/v1/conversations/{conversationId}")
    suspend fun getConversationHistory(
        @Header("X-API-KEY") apiKey: String,
        @Path("conversationId") conversationId: String
    ): Response<Map<String, Any>>

    /**
     * 获取知识库列表
     * @param apiKey RAGFlow API密钥
     * @return 知识库列表响应
     */
    @GET("api/v1/knowledgebases")
    suspend fun getKnowledgeBases(
        @Header("X-API-KEY") apiKey: String
    ): Response<Map<String, Any>>
} 