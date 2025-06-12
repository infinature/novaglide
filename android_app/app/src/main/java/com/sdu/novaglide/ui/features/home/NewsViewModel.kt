package com.sdu.novaglide.ui.features.home

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

    fun fetchNews() {
        viewModelScope.launch {
            try {
                val response = api.getDatasetDocuments(
                    bearerToken = "Bearer $apiKey",
                    datasetId = datasetId,
                    page = 1,
                    pageSize = 100
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    println("RAGFLOW返回内容: $body")
                    val data = body?.get("data")
                    println("data字段内容: $data")
                    val docs = (data as? Map<*, *>)?.get("docs") as? List<Map<String, Any>>
                    println("docs内容: $docs")
                    val news = docs?.mapNotNull { doc ->
                        try {
                            val meta = doc["meta_fields"] as? Map<*, *>
                            NewsArticle(
                                id = meta?.get("id")?.toString() ?: "",
                                title = meta?.get("title")?.toString() ?: "",
                                summary = meta?.get("summary")?.toString() ?: "",
                                content = meta?.get("content")?.toString() ?: "",
                                source = meta?.get("source")?.toString() ?: "",
                                publishTime = meta?.get("publishTime")?.toString()?.toLongOrNull() ?: 0L,
                                category = meta?.get("category")?.toString() ?: ""
                            )
                        } catch (e: Exception) {
                            println("单条资讯解析异常: ${e.message}")
                            null
                        }
                    } ?: emptyList()
                    println("最终newsList: $news")
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