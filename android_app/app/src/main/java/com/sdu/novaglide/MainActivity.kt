package com.sdu.novaglide

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.sdu.novaglide.core.constants.ApiConstants
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.core.util.ApiKeyStore.Companion.dataStore
import com.sdu.novaglide.data.di.NetworkModule
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
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    // API密钥存储
    private lateinit var apiKeyStore: ApiKeyStore
    
    // 聊天仓库
    private lateinit var chatRepository: ChatRepository
    
    // 指示是否是首次运行
    private val FIRST_RUN_KEY = booleanPreferencesKey(ApiConstants.PREF_FIRST_RUN)

    // 动态起始路由
    private var determinedStartDestination by mutableStateOf<String?>(null)
    
    // 初始化错误信息
    private var initializationError by mutableStateOf<String?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate 开始初始化应用")

        // 在主线程启动协程进行初始化
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 初始化依赖
                initDependencies()
                
                // 检查首次运行和用户状态
                checkFirstRunAndUserStatus()
            } catch (e: Exception) {
                Log.e(TAG, "onCreate 初始化或状态检查失败", e)
                initializationError = "应用初始化失败: ${e.message}"
            }
        }

        setContent {
            NovaGlideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppStartup(
                        determinedStartDestination = determinedStartDestination,
                        initializationError = initializationError,
                        onRetry = {
                            // 重置状态并重试初始化
                            determinedStartDestination = null
                            initializationError = null
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    initDependencies()
                                    checkFirstRunAndUserStatus()
                                } catch (e: Exception) {
                                    Log.e(TAG, "重试初始化失败", e)
                                    initializationError = "应用初始化失败: ${e.message}"
                                }
                            }
                        },
                        content = { startRoute -> // startRoute 是确定的目的地
                            NovaGlideApp(
                                chatRepository = chatRepository,
                                apiKeyStore = apiKeyStore,
                                initialRoute = startRoute // 传递给 NovaGlideApp
                            )
                        }
                    )
                }
            }
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
     * 检查首次运行应用和用户登录状态
     */
    private suspend fun checkFirstRunAndUserStatus() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始检查首次运行和用户状态")
                
                // 检查首次运行标志
                val isFirstRun = applicationContext.dataStore.data
                    .catch { exception ->
                        Log.e(TAG, "读取DataStore数据失败", exception)
                        emit(androidx.datastore.preferences.core.emptyPreferences())
                    }
                    .map { preferences -> preferences[FIRST_RUN_KEY] ?: true }
                    .first()

                if (isFirstRun) {
                    Log.d(TAG, "首次运行应用，初始化默认设置")
                    apiKeyStore.saveRagFlowServerUrl("http://localhost") // 示例默认值
                    applicationContext.dataStore.edit { preferences ->
                        preferences[FIRST_RUN_KEY] = false
                    }
                    Log.d(TAG, "默认设置初始化成功")
                }

                // 检查当前用户
                val application = applicationContext as NovaGlideApplication
                // getCurrentUser() 现在应该只返回 isLoggedIn = true 的用户
                val currentUser = application.userDao.getCurrentUser() 

                // 在主线程更新起始路由
                withContext(Dispatchers.Main) {
                    // 添加详细日志
                    if (currentUser == null) {
                        Log.d(TAG, "从DAO获取的当前用户 (isLoggedIn=true): null")
                    } else {
                        Log.d(TAG, "从DAO获取的当前用户 (isLoggedIn=true): userId=${currentUser.userId}, username=${currentUser.username}, isLoggedIn=${currentUser.isLoggedIn}")
                    }

                    determinedStartDestination = if (currentUser != null) {
                        Log.d(TAG, "用户已登录 (currentUser != null && currentUser.isLoggedIn == true)，起始页设置为: HOME")
                        AppRoute.HOME
                    } else {
                        Log.d(TAG, "用户未登录 (currentUser == null or currentUser.isLoggedIn == false)，起始页设置为: LOGIN")
                        AppRoute.LOGIN
                    }
                    Log.d(TAG, "最终确定的起始路由 (determinedStartDestination): $determinedStartDestination")
                }
                Log.d(TAG, "首次运行和用户状态检查完成")
            } catch (e: Exception) {
                Log.e(TAG, "检查首次运行或用户状态时出错", e)
                withContext(Dispatchers.Main) { // 切换到主线程更新UI状态
                    initializationError = "初始化设置或检查用户状态失败: ${e.message}"
                }
            }
        }
    }
}

@Composable
fun AppStartup(
    determinedStartDestination: String?,
    initializationError: String?,
    onRetry: () -> Unit,
    content: @Composable (startRoute: String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (initializationError != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = initializationError)
                Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                    Text("重试")
                }
            }
        } else if (determinedStartDestination == null) { // 如果目的地尚未确定，则显示加载中
            CircularProgressIndicator()
            Text("正在加载...", modifier = Modifier.padding(top = 8.dp))
        } else {
            content(determinedStartDestination) // 将确定的路由传递给内容
        }
    }
}

@Composable
fun NovaGlideApp(
    chatRepository: ChatRepository,
    apiKeyStore: ApiKeyStore,
    initialRoute: String // 添加起始目的地参数
) {
    LaunchedEffect(key1 = Unit) {
        Log.d(TAG, "启动导航系统, 起始路由: $initialRoute")
    }
    AppNavigation(
        chatRepository = chatRepository,
        apiKeyStore = apiKeyStore,
        startDestination = initialRoute // 传递确定的起始目的地
    )
}