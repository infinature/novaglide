package com.sdu.novaglide.core.constants

/**
 * API相关常量
 */
object ApiConstants {
    // DeepSeek API
    const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/"
    const val DEEPSEEK_MODEL = "deepseek-chat" // 使用默认模型，已升级为V3
    
    // RAGFlow API 
    const val RAGFLOW_BASE_URL = "http://10.0.2.2:9380/" // 本地RAGFlow服务地址 (模拟器)
    // 注意：尝试使用标准的https协议和端口
    
    // API服务超时设置（毫秒）
    const val CONNECT_TIMEOUT = 60_000L  // 增加超时时间到60秒
    const val READ_TIMEOUT = 300_000L    // 增加超时时间到300秒（5分钟）
    const val WRITE_TIMEOUT = 60_000L    // 增加超时时间到60秒
    
    // 偏好设置键
    const val PREF_DEEPSEEK_API_KEY = "deepseek_api_key"
    const val PREF_RAGFLOW_API_KEY = "ragflow_api_key"
    const val PREF_RAGFLOW_SERVER_URL = "ragflow_server_url"
    const val PREF_FIRST_RUN = "first_run"
} 