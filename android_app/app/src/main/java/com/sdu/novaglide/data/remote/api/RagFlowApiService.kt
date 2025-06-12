package com.sdu.novaglide.data.remote.api

import com.sdu.novaglide.data.remote.dto.ragflow.RagFlowChatResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * RAGFlow API服务接口
 */
interface RagFlowApiService {
    /**
     * 直接与指定的RAGFlow助手对话（使用固定的助手ID和会话ID）
     * @param bearerToken RAGFlow API授权令牌（Bearer token）
     * @param question 问题内容
     * @param stream 是否启用流式响应
     * @return 聊天响应
     */
    @POST("api/v1/chats/8ecce028389e11f092625aaa5731e5df/completions")
    suspend fun chatWithSpecificAssistant(
        @Header("Authorization") bearerToken: String,
        @Body request: Map<String, Any>
    ): RagFlowChatResponse
    /**
     * 获取所有助手列表
     * @param bearerToken RAGFlow API授权令牌（Bearer token）
     * @param page 页码，默认1
     * @param pageSize 每页数量，默认100
     * @param orderBy 排序字段，默认create_time
     * @param desc 是否降序，默认true
     * @param name 按名称过滤
     * @return 助手列表响应
     */
    @GET("api/v1/chats")
    suspend fun listChatAssistants(
        @Header("Authorization") bearerToken: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
        @Query("orderby") orderBy: String = "create_time",
        @Query("desc") desc: Boolean = true,
        @Query("name") name: String? = null
    ): Response<Map<String, Any>>

    /**
     * 创建与助手的会话
     * @param bearerToken RAGFlow API授权令牌（Bearer token）
     * @param chatId 助手ID
     * @param sessionName 会话名称
     * @return 会话创建响应
     */
    @POST("api/v1/chats/{chat_id}/sessions")
    suspend fun createChatSession(
        @Header("Authorization") bearerToken: String,
        @Path("chat_id") chatId: String,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>

    /**
     * 与助手对话
     * @param bearerToken RAGFlow API授权令牌（Bearer token）
     * @param chatId 助手ID
     * @param question 问题
     * @param sessionId 会话ID（可选）
     * @param stream 是否启用流式响应
     * @return 聊天响应
     */
    @POST("api/v1/chats/{chat_id}/completions")
    suspend fun chatWithAssistant(
        @Header("Authorization") bearerToken: String,
        @Path("chat_id") chatId: String,
        @Body request: Map<String, Any>
    ): RagFlowChatResponse

    /**
     * 流式对话
     * 注意：stream字段需要设置为true
     * @param bearerToken RAGFlow API授权令牌（Bearer token）
     * @param chatId 助手ID
     * @param question 问题
     * @param sessionId 会话ID（可选）
     * @param stream 是否启用流式响应
     * @return 流式响应体
     */
    @Streaming
    @POST("api/v1/chats/{chat_id}/completions")
    suspend fun streamChatWithAssistant(
        @Header("Authorization") bearerToken: String,
        @Path("chat_id") chatId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseBody>
    
    /**
     * 获取知识库列表
     * @param bearerToken RAGFlow API授权令牌（Bearer token）
     * @return 知识库列表响应
     */
    @GET("api/v1/knowledgebases")
    suspend fun getKnowledgeBases(
        @Header("Authorization") bearerToken: String
    ): Response<Map<String, Any>>
    
    /**
     * 获取共享聊天历史
     * @param bearerToken RAGFlow API授权令牌（Bearer token）
     * @param sharedId 共享ID
     * @return 共享聊天历史响应
     */
    @GET("api/v1/chat/share")
    suspend fun getSharedChat(
        @Header("Authorization") bearerToken: String,
        @Query("shared_id") sharedId: String
    ): Response<Map<String, Any>>

    /**
     * 获取知识库文档元数据列表
     * @param bearerToken RAGFlow API授权令牌（Bearer token）
     * @param datasetId 知识库ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param query 查询条件
     * @return 文档元数据列表响应
     */
    @GET("api/v1/datasets/{dataset_id}/documents")
    suspend fun getDatasetDocuments(
        @Header("Authorization") bearerToken: String,
        @Path("dataset_id") datasetId: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
        @Query("q") query: String? = null
    ): Response<Map<String, Any>>

    /**
     * 获取单个文档详情
     * @param bearerToken RAGFlow API授权令牌（Bearer token）
     * @param datasetId 知识库ID
     * @param docId 文档ID
     * @return 单个文档的元数据响应
     */
    @GET("api/v1/datasets/{dataset_id}/documents/{doc_id}")
    suspend fun getDatasetDocumentDetail(
        @Header("Authorization") bearerToken: String,
        @Path("dataset_id") datasetId: String,
        @Path("doc_id") docId: String
    ): Response<ResponseBody>
} 