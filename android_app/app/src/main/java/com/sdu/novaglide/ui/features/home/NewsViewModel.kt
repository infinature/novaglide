package com.sdu.novaglide.ui.features.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.data.remote.api.RagFlowApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

class NewsViewModel(private val context: Context) : ViewModel() {
    private val _newsList = MutableStateFlow<List<NewsArticle>>(emptyList())
    val newsList = _newsList.asStateFlow()

    private var allNews: List<NewsArticle> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    // 添加刷新状态
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    
    // 添加刷新时间戳，用于触发推荐算法重新计算
    private val _refreshTimestamp = MutableStateFlow(0L)
    val refreshTimestamp = _refreshTimestamp.asStateFlow()

    // 使用ApiKeyStore来获取保存的API配置
    private val apiKeyStore = ApiKeyStore(context)
    
    // 默认的数据集ID（用户可以在设置中修改这个值）
    private val defaultDatasetId = "526578e449aa11f08a938a6c0dbc5424"

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .build()
    }

    // 动态创建API服务
    private suspend fun createApiService(): RagFlowApiService {
        val serverUrl = apiKeyStore.ragFlowServerUrl.first() ?: "https://frp-rug.com:65008/"
        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getUnsafeOkHttpClient())
        .build()
        return retrofit.create(RagFlowApiService::class.java)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _newsList.value = allNews
        }
    }

    fun searchNews(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            _newsList.value = allNews
            return
        }
        val filteredList = allNews.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.summary.contains(query, ignoreCase = true)
        }
        _newsList.value = filteredList
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _newsList.value = allNews
    }

    fun fetchNews() {
        viewModelScope.launch {
            try {
                // 获取保存的API配置
                val apiKey = apiKeyStore.ragFlowApiKey.first()
                val datasetId = defaultDatasetId // 可以后续从设置中获取
                
                if (apiKey.isNullOrEmpty()) {
                    Log.e("NewsViewModel", "RAGFlow API密钥为空，请先在设置中配置")
                    return@launch
                }
                
                Log.d("NewsViewModel", "开始获取数据，API Key: ${apiKey.take(10)}..., Dataset ID: $datasetId")
                
                val api = createApiService()
                val response = api.getDatasetDocuments(
                    bearerToken = "Bearer $apiKey",
                    datasetId = datasetId,
                    page = 1,
                    pageSize = 1000, // 增加页面大小以获取更多文档
                    query = null // Always fetch all news
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("NewsViewModel", "RAGFLOW完整返回内容: $body")
                    val data = body?.get("data")
                    Log.d("NewsViewModel", "data字段内容: $data")
                    val docs = (data as? Map<*, *>)?.get("docs") as? List<Map<String, Any>>
                    Log.d("NewsViewModel", "docs内容: $docs")
                    Log.d("NewsViewModel", "docs数量: ${docs?.size}")
                    
                    val news = docs?.mapNotNull { doc ->
                        try {
                            // Log the entire doc object to inspect its structure
                            Log.d("NewsViewModel", "单个文档的完整结构: $doc")
                            
                            // 尝试不同的字段名称获取元数据
                            val meta = doc["meta_fields"] as? Map<*, *> ?: doc["metadata"] as? Map<*, *> ?: doc
                            Log.d("NewsViewModel", "meta数据: $meta")
                            
                            // 尝试从不同位置获取数据
                            val docId = doc["id"]?.toString() ?: doc["doc_id"]?.toString() ?: ""
                            val title = meta["title"]?.toString() ?: doc["title"]?.toString() ?: doc["name"]?.toString() ?: ""
                            val summary = meta["summary"]?.toString() ?: meta["content"]?.toString() ?: doc["summary"]?.toString() ?: doc["content"]?.toString() ?: ""
                            val source = meta["source"]?.toString() ?: doc["source"]?.toString() ?: "未知来源"
                            val category = meta["category"]?.toString() ?: doc["category"]?.toString() ?: doc["type"]?.toString() ?: ""
                            
                            // 尝试解析时间戳
                            val publishTime = try {
                                meta["publishTime"]?.toString()?.toLongOrNull() 
                                    ?: meta["publish_time"]?.toString()?.toLongOrNull()
                                    ?: meta["created_time"]?.toString()?.toLongOrNull()
                                    ?: doc["publishTime"]?.toString()?.toLongOrNull()
                                    ?: doc["publish_time"]?.toString()?.toLongOrNull()
                                    ?: doc["created_time"]?.toString()?.toLongOrNull()
                                    ?: System.currentTimeMillis() / 1000 // 默认使用当前时间
                            } catch (e: Exception) {
                                System.currentTimeMillis() / 1000
                            }
                            
                            Log.d("NewsViewModel", "解析结果 - ID: '$docId', 标题: '$title', 摘要: '${summary.take(50)}...', 来源: '$source', 类别: '$category', 时间: $publishTime")
                            
                            // 如果标题为空，跳过这条记录
                            if (title.isBlank()) {
                                Log.w("NewsViewModel", "跳过空标题文档: $doc")
                                return@mapNotNull null
                            }
                            
                            NewsArticle(
                                id = docId,
                                title = title,
                                summary = summary.ifBlank { "暂无摘要" },
                                source = source,
                                publishTime = publishTime,
                                category = category.ifBlank { "其他" }
                            )
                        } catch (e: Exception) {
                            Log.e("NewsViewModel", "单条资讯解析异常: ${e.message}", e)
                            null
                        }
                    } ?: emptyList()
                    
                    // 添加类别统计信息
                    val categoryStats = news.groupBy { it.category }.mapValues { it.value.size }
                    Log.d("NewsViewModel", "所有类别统计: $categoryStats")
                    Log.d("NewsViewModel", "总文章数: ${news.size}")
                    Log.d("NewsViewModel", "有效文章数: ${news.count { it.title.isNotBlank() }}")
                    
                    allNews = news
                    _newsList.value = news
                } else {
                    println("RAGFLOW接口失败: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                println("RAGFLOW请求异常: ${e.message}")
            }
        }
    }

    // 添加刷新方法
    fun refreshNews() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // 在这里添加延迟，模拟网络请求
            kotlinx.coroutines.delay(1000)
            // 重新获取数据
            fetchNews()
            // 更新刷新时间戳，触发 recomposition
            _refreshTimestamp.value = System.currentTimeMillis()
            _isRefreshing.value = false
        }
    }

    fun updateSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * 获取单个文档的详细内容
     */
    suspend fun getDocumentContent(docId: String): String? {
        return try {
            val apiKey = apiKeyStore.ragFlowApiKey.first()
            val datasetId = defaultDatasetId
            
            if (apiKey.isNullOrEmpty()) {
                Log.e("NewsViewModel", "RAGFlow API密钥为空")
                return null
            }
            
            val api = createApiService()
            val response = api.getDatasetDocumentDetail(
                bearerToken = "Bearer $apiKey",
                datasetId = datasetId,
                docId = docId
            )
            
            if (response.isSuccessful) {
                response.body()?.string()
            } else {
                Log.e("NewsViewModel", "获取文档详情失败: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("NewsViewModel", "获取文档详情异常: ${e.message}", e)
            null
        }
    }
} 