package com.sdu.novaglide.ui.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.data.remote.api.RagFlowApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

class NewsViewModel : ViewModel() {
    private val _newsList = MutableStateFlow<List<NewsArticle>>(emptyList())
    val newsList: StateFlow<List<NewsArticle>> = _newsList

    private var allNews: List<NewsArticle> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val apiKey = "ragflow-ExZjM1NmYyNDc3NDExZjBhMTIxZmVjY2"
    private val datasetId = "bfd51b5e475d11f0850dfecceaed7a8e"

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

    // TODO: 替换为你的ragflow服务实际地址
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://frp-off.com:65008/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(getUnsafeOkHttpClient())
        .build()
    private val api = retrofit.create(RagFlowApiService::class.java)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _newsList.value = allNews
        }
    }

    fun searchNews(query: String) {
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

    fun fetchNews() {
        viewModelScope.launch {
            try {
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
} 