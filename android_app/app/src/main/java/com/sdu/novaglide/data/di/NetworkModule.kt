package com.sdu.novaglide.data.di

import android.content.Context
import android.util.Log
import com.sdu.novaglide.core.constants.ApiConstants
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.data.remote.api.DeepSeekApiService
import com.sdu.novaglide.data.remote.api.RagFlowApiService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val TAG = "NetworkModule"

/**
 * 网络依赖注入模块
 * 提供Retrofit、OkHttp和API服务实例
 */
object NetworkModule {
    /**
     * 创建OkHttpClient实例
     * @param isDebug 是否为调试模式
     * @return OkHttpClient实例
     */
    fun provideOkHttpClient(isDebug: Boolean = false): OkHttpClient {
        Log.d(TAG, "创建 OkHttpClient，调试模式: $isDebug")

        // 使用自定义SSL配置，解决证书验证问题
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        // 日志拦截器，在调试模式下记录网络请求和响应
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            Log.d(TAG, "创建日志拦截器，级别: $level")
        }
        
        val builder = OkHttpClient.Builder()
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            // 当网络出现问题时自动重试
            .retryOnConnectionFailure(true)
            // 信任所有证书
            .sslSocketFactory(sslContext.socketFactory as SSLSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            // 使用日志拦截器
            .addInterceptor(loggingInterceptor)

        if (isDebug) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
            Log.d(TAG, "已添加HTTP日志拦截器")
        }

        return builder.build()
    }

    /**
     * 创建DeepSeek API服务实例
     * @param client OkHttpClient实例
     * @return DeepSeekApiService实例
     */
    fun provideDeepSeekApiService(client: OkHttpClient): DeepSeekApiService {
        Log.d(TAG, "创建DeepSeek API服务，baseUrl: ${ApiConstants.DEEPSEEK_BASE_URL}")

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConstants.DEEPSEEK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(DeepSeekApiService::class.java)
    }

    /**
     * 创建RAGFlow API服务实例
     * @param client OkHttpClient实例
     * @param apiKeyStore API密钥存储
     * @return RagFlowApiService实例（可能为null，依赖于用户配置的服务器URL）
     */
    suspend fun provideRagFlowApiService(
        client: OkHttpClient,
        apiKeyStore: ApiKeyStore
    ): RagFlowApiService? {
        try {
            Log.d(TAG, "尝试创建RAGFlow API服务")

            // 获取用户配置的RAGFlow服务器URL，若未配置则返回null
            val serverUrl = apiKeyStore.ragFlowServerUrl
                .catch { exception ->
                    Log.e(TAG, "获取RAGFlow服务器URL时出错", exception)
                    if (exception is IOException) {
                        emit(null)
                    } else {
                        throw exception
                    }
                }
                .first()

            if (!serverUrl.isNullOrEmpty()) {
                Log.d(TAG, "创建RAGFlow API服务，serverUrl: $serverUrl")

                // 创建一个宽松的Gson解析器，可以处理非标准JSON和HTML响应
                val gson = GsonBuilder()
                    .setLenient()
                    .create()
                
                // 为 RAGFlow 请求统一添加 Accept 和 Content-Type 头，确保返回 JSON
                val ragFlowClient = client.newBuilder()
                    .addInterceptor { chain ->
                        val originalRequest = chain.request()
                        val newRequest = originalRequest.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")
                            .build()
                        
                        // 记录详细的请求信息
                        Log.d(TAG, "RAGFlow 请求URL: ${newRequest.url}")
                        Log.d(TAG, "RAGFlow 请求方法: ${newRequest.method}")
                        Log.d(TAG, "RAGFlow 请求头: ${newRequest.headers}")
                        
                        val response = chain.proceed(newRequest)
                        
                        // 记录响应信息
                        Log.d(TAG, "RAGFlow 响应状态码: ${response.code}")
                        Log.d(TAG, "RAGFlow 响应头: ${response.headers}")
                        
                        response
                    }
                    // 添加简单的重试拦截器
                    .addInterceptor { chain ->
                        // 最多重试3次
                        var retryCount = 0
                        val maxRetries = 3
                        var lastException: IOException? = null
                        
                        while (retryCount < maxRetries) {
                            try {
                                val request = chain.request()
                                Log.d(TAG, "RAGFlow 请求尝试 ${retryCount + 1}/$maxRetries: ${request.url}")
                                
                                val response = chain.proceed(request)
                                if (response.isSuccessful) {
                                    return@addInterceptor response
                                } else {
                                    Log.w(TAG, "RAGFlow 请求失败，状态码: ${response.code}, 重试中...")
                                    response.close()
                                }
                            } catch (e: IOException) {
                                lastException = e
                                Log.w(TAG, "RAGFlow 请求发生IO异常 (${e.javaClass.simpleName}): ${e.message}")
                            }
                            
                            retryCount++
                            if (retryCount < maxRetries) {
                                val sleepTime = 1000L * retryCount
                                Log.d(TAG, "等待 ${sleepTime}ms 后重试")
                                Thread.sleep(sleepTime)
                            }
                        }
                        
                        throw lastException ?: IOException("请求失败，已达最大重试次数")
                    }
                    .build()
                
                // 确保URL包含api/v1前缀
                var baseUrl = ensureValidBaseUrl(serverUrl)
                if (!baseUrl.contains("/api/v1/")) {
                    baseUrl = baseUrl + "api/v1/"
                    Log.d(TAG, "添加API版本前缀，最终baseUrl: $baseUrl")
                }
                
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(ragFlowClient)
                    .build()

                return retrofit.create(RagFlowApiService::class.java)
            } else {
                Log.w(TAG, "RAGFlow服务器URL为空，无法创建API服务")
            }

            return null
        } catch (e: Exception) {
            Log.e(TAG, "创建RAGFlow API服务失败", e)
            return null
        }
    }

    /**
     * 确保URL格式正确，包含结尾的斜杠
     */
    private fun ensureValidBaseUrl(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }

    /**
     * 创建API密钥存储实例
     * @param context 上下文
     * @return ApiKeyStore实例
     */
    fun provideApiKeyStore(context: Context): ApiKeyStore {
        Log.d(TAG, "创建ApiKeyStore实例")
        return ApiKeyStore(context)
    }
}