package com.sdu.novaglide.data.remote.dto.deepseek

import com.google.gson.annotations.SerializedName

/**
 * DeepSeek聊天请求数据模型
 * 参考DeepSeek API文档：https://api-docs.deepseek.com/zh-cn/api/deepseek-api
 */
data class DeepSeekChatRequest(
    val model: String,                         // 模型名称，如"deepseek-chat"
    val messages: List<DeepSeekMessage>,       // 消息列表
    val temperature: Double = 0.7,             // 温度（随机性）
    
    @SerializedName("max_tokens")
    val maxTokens: Int? = 4096,                // 最大生成令牌数
    
    @SerializedName("top_p")
    val topP: Double = 0.8,                    // 核采样阈值
    
    @SerializedName("presence_penalty")
    val presencePenalty: Double = 0.0,         // 存在惩罚
    
    @SerializedName("frequency_penalty")
    val frequencyPenalty: Double = 0.0,        // 频率惩罚
    
    val stream: Boolean = false,               // 是否启用流式输出
    
    @SerializedName("deep_thinking") 
    val deepThinking: Boolean = false          // 是否启用深度思考模式
)

/**
 * DeepSeek消息格式
 */
data class DeepSeekMessage(
    val role: String,              // 角色："system", "user" 或 "assistant"
    val content: String            // 消息内容
) 