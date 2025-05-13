package com.sdu.novaglide.data.remote.api

import com.sdu.novaglide.data.remote.dto.deepseek.DeepSeekChatRequest
import com.sdu.novaglide.data.remote.dto.deepseek.DeepSeekChatResponse
import com.sdu.novaglide.data.remote.dto.deepseek.DeepSeekStreamResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * DeepSeek API服务接口
 */
interface DeepSeekApiService {
    /**
     * 聊天完成接口
     * @param authorization Bearer认证头，格式为 "Bearer YOUR_API_KEY"
     * @param request 聊天请求
     * @return 聊天响应
     */
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekChatRequest
    ): DeepSeekChatResponse

    /**
     * 流式聊天完成接口
     * 注意：stream字段需要设置为true
     * @param authorization Bearer认证头，格式为 "Bearer YOUR_API_KEY"
     * @param request 聊天请求（需要设置stream=true）
     * @return 流式响应体
     */
    @Streaming
    @POST("v1/chat/completions")
    suspend fun streamChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekChatRequest
    ): Response<ResponseBody>
} 