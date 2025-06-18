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

class NewsViewModel(private val context: Context) : ViewModel() {
    private val _newsList = MutableStateFlow<List<NewsArticle>>(emptyList())
    val newsList: StateFlow<List<NewsArticle>> = _newsList

    private var allNews: List<NewsArticle> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    // 添加刷新状态
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    // 添加刷新时间戳，用于触发推荐算法重新计算
    private val _refreshTimestamp = MutableStateFlow(0L)
    val refreshTimestamp: StateFlow<Long> = _refreshTimestamp

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
        val serverUrl = apiKeyStore.ragFlowServerUrl.first() ?: "https://frp-off.com:65008/"
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
        // 更新搜索查询文本
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
                
                val api = createApiService()
                val response = api.getDatasetDocuments(
                    bearerToken = "Bearer $apiKey",
                    datasetId = datasetId,
                    page = 1,
                    pageSize = 100,
                    query = null // Always fetch all news
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("NewsViewModel", "RAGFLOW完整返回内容: $body")
                    val data = body?.get("data")
                    // Log.d("NewsViewModel", "data字段内容: $data")
                    val docs = (data as? Map<*, *>)?.get("docs") as? List<Map<String, Any>>
                    // Log.d("NewsViewModel", "docs内容: $docs")
                    val news = docs?.mapNotNull { doc ->
                        try {
                            // Log the entire doc object to inspect its structure
                            Log.d("NewsViewModel", "单个文档的完整结构: $doc")
                            val meta = doc["meta_fields"] as? Map<*, *>
                            NewsArticle(
                                id = doc["id"]?.toString() ?: "", // Let's try getting id from the top-level doc
                                title = meta?.get("title")?.toString() ?: "",
                                summary = meta?.get("summary")?.toString() ?: "",
                                source = meta?.get("source")?.toString() ?: "",
                                publishTime = meta?.get("publishTime")?.toString()?.toLongOrNull() ?: 0L,
                                category = meta?.get("category")?.toString() ?: ""
                            )
                        } catch (e: Exception) {
                            println("单条资讯解析异常: ${e.message}")
                            null
                        }
                    } ?: emptyList()
                    // Log.d("NewsViewModel", "最终newsList: $news")
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
            try {
                // 获取保存的API配置
                val apiKey = apiKeyStore.ragFlowApiKey.first()
                val datasetId = defaultDatasetId // 可以后续从设置中获取
                
                if (apiKey.isNullOrEmpty()) {
                    Log.e("NewsViewModel", "刷新失败：RAGFlow API密钥为空，请先在设置中配置")
                    return@launch
                }
                
                val api = createApiService()
                val response = api.getDatasetDocuments(
                    bearerToken = "Bearer $apiKey",
                    datasetId = datasetId,
                    page = 1,
                    pageSize = 100,
                    query = null
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("NewsViewModel", "刷新 - RAGFLOW完整返回内容: $body")
                    val data = body?.get("data")
                    val docs = (data as? Map<*, *>)?.get("docs") as? List<Map<String, Any>>
                    val news = docs?.mapNotNull { doc ->
                        try {
                            Log.d("NewsViewModel", "刷新 - 单个文档的完整结构: $doc")
                            val meta = doc["meta_fields"] as? Map<*, *>
                            NewsArticle(
                                id = doc["id"]?.toString() ?: "",
                                title = meta?.get("title")?.toString() ?: "",
                                summary = meta?.get("summary")?.toString() ?: "",
                                source = meta?.get("source")?.toString() ?: "",
                                publishTime = meta?.get("publishTime")?.toString()?.toLongOrNull() ?: 0L,
                                category = meta?.get("category")?.toString() ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e("NewsViewModel", "刷新 - 单条资讯解析异常: ${e.message}")
                            null
                        }
                    } ?: emptyList()
                    Log.d("NewsViewModel", "刷新 - 最终newsList: $news")
                    allNews = news
                    _newsList.value = news
                    // 更新刷新时间戳，触发推荐算法重新计算
                    _refreshTimestamp.value = System.currentTimeMillis()
                } else {
                    Log.e("NewsViewModel", "刷新 - RAGFLOW接口失败: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "刷新 - RAGFLOW请求异常: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updateSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }
} 