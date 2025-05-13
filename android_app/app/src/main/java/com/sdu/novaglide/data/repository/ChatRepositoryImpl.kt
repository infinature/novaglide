package com.sdu.novaglide.data.repository

import android.util.Log
import com.google.gson.Gson
import com.sdu.novaglide.core.constants.ApiConstants
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.data.remote.api.DeepSeekApiService
import com.sdu.novaglide.data.remote.api.RagFlowApiService
import com.sdu.novaglide.data.remote.dto.deepseek.DeepSeekChatRequest
import com.sdu.novaglide.data.remote.dto.deepseek.DeepSeekMessage
import com.sdu.novaglide.data.remote.dto.deepseek.DeepSeekStreamResponse
import com.sdu.novaglide.data.remote.dto.ragflow.RagFlowChatRequest
import com.sdu.novaglide.data.remote.dto.ragflow.RagFlowDocument
import com.sdu.novaglide.data.remote.dto.ragflow.RagFlowStreamResponse
import com.sdu.novaglide.domain.model.ChatMessage
import com.sdu.novaglide.domain.model.DocumentReference
import com.sdu.novaglide.domain.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import kotlinx.coroutines.flow.first
import org.json.JSONObject

/**
 * 聊天仓库实现类
 */
class ChatRepositoryImpl(
    private val deepSeekApiService: DeepSeekApiService,
    private val ragFlowApiService: RagFlowApiService?,
    private val apiKeyStore: ApiKeyStore
) : ChatRepository {

    private val TAG = "ChatRepositoryImpl"
    private val gson = Gson()

    /**
     * 发送聊天消息到DeepSeek API
     */
    override suspend fun sendMessageToDeepSeek(
        messages: List<ChatMessage>,
        userMessage: String,
        useStream: Boolean
    ): Flow<String> = flow {
        try {
            // 获取API密钥
            val apiKey = apiKeyStore.deepSeekApiKey.first()
            Log.d(TAG, "DeepSeek API密钥长度: ${apiKey?.length ?: 0}")
            
            if (apiKey.isNullOrEmpty()) {
                Log.e(TAG, "DeepSeek API密钥未配置")
                throw IllegalStateException("DeepSeek API密钥未配置")
            }

            // 构建请求体
            val deepSeekMessages = messages.map {
                DeepSeekMessage(
                    role = if (it.role == MessageRole.USER) "user" else "assistant",
                    content = it.content
                )
            }.toMutableList()

            // 添加系统消息
            if (deepSeekMessages.none { it.role == "system" }) {
                deepSeekMessages.add(0, DeepSeekMessage(
                    role = "system",
                    content = "你是一个有用的AI助手。请用中文回答问题，除非用户要求使用其他语言。"
                ))
            }

            // 添加用户新消息
            deepSeekMessages.add(DeepSeekMessage(
                role = "user",
                content = userMessage
            ))

            val request = DeepSeekChatRequest(
                model = ApiConstants.DEEPSEEK_MODEL,
                messages = deepSeekMessages,
                stream = useStream
            )
            
            Log.d(TAG, "发送请求到DeepSeek API，使用模型: ${ApiConstants.DEEPSEEK_MODEL}, 消息数量: ${deepSeekMessages.size}, 流式模式: $useStream")

            if (useStream) {
                // 流式请求
                Log.d(TAG, "开始发送流式请求")
                val response = deepSeekApiService.streamChatCompletion(
                    authorization = "Bearer $apiKey",
                    request = request
                )

                if (response.isSuccessful) {
                    Log.d(TAG, "流式请求成功，状态码: ${response.code()}")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Log.d(TAG, "开始处理流式响应")
                        processDeepSeekStreamResponse(responseBody) { content ->
                            emit(content)
                        }
                    } else {
                        Log.e(TAG, "流式响应体为空，状态码: ${response.code()}")
                        emit("错误：响应体为空")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "流式请求失败，状态码: ${response.code()}, 错误信息: ${response.message()}, 错误体: $errorBody")
                    emit("错误：${response.code()} - ${response.message()} - $errorBody")
                }
            } else {
                // 非流式请求
                Log.d(TAG, "开始发送非流式请求")
                val response = deepSeekApiService.chatCompletion(
                    authorization = "Bearer $apiKey",
                    request = request
                )
                Log.d(TAG, "非流式请求成功，选择数: ${response.choices.size}")
                emit(response.choices[0].message.content)
            }
        } catch (e: Exception) {
            Log.e(TAG, "发送DeepSeek消息时出错", e)
            emit("发送消息时出错: ${e.message}")
        }
    }

    /**
     * 发送查询到RAGFlow
     */
    override suspend fun sendQueryToRagFlow(
        query: String,
        conversationId: String?,
        assistantId: String,
        useStream: Boolean
    ): Flow<Pair<String, List<DocumentReference>?>> = flow {
        var request: RagFlowChatRequest? = null
        try {
            // 检查RAGFlow API是否配置
            if (ragFlowApiService == null) {
                Log.w(TAG, "RAGFlow API服务未配置，无法发送查询")
                throw IllegalStateException("RAGFlow API服务未配置")
            }

            // 获取API密钥
            val apiKey = apiKeyStore.ragFlowApiKey.first()
            if (apiKey.isNullOrEmpty()) {
                Log.w(TAG, "RAGFlow API密钥未配置，无法发送查询")
                throw IllegalStateException("RAGFlow API密钥未配置")
            }

            // 构建请求体
            request = RagFlowChatRequest(
                assistantId = assistantId,
                conversationId = conversationId,
                query = query,
                stream = useStream
            )
            Log.d(TAG, "构造RAGFlow请求: $request, API Key长度: ${apiKey.length}")

            if (useStream) {
                // 流式请求
                val response = ragFlowApiService.streamChat(
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        var documents: List<DocumentReference>? = null
                        var content = StringBuilder()

                        processRagFlowStreamResponse(responseBody) { streamResponse ->
                            if (streamResponse.data?.status == "finished") {
                                // 只在最后一个流块中提取文档引用
                                documents = streamResponse.data.quotedDocuments?.map { doc ->
                                    mapRagFlowDocumentToReference(doc)
                                }
                            }
                            
                            // 提取内容片段
                            val fragment = streamResponse.data?.content
                            if (fragment != null) {
                                content.append(fragment)
                                emit(Pair(content.toString(), documents))
                            }
                        }
                    } else {
                        Log.e(TAG, "RAGFlow流式响应体为空，请求URL: /api/v1/chat, 请求: $request")
                        emit(Pair("错误：响应体为空", null))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "RAGFlow流式请求失败，状态码: ${response.code()}, 错误信息: ${response.message()}, 请求URL: /api/v1/chat, 请求: $request, 错误体: $errorBody")
                    emit(Pair("错误：${response.code()} - ${response.message()}", null))
                }
            } else {
                // 非流式请求
                val response = ragFlowApiService.chat(
                    apiKey = apiKey,
                    request = request
                )

                if (response.code == 0) {
                    val answer = response.data?.answer ?: "无回答"
                    val documents = response.data?.quotedDocuments?.map { doc ->
                        mapRagFlowDocumentToReference(doc)
                    }
                    emit(Pair(answer, documents))
                } else {
                    Log.e(TAG, "RAGFlow非流式请求失败，代码: ${response.code}, 消息: ${response.message}, 请求URL: /api/v1/chat, 请求: $request")
                    emit(Pair("错误：${response.message}", null))
                }
            }
        } catch (e: Exception) {
            val serverUrl = apiKeyStore.ragFlowServerUrl.first() ?: "未配置"
            val apiKeyProvided = !apiKeyStore.ragFlowApiKey.first().isNullOrEmpty()
            Log.e(TAG, "发送RAGFlow查询时出错。服务器URL: $serverUrl, API密钥已提供: $apiKeyProvided, 请求: ${request ?: "初始化失败或未进入try"}, 助手ID: $assistantId", e)
            emit(Pair("发送查询时出错: ${e.message} (详情请查看Logcat)", null))
        }
    }

    /**
     * 获取RAGFlow的所有助手
     */
    override suspend fun getRagFlowAssistants(): Map<String, String> {
        try {
            // 检查RAGFlow API是否配置
            if (ragFlowApiService == null) {
                return emptyMap()
            }

            // 获取API密钥
            val apiKey = apiKeyStore.ragFlowApiKey.first()
            if (apiKey.isNullOrEmpty()) {
                return emptyMap()
            }

            // 请求助手列表
            val response = ragFlowApiService.getAssistants(apiKey)
            if (response.isSuccessful) {
                val data = response.body()
                if (data != null && data.containsKey("data")) {
                    val assistantsData = data["data"] as? List<*> ?: return emptyMap()
                    
                    val result = mutableMapOf<String, String>()
                    for (assistant in assistantsData) {
                        if (assistant is Map<*, *>) {
                            val id = assistant["id"] as? String ?: continue
                            val name = assistant["name"] as? String ?: continue
                            result[id] = name
                        }
                    }
                    return result
                }
            }
            return emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "获取RAGFlow助手时出错", e)
            return emptyMap()
        }
    }

    /**
     * 获取RAGFlow的知识库列表
     */
    override suspend fun getRagFlowKnowledgeBases(): Map<String, String> {
        try {
            // 检查RAGFlow API是否配置
            if (ragFlowApiService == null) {
                return emptyMap()
            }

            // 获取API密钥
            val apiKey = apiKeyStore.ragFlowApiKey.first()
            if (apiKey.isNullOrEmpty()) {
                return emptyMap()
            }

            // 请求知识库列表
            val response = ragFlowApiService.getKnowledgeBases(apiKey)
            if (response.isSuccessful) {
                val data = response.body()
                if (data != null && data.containsKey("data")) {
                    val kbsData = data["data"] as? List<*> ?: return emptyMap()
                    
                    val result = mutableMapOf<String, String>()
                    for (kb in kbsData) {
                        if (kb is Map<*, *>) {
                            val id = kb["id"] as? String ?: continue
                            val name = kb["name"] as? String ?: continue
                            result[id] = name
                        }
                    }
                    return result
                }
            }
            return emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "获取RAGFlow知识库时出错", e)
            return emptyMap()
        }
    }

    /**
     * 检查API密钥和设置是否已配置
     */
    override suspend fun isApiConfigured(): Boolean {
        val deepSeekApiKey = apiKeyStore.deepSeekApiKey.first()
        val ragFlowApiKey = apiKeyStore.ragFlowApiKey.first()
        val ragFlowServerUrl = apiKeyStore.ragFlowServerUrl.first()
        
        return !deepSeekApiKey.isNullOrEmpty() || 
               (!ragFlowApiKey.isNullOrEmpty() && !ragFlowServerUrl.isNullOrEmpty())
    }

    /**
     * 处理DeepSeek流式响应
     */
    private suspend fun processDeepSeekStreamResponse(
        responseBody: ResponseBody,
        onContent: suspend (content: String) -> Unit
    ) {
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
        try {
            var line: String?
            val responseBuffer = StringBuilder()
            
            Log.d(TAG, "开始读取流式响应")
            var linesRead = 0
            var emptyLines = 0

            while (reader.readLine().also { line = it } != null) {
                linesRead++
                
                // 处理空行（心跳保持连接）
                if (line.isNullOrEmpty()) {
                    emptyLines++
                    if (emptyLines % 50 == 0) {
                        Log.d(TAG, "收到${emptyLines}个空行...")
                    }
                    continue
                }
                
                // 处理keep-alive消息
                if (line == ": keep-alive") {
                    continue
                }
                
                if (line?.startsWith("data: ") == true) {
                    val data = line!!.substring(6).trim()
                    
                    // 处理完成标记
                    if (data == "[DONE]") {
                        Log.d(TAG, "收到[DONE]标记，流式响应完成")
                        break
                    }

                    try {
                        val streamResponse = gson.fromJson(data, DeepSeekStreamResponse::class.java)
                        val content = streamResponse.choices[0].delta.content
                        if (!content.isNullOrEmpty()) {
                            responseBuffer.append(content)
                            onContent(responseBuffer.toString())
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "解析DeepSeek流响应时出错: $data", e)
                    }
                } else {
                    Log.d(TAG, "收到非data行: $line")
                }
                
                // 每100行记录一次日志
                if (linesRead % 100 == 0) {
                    Log.d(TAG, "已处理${linesRead}行响应")
                }
            }
            
            Log.d(TAG, "流式响应处理完成，共读取${linesRead}行，其中${emptyLines}个空行")
        } catch (e: Exception) {
            Log.e(TAG, "处理流式响应时出错", e)
        } finally {
            reader.close()
        }
    }

    /**
     * 处理RAGFlow流式响应
     */
    private suspend fun processRagFlowStreamResponse(
        responseBody: ResponseBody,
        onChunk: suspend (response: RagFlowStreamResponse) -> Unit
    ) {
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line?.startsWith("data: ") == true) {
                    val data = line!!.substring(6)
                    try {
                        val streamResponse = gson.fromJson(data, RagFlowStreamResponse::class.java)
                        onChunk(streamResponse)
                    } catch (e: Exception) {
                        Log.e(TAG, "解析RAGFlow流响应时出错", e)
                    }
                }
            }
        } finally {
            reader.close()
        }
    }

    /**
     * 将RAGFlow文档转换为文档引用模型
     */
    private fun mapRagFlowDocumentToReference(doc: RagFlowDocument): DocumentReference {
        return DocumentReference(
            documentId = doc.id,
            documentName = doc.fileName,
            documentType = doc.fileType,
            content = doc.content,
            pageNumber = doc.pageNum,
            confidence = doc.score?.toFloat(),
            source = doc.filePath ?: doc.fileName
        )
    }
} 