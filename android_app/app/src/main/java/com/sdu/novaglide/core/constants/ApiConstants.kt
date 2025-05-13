package com.sdu.novaglide.core.constants

/**
 * API相关常量
 */
object ApiConstants {
    // DeepSeek API
    const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/"
    const val DEEPSEEK_MODEL = "deepseek-chat" // 使用默认模型，已升级为V3
    
    // RAGFlow API 
    const val RAGFLOW_BASE_URL = "https://demo.ragflow.io/" // 默认服务器，可通过设置更改
    
    // API服务超时设置（毫秒）
    const val CONNECT_TIMEOUT = 60_000L  // 增加超时时间到60秒
    const val READ_TIMEOUT = 120_000L    // 增加超时时间到120秒
    const val WRITE_TIMEOUT = 60_000L    // 增加超时时间到60秒
    
    // 偏好设置键
    const val PREF_DEEPSEEK_API_KEY = "deepseek_api_key"
    const val PREF_RAGFLOW_API_KEY = "ragflow_api_key"
    const val PREF_RAGFLOW_SERVER_URL = "ragflow_server_url"
    const val PREF_FIRST_RUN = "first_run"
} 