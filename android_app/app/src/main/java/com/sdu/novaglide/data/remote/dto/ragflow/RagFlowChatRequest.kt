package com.sdu.novaglide.data.remote.dto.ragflow

/**
 * 新的请求格式，不再使用固定的请求模型
 * 现在使用Map<String, Any>构建请求体，以适应API变化
 * 
 * 主要使用的字段：
 * - 新建会话：{"name": String}
 * - 发送问题：{
 *   "question": String,      // 问题内容
 *   "stream": Boolean,       // 是否流式输出
 *   "session_id": String     // 可选的会话ID
 * }
 */
// 类已经删除，改用Map<String, Any>构建请求