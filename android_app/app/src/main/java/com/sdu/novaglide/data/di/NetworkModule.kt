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
import java.io.IOException
import java.util.concurrent.TimeUnit

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
        Log.d(TAG, "创建OkHttpClient，调试模式: $isDebug")
        
        val builder = OkHttpClient.Builder()
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
        
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
                
                val retrofit = Retrofit.Builder()
                    .baseUrl(ensureValidBaseUrl(serverUrl))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
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