package com.sdu.novaglide.data.remote.dto.deepseek

import com.google.gson.annotations.SerializedName

/**
 * DeepSeek聊天响应数据模型
 */
data class DeepSeekChatResponse(
    val id: String,                         // 响应ID
    val created: Long,                      // 创建时间戳（Unix时间戳，单位为秒）
    val model: String,                      // 使用的模型名称
    val choices: List<DeepSeekChoice>,      // 选择列表（通常只有一个元素）
    val usage: DeepSeekUsage                // 使用情况（令牌计数）
)

/**
 * DeepSeek响应选择
 */
data class DeepSeekChoice(
    val index: Int,                         // 选择索引
    val message: DeepSeekMessage,           // 消息内容
    
    @SerializedName("finish_reason")
    val finishReason: String?               // 结束原因："stop"、"length"等
)

/**
 * DeepSeek使用情况
 */
data class DeepSeekUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,                  // 提示令牌数
    
    @SerializedName("completion_tokens")
    val completionTokens: Int,              // 完成令牌数
    
    @SerializedName("total_tokens")
    val totalTokens: Int                    // 总令牌数
)

/**
 * DeepSeek流式响应数据模型
 */
data class DeepSeekStreamResponse(
    val id: String,                         // 响应ID
    val created: Long,                      // 创建时间戳
    val model: String,                      // 使用的模型名称
    val choices: List<DeepSeekStreamChoice> // 流式选择列表
)

/**
 * DeepSeek流式响应选择
 */
data class DeepSeekStreamChoice(
    val index: Int,                         // 选择索引
    val delta: DeepSeekDelta,               // 增量内容
    
    @SerializedName("finish_reason")
    val finishReason: String?               // 结束原因
)

/**
 * DeepSeek增量内容
 */
data class DeepSeekDelta(
    val role: String? = null,               // 角色（只在第一个块中出现）
    val content: String? = null             // 内容片段
) 