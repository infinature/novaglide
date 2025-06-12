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
import com.sdu.novaglide.data.remote.dto.ragflow.RagFlowDocument
import com.sdu.novaglide.data.remote.dto.ragflow.RagFlowStreamResponse
import com.sdu.novaglide.domain.model.ChatMessage
import com.sdu.novaglide.domain.model.DocumentReference
import com.sdu.novaglide.domain.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
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
            
            // 添加重试机制
            var retryCount = 0
            val maxRetries = 3
            
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
                var lastException: Exception? = null
                var response = try {
                    // 尝试执行请求
                    deepSeekApiService.streamChatCompletion(
                        authorization = "Bearer $apiKey",
                        request = request
                    )
                } catch (e: Exception) {
                    // 如果首次请求失败，记录异常并开始重试
                    Log.w(TAG, "首次DeepSeek API请求失败，将尝试重试", e)
                    lastException = e
                    retryCount++
                    null
                }
                
                // 如果首次请求失败，则进行重试
                while (response == null && retryCount <= maxRetries) {
                    try {
                        // 指数退避策略等待
                        val delayTime = 1000L * (1 shl (retryCount - 1))
                        Log.d(TAG, "等待${delayTime}ms后进行第${retryCount}次重试")
                        kotlinx.coroutines.delay(delayTime)
                        
                        // 重试请求
                        response = deepSeekApiService.streamChatCompletion(
                            authorization = "Bearer $apiKey",
                            request = request
                        )
                        Log.d(TAG, "第${retryCount}次重试成功")
                    } catch (e: Exception) {
                        lastException = e
                        Log.w(TAG, "DeepSeek API请求失败，已尝试${retryCount}次", e)
                        retryCount++
                    }
                }
                
                // 如果所有重试都失败，抛出最后一个异常
                if (response == null) {
                    Log.e(TAG, "DeepSeek API请求在${maxRetries}次尝试后仍然失败")
                    throw lastException ?: IllegalStateException("请求失败，但没有捕获到异常")
                }

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
            
            val bearerToken = "Bearer $apiKey"
            var sessionId = conversationId
            
            // 如果没有指定会话，则创建一个新会话
            if (sessionId == null) {
                try {
                    Log.d(TAG, "创建新会话与助手: $assistantId")
                    val sessionRequest = mapOf(
                        "name" to "NovaGlide_Session_${System.currentTimeMillis()}"
                    )
                    
                    val response = ragFlowApiService.createChatSession(
                        bearerToken = bearerToken,
                        chatId = assistantId,
                        request = sessionRequest
                    )
                    
                    if (response.isSuccessful) {
                        val responseData = response.body()
                        if (responseData != null && responseData.containsKey("data")) {
                            val data = responseData["data"] as? Map<*, *>
                            sessionId = data?.get("id") as? String
                            Log.d(TAG, "成功创建会话，ID: $sessionId")
                        } else {
                            Log.w(TAG, "创建会话响应格式异常: $responseData")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "创建会话失败，状态码: ${response.code()}, 错误信息: ${response.message()}, 错误体: $errorBody")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "创建会话时出错", e)
                    // 如果创建会话失败，继续尝试发送查询，让服务器自动创建会话
                }
            }
            
            val completionRequest = mutableMapOf<String, @JvmSuppressWildcards Any>(
                "question" to query,
                "stream" to useStream
            )
            
            if (sessionId != null) {
                completionRequest["session_id"] = sessionId
            }
            
            Log.d(TAG, "构造对话请求: $completionRequest, 助手ID: $assistantId, 会话ID: $sessionId")
            
            if (useStream) {
                val response = ragFlowApiService.streamChatWithAssistant(
                    bearerToken = bearerToken,
                    chatId = assistantId,
                    request = completionRequest
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        var documents: List<DocumentReference>? = null
                        var lastContent = "" 

                        processRagFlowStreamResponse(responseBody) { streamResponse ->
                            if (streamResponse.data?.status == "finished") {
                                documents = streamResponse.data.quotedDocuments?.map { doc ->
                                    mapRagFlowDocumentToReference(doc)
                                }
                                emit(Pair(lastContent, documents))
                                Log.d(TAG, "[RAGFLOW] 流式响应完成，最终内容长度: ${lastContent.length}，内容前200个字符: ${lastContent.take(200)}")
                            } else {
                                val newContent = streamResponse.data?.content
                                if (newContent != null) {
                                    if (newContent.length > lastContent.length && newContent.startsWith(lastContent)) {
                                        lastContent = newContent
                                        Log.d(TAG, "[RAGFLOW] 服务器返回累积内容，完整长度: ${newContent.length}，内容前200个字符: ${newContent.take(200)}")
                                    } else if (newContent != lastContent) {
                                        if (lastContent.isEmpty()) {
                                            lastContent = newContent
                                        } else {
                                            lastContent = newContent 
                                        }
                                        Log.d(TAG, "[RAGFLOW] 接收到新内容，长度: ${newContent.length}，内容前200个字符: ${newContent.take(200)}")
                                    }
                                    emit(Pair(lastContent, documents))
                                    Log.d(TAG, "[RAGFLOW] 发送到UI的内容长度: ${lastContent.length}，内容前100个字符: ${lastContent.take(100)}")
                                }
                            }
                        }
                    } else { // responseBody is null
                        Log.e(TAG, "[RAGFLOW] 流式响应体为空，助手ID: $assistantId")
                        emit(Pair("错误：响应体为空", null))
                    }
                } else { // response not successful
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "[RAGFLOW] 流式请求失败，状态码: ${response.code()}, 错误信息: ${response.message()}, 助手ID: $assistantId, 错误体: $errorBody")
                    emit(Pair("错误：${response.code()} - ${response.message()}", null))
                }
            } else { // not useStream
                val response = ragFlowApiService.chatWithAssistant(
                    bearerToken = bearerToken,
                    chatId = assistantId,
                    request = completionRequest
                )

                if (response.code == 0) { // Assuming 0 is success for non-streamed
                    val answer = response.data?.answer ?: "无回答"
                    val documents = response.data?.quotedDocuments?.map { doc ->
                        mapRagFlowDocumentToReference(doc)
                    }
                    emit(Pair(answer, documents))
                } else {
                    Log.e(TAG, "[RAGFLOW] 非流式请求失败，代码: ${response.code}, 消息: ${response.message}, 助手ID: $assistantId")
                    emit(Pair("错误：${response.message}", null))
                }
            }
        } catch (e: Exception) {
            val serverUrl = apiKeyStore.ragFlowServerUrl.first() ?: "未配置"
            val apiKeyProvided = !apiKeyStore.ragFlowApiKey.first().isNullOrEmpty()
            Log.e(TAG, "发送RAGFlow查询时出错。服务器URL: $serverUrl, API密钥已提供: $apiKeyProvided, 助手ID: $assistantId", e)
            emit(Pair("发送查询时出错: ${e.message} (详情请查看Logcat)", null))
        }
    } // End of sendQueryToRagFlow method

    override suspend fun getRagFlowAssistants(): Map<String, String> {
        if (ragFlowApiService == null) {
            Log.w(TAG, "RAGFlow API服务未配置，无法获取助手列表")
            return emptyMap()
        }
        val apiKey = apiKeyStore.ragFlowApiKey.first()
        if (apiKey.isNullOrEmpty()) {
            Log.w(TAG, "RAGFlow API密钥未配置，无法获取助手列表")
            return emptyMap()
        }

        val bearerToken = "Bearer $apiKey"
        try {
            Log.d(TAG, "开始获取RAGFlow助手列表，使用新的API路径 /api/v1/chats")
            val response = ragFlowApiService.listChatAssistants(bearerToken) // <--- 修改在这里

            // 新增：打印原始返回内容
            try {
                val rawBody = response.body()
                Log.e(TAG, "[调试] RAGFlow助手列表-原始body: $rawBody")
            } catch (e: Exception) {
                Log.e(TAG, "[调试] 读取RAGFlow助手列表body时异常", e)
            }
            try {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "[调试] RAGFlow助手列表-原始errorBody: $errorBody")
            } catch (e: Exception) {
                Log.e(TAG, "[调试] 读取RAGFlow助手列表errorBody时异常", e)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null && responseBody.containsKey("data")) {
                    val dataArray = responseBody["data"] as? List<*>
                    if (dataArray != null) {
                        val assistantsMap = mutableMapOf<String, String>()
                        for (item in dataArray) {
                            if (item is Map<*, *>) {
                                val id = item["id"] as? String
                                val name = item["name"] as? String
                                if (id != null && name != null) {
                                    assistantsMap[id] = name
                                }
                            }
                        }
                        Log.d(TAG, "成功获取并解析RAGFlow助手列表，数量: ${assistantsMap.size}")
                        return assistantsMap
                    } else {
                        Log.w(TAG, "RAGFlow助手列表响应的data字段不是预期的列表类型: $dataArray")
                    }
                } else {
                    Log.w(TAG, "RAGFlow助手列表响应体为空或不包含data字段: $responseBody")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "获取RAGFlow助手列表失败，状态码: ${response.code()}, 错误: ${response.message()}, 错误体: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取RAGFlow助手列表时发生异常", e)
        }
        return emptyMap()
    }

    override suspend fun getRagFlowKnowledgeBases(): Map<String, String> {
        // TODO: 实现获取知识库的逻辑，如果需要的话
        Log.w(TAG, "getRagFlowKnowledgeBases 方法尚未实现")
        return emptyMap()
    }

    override suspend fun isApiConfigured(): Boolean {
        val deepSeekApiKey = apiKeyStore.deepSeekApiKey.first()
        val ragFlowApiKey = apiKeyStore.ragFlowApiKey.first()
        val ragFlowServerUrl = apiKeyStore.ragFlowServerUrl.first()
        return !deepSeekApiKey.isNullOrEmpty() && !ragFlowApiKey.isNullOrEmpty() && !ragFlowServerUrl.isNullOrEmpty()
    }

    override suspend fun getSharedChat(
        sharedId: String,
        authToken: String
    ): Flow<List<com.sdu.novaglide.data.remote.dto.ragflow.RagFlowChatHistoryItem>> = flow {
        if (ragFlowApiService == null) {
            Log.w(TAG, "RAGFlow API服务未配置，无法获取共享聊天")
            throw IllegalStateException("RAGFlow API服务未配置")
        }
        // 注意：共享聊天API可能使用不同的认证方式，或者authToken本身就是bearer token
        // 这里假设authToken可以直接用作Bearer token，如果不是，需要调整
        val bearerToken = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"

        try {
            Log.d(TAG, "请求共享聊天历史: $sharedId")
            val response = ragFlowApiService.getSharedChat(
                bearerToken = bearerToken, 
                sharedId = sharedId
            )

            if (response.isSuccessful) {
                val responseData = response.body()
                Log.d(TAG, "共享聊天响应: $responseData")
                if (responseData != null && responseData.containsKey("data")) {
                    val data = responseData["data"] as? Map<*, *>
                    val history = data?.get("history") as? List<*>
                    if (history != null) {
                        val chatHistory = mutableListOf<com.sdu.novaglide.data.remote.dto.ragflow.RagFlowChatHistoryItem>()
                        history.forEach { item ->
                            if (item is Map<*, *>) {
                                val role = item["role"] as? String
                                val content = item["content"] as? String
                                val timestamp = (item["timestamp"] as? Number)?.toLong()
                                val documents = (item["documents"] as? List<*>)?.mapNotNull { doc ->
                                    if (doc is Map<*, *>) {
                                        try {
                                            gson.fromJson(gson.toJson(doc), RagFlowDocument::class.java)?.let {
                                                mapRagFlowDocumentToReference(it) // 这行会报错，因为类型不匹配，应该直接用RagFlowDocument
                                            }
                                            // 修正：直接使用RagFlowDocument，因为getSharedChat的Flow类型期望RagFlowChatHistoryItem，其内部包含RagFlowDocument
                                            gson.fromJson(gson.toJson(doc), RagFlowDocument::class.java)
                                        } catch (e: Exception) {
                                            Log.e(TAG, "解析共享聊天中的文档时出错", e)
                                            null
                                        }
                                    } else null
                                }
                                if (role != null && content != null) {
                                    chatHistory.add(
                                        com.sdu.novaglide.data.remote.dto.ragflow.RagFlowChatHistoryItem(
                                            role = role,
                                            content = content,
                                            documents = documents?.filterIsInstance<RagFlowDocument>(), // 确保类型正确
                                            timestamp = timestamp
                                        )
                                    )
                                }
                            }
                        }
                        Log.d(TAG, "成功解析共享聊天历史，共${chatHistory.size}条消息")
                        emit(chatHistory)
                    } else {
                        Log.w(TAG, "共享聊天历史为空或格式不正确")
                        emit(emptyList())
                    }
                } else {
                    Log.w(TAG, "共享聊天响应格式异常: $responseData")
                    emit(emptyList())
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "获取共享聊天失败，状态码: ${response.code()}, 错误信息: ${response.message()}, 错误体: $errorBody")
                throw IllegalStateException("获取共享聊天失败: ${response.message()} ($errorBody)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取共享聊天时出错", e)
            throw e
        }
    }

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

    private suspend fun processDeepSeekStreamResponse(
        responseBody: ResponseBody,
        onContent: suspend (content: String) -> Unit
    ) {
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
        try {
            var line: String?
            val responseBuffer = StringBuilder()
            
            Log.d(TAG, "开始读取DeepSeek流式响应")
            var linesRead = 0
            var emptyLines = 0

            while (reader.readLine().also { line = it } != null) {
                linesRead++
                
                if (line.isNullOrEmpty()) {
                    emptyLines++
                    if (emptyLines % 50 == 0) {
                        Log.d(TAG, "收到${emptyLines}个空行...")
                    }
                    continue
                }
                
                if (line == ": keep-alive") {
                    continue
                }
                
                if (line?.startsWith("data: ") == true) {
                    val data = line!!.substring(6).trim()
                    
                    if (data == "[DONE]") {
                        Log.d(TAG, "收到[DONE]标记，DeepSeek流式响应完成")
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
                
                if (linesRead % 100 == 0) {
                    Log.d(TAG, "已处理${linesRead}行DeepSeek响应")
                }
            }
            Log.d(TAG, "DeepSeek流式响应处理完成，共读取${linesRead}行，其中${emptyLines}个空行")
        } catch (e: Exception) {
            Log.e(TAG, "处理DeepSeek流式响应时出错", e)
        } finally {
            try {
                reader.close()
            } catch (ioe: IOException) {
                Log.e(TAG, "关闭DeepSeek响应读取器时出错", ioe)
            }
        }
    }

    private suspend fun processRagFlowStreamResponse(
        responseBody: ResponseBody,
        onChunk: suspend (response: RagFlowStreamResponse) -> Unit
    ) {
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
        try {
            var line: String?
            var totalLinesProcessed = 0
            var successfullyProcessed = 0
            val fullResponseBuilder = StringBuilder()

            while (reader.readLine().also { line = it } != null) {
                totalLinesProcessed++
                val trimmedLine = line?.trim() ?: continue
                fullResponseBuilder.append(trimmedLine).append("\n")
                
                Log.d(TAG, "[RAGFLOW] 响应行 #${totalLinesProcessed}: ${trimmedLine.take(200)}${if (trimmedLine.length > 200) "..." else ""}")
                
                if (trimmedLine.startsWith("data:")) {
                    val jsonData = trimmedLine.substring(5).trim()
                    if (jsonData.isEmpty()) continue
                    
                    if (jsonData == "{\"code\": 0, \"data\": true}" || 
                        jsonData.contains("\"data\":true") || 
                        jsonData.contains("\"data\": true")) {
                        Log.d(TAG, "[RAGFLOW] 检测到流式结束消息")
                        val finalResponse = RagFlowStreamResponse(
                            code = 0, message = "",
                            data = com.sdu.novaglide.data.remote.dto.ragflow.RagFlowStreamData(
                                conversationId = "", content = null, status = "finished"
                            )
                        )
                        onChunk(finalResponse)
                        successfullyProcessed++
                        continue
                    }
                    processJsonData(jsonData, onChunk, totalLinesProcessed) { success ->
                        if (success) successfullyProcessed++
                    }
                } else if (!trimmedLine.startsWith("data:") && totalLinesProcessed <= 2) {
                    processJsonData(trimmedLine, onChunk, totalLinesProcessed) { success ->
                        if (success) successfullyProcessed++
                    }
                }
            }
            
            if (totalLinesProcessed > 0 && successfullyProcessed == 0) {
                Log.w(TAG, "[RAGFLOW] 注意: 已处理${totalLinesProcessed}行数据，但没有成功解析任何消息，尝试整体解析")
                val fullResponse = fullResponseBuilder.toString()
                try {
                    val jsonPattern = """{.*?\"answer\".*?}"""
                    val matcher = Regex(jsonPattern, RegexOption.DOT_MATCHES_ALL).find(fullResponse)
                    if (matcher != null) {
                        processJsonData(matcher.value, onChunk, -1) { success ->
                            if (success) successfullyProcessed++
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[RAGFLOW] 整体解析JSON失败: ${e.message}")
                }
                if (successfullyProcessed == 0) {
                    try {
                        val answerPattern = """\"answer\":[ \t]*\"([^\"]+)\""""
                        val matcher = Regex(answerPattern).find(fullResponse)
                        if (matcher != null) {
                            val answerContent = matcher.groupValues[1]
                            if (answerContent.isNotEmpty()) {
                                val streamData = com.sdu.novaglide.data.remote.dto.ragflow.RagFlowStreamData(
                                    conversationId = "", content = answerContent, status = "finished"
                                )
                                onChunk(RagFlowStreamResponse(code = 0, message = "", data = streamData))
                                successfullyProcessed++
                                Log.d(TAG, "[RAGFLOW] 通过整体正则提取回答，长度: ${answerContent.length}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "[RAGFLOW] 通过正则提取answer失败: ${e.message}")
                    }
                }
            }
            
            if (totalLinesProcessed > 0 && successfullyProcessed == 0) {
                Log.w(TAG, "[RAGFLOW] 所有解析尝试均失败，提供默认响应")
                onChunk(RagFlowStreamResponse(code = 0, message = "", data = com.sdu.novaglide.data.remote.dto.ragflow.RagFlowStreamData(
                    conversationId = "", content = "抱歉，无法解析服务器响应。", status = "finished"
                )))
            }
            Log.d(TAG, "[RAGFLOW] 流式响应处理完成，共处理${totalLinesProcessed}行，成功解析${successfullyProcessed}条消息")
        } catch (e: Exception) {
            Log.e(TAG, "[RAGFLOW] 处理流式响应时出错", e)
        } finally {
            try {
                reader.close()
            } catch (ioe: IOException) {
                Log.e(TAG, "[RAGFLOW] 关闭响应读取器时出错", ioe)
            }
        }
    }

    private suspend fun processJsonData(
        jsonData: String,
        onChunk: suspend (response: RagFlowStreamResponse) -> Unit,
        lineNumber: Int,
        onSuccess: (Boolean) -> Unit
    ) {
        val logContent = if (jsonData.length > 100) jsonData.take(100) + "..." else jsonData
        Log.d(TAG, "[RAGFLOW] 处理JSON数据，行号: $lineNumber, 长度: ${jsonData.length}, 内容: $logContent")
        var success = false
        try {
            val jsonObject = JSONObject(jsonData)
            if (jsonObject.has("code") && jsonObject.has("data")) {
                val dataObj = jsonObject.optJSONObject("data")
                if (dataObj != null && dataObj.has("answer")) {
                    val answerContent = dataObj.getString("answer")
                    val sessionId = dataObj.optString("session_id", "")
                    val streamData = com.sdu.novaglide.data.remote.dto.ragflow.RagFlowStreamData(
                        conversationId = sessionId, content = answerContent, 
                        status = if (dataObj.optString("status", "running") == "finished") "finished" else "running",
                        quotedDocuments = dataObj.optJSONArray("quoted_documents")?.let { arr ->
                            (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let { gson.fromJson(it.toString(), RagFlowDocument::class.java) } }
                        }
                    )
                    onChunk(RagFlowStreamResponse(code = 0, message = "", data = streamData))
                    success = true
                    Log.d(TAG, "[RAGFLOW] 成功解析标准格式JSON，长度: ${answerContent.length}")
                }
            } else if (jsonObject.has("answer")) {
                val answerContent = jsonObject.getString("answer")
                val streamData = com.sdu.novaglide.data.remote.dto.ragflow.RagFlowStreamData(
                    conversationId = "", content = answerContent, status = "finished"
                )
                onChunk(RagFlowStreamResponse(code = 0, message = "", data = streamData))
                success = true
                Log.d(TAG, "[RAGFLOW] 成功解析直接answer JSON，长度: ${answerContent.length}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[RAGFLOW] 解析JSON数据失败: ${e.message}")
            // Fallback to regex if JSON parsing fails for some common direct answer cases
            try {
                val answerPattern = """\"answer\":[ \t]*\"([^\"]+)\""""
                val matcher = Regex(answerPattern).find(jsonData)
                if (matcher != null) {
                    val answerContent = matcher.groupValues[1]
                    if (answerContent.isNotEmpty()) {
                        val streamData = com.sdu.novaglide.data.remote.dto.ragflow.RagFlowStreamData(
                            conversationId = "", content = answerContent, status = "finished"
                        )
                        onChunk(RagFlowStreamResponse(code = 0, message = "", data = streamData))
                        success = true
                        Log.d(TAG, "[RAGFLOW] 通过正则提取回答，长度: ${answerContent.length}")
                    }
                }
            } catch (e2: Exception) {
                Log.e(TAG, "[RAGFLOW] 正则提取answer失败: ${e2.message}")
            }
        }
        onSuccess(success)
        if (!success && lineNumber > 0) { // lineNumber > 0 to avoid logging for whole-response parsing attempt
            Log.w(TAG, "[RAGFLOW] JSON结构中未找到answer字段: ${jsonData.take(100)}...")
        }
    }

} // End of ChatRepositoryImpl class