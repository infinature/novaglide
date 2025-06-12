package com.sdu.novaglide

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.sdu.novaglide.core.constants.ApiConstants
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.core.util.ApiKeyStore.Companion.dataStore
import com.sdu.novaglide.data.di.NetworkModule
import com.sdu.novaglide.data.remote.api.DeepSeekApiService
import com.sdu.novaglide.data.remote.api.RagFlowApiService
import com.sdu.novaglide.data.repository.ChatRepository
import com.sdu.novaglide.data.repository.ChatRepositoryImpl
import com.sdu.novaglide.ui.navigation.AppNavigation
import com.sdu.novaglide.ui.navigation.AppRoute
import com.sdu.novaglide.ui.theme.NovaGlideTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    // API密钥存储
    private lateinit var apiKeyStore: ApiKeyStore
    
    // 聊天仓库
    private lateinit var chatRepository: ChatRepository
    
    // 指示是否是首次运行
    private val FIRST_RUN_KEY = booleanPreferencesKey(ApiConstants.PREF_FIRST_RUN)
    
    // 应用初始化状态
    private var initSuccess by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate 开始初始化应用")
        
        try {
            // 初始化依赖
            initDependencies()
            
            // 检查是否首次运行并设置默认值
            checkFirstRun()
            
            setContent {
                NovaGlideTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppStartup(
                            initSuccess = initSuccess,
                            errorMessage = errorMessage,
                            onRetry = { recreate() },
                            content = {
                                NovaGlideApp(
                                    chatRepository = chatRepository,
                                    apiKeyStore = apiKeyStore
                                )
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate 初始化失败", e)
            errorMessage = "应用初始化失败: ${e.message}"
        }
    }
    
    /**
     * 初始化依赖
     */
    private fun initDependencies() {
        try {
            Log.d(TAG, "开始初始化API密钥存储和网络依赖")
            
            // 初始化API密钥存储
            apiKeyStore = ApiKeyStore(applicationContext)
            
            // 使用NetworkModule创建依赖
            val okHttpClient = NetworkModule.provideOkHttpClient(isDebug = true)
            val deepSeekApiService = NetworkModule.provideDeepSeekApiService(okHttpClient)
            
            // 暂时使用直接创建的RagFlow API服务，后续可以用协程获取用户配置的服务
            val ragFlowApiService = Retrofit.Builder()
                .baseUrl(ApiConstants.RAGFLOW_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RagFlowApiService::class.java)
            
            // 创建聊天仓库
            chatRepository = ChatRepositoryImpl(
                deepSeekApiService = deepSeekApiService,
                ragFlowApiService = ragFlowApiService,
                apiKeyStore = apiKeyStore
            )
            
            Log.d(TAG, "依赖初始化成功")
        } catch (e: Exception) {
            Log.e(TAG, "初始化依赖失败", e)
            throw e
        }
    }
    
    /**
     * 检查是否首次运行应用，如果是则初始化默认设置
     * 使用更安全的方式初始化API密钥（不在源代码中存储）
     */
    private fun checkFirstRun() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "开始检查首次运行状态")
                
                val isFirstRun = try {
                    applicationContext.dataStore.data
                        .catch { exception -> 
                            Log.e(TAG, "读取DataStore数据失败", exception)
                            emit(androidx.datastore.preferences.core.emptyPreferences()) 
                        }
                        .map { preferences -> 
                            preferences[FIRST_RUN_KEY] ?: true 
                        }
                        .first()
                } catch (e: Exception) {
                    Log.e(TAG, "获取首次运行状态失败", e)
                    true // 默认为首次运行
                }
                
                if (isFirstRun) {
                    Log.d(TAG, "首次运行应用，初始化默认设置")
                    
                    try {
                        // 由于不应在代码中明文存储密钥，我们只设置服务器URL
                        // 用户需要在设置界面手动输入API密钥
                        apiKeyStore.saveRagFlowServerUrl("http://localhost")
                        
                        // 将首次运行标志设置为false
                        applicationContext.dataStore.edit { preferences ->
                            preferences[FIRST_RUN_KEY] = false
                        }
                        
                        Log.d(TAG, "默认设置初始化成功")
                    } catch (e: Exception) {
                        Log.e(TAG, "保存默认设置失败", e)
                    }
                }
                
                // 标记初始化成功
                initSuccess = true
            } catch (e: Exception) {
                Log.e(TAG, "检查首次运行时出错", e)
                errorMessage = "初始化设置失败: ${e.message}"
            }
        }
    }
}

@Composable
fun AppStartup(
    initSuccess: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (errorMessage != null) {
            Text(text = errorMessage)
        } else if (!initSuccess) {
            CircularProgressIndicator()
        } else {
            content()
        }
    }
}

@Composable
fun NovaGlideApp(
    chatRepository: ChatRepository,
    apiKeyStore: ApiKeyStore
) {
    // 使用新的导航系统
    LaunchedEffect(key1 = Unit) {
        Log.d(TAG, "启动导航系统")
    }
    
    AppNavigation(
        chatRepository = chatRepository,
        apiKeyStore = apiKeyStore
    )
}