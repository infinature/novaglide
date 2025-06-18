package com.sdu.novaglide.ui.features.home

import android.content.Context
import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite // 确保导入
import androidx.compose.material.icons.filled.FavoriteBorder // 确保导入
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color // 确保导入
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.data.remote.api.RagFlowApiService
import com.sdu.novaglide.ui.features.profile.FavoriteArticleViewModel // 导入
import com.sdu.novaglide.ui.features.profile.UserInfoState // 导入
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel // 导入
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch // 确保导入
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val TAG = "NewsDetailScreen"

sealed class ContentState {
    object Loading : ContentState()
    data class Success(val content: String, val title: String) : ContentState()
    data class Error(val message: String) : ContentState()
}

// 动态创建API服务的函数
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

private suspend fun createApiService(context: Context): RagFlowApiService {
    val apiKeyStore = ApiKeyStore(context)
    val serverUrl = apiKeyStore.ragFlowServerUrl.first() ?: "https://frp-off.com:65008/"
    val retrofit = Retrofit.Builder()
        .baseUrl(serverUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getUnsafeOkHttpClient())
        .build()
    return retrofit.create(RagFlowApiService::class.java)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: String?, // 修改参数名为 newsId，并使其可空以处理潜在的 null 情况
    onNavigateBack: () -> Unit, // 修改参数名为 onNavigateBack
    userInfoViewModel: UserInfoViewModel, // 添加 userInfoViewModel
    favoriteArticleViewModel: FavoriteArticleViewModel, // 添加 favoriteArticleViewModel
    newsViewModel: NewsViewModel // 添加 newsViewModel (虽然当前未使用，但保持一致性)
) {
    var contentState by remember { mutableStateOf<ContentState>(ContentState.Loading) }
    val snackbarHostState = remember { SnackbarHostState() } // 用于显示提示信息
    val coroutineScope = rememberCoroutineScope()
    val currentUserState by userInfoViewModel.userInfoState.collectAsState()
    val isFavorite by favoriteArticleViewModel.isFavorite.collectAsState()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // 使用传入的 newsId 作为 documentId
    val documentIdToFetch = newsId

    LaunchedEffect(documentIdToFetch) {
        if (documentIdToFetch == null) {
            Log.w(TAG, "newsId is null, cannot fetch details.")
            contentState = ContentState.Error("资讯ID无效")
            return@LaunchedEffect
        }
        Log.d(TAG, "LaunchedEffect started for documentId: $documentIdToFetch")
        contentState = ContentState.Loading
        try {
            val response = withContext(Dispatchers.IO) {
                // 获取保存的API配置
                val apiKeyStore = ApiKeyStore(context)
                val apiKey = apiKeyStore.ragFlowApiKey.first()
                val datasetId = "526578e449aa11f08a938a6c0dbc5424" // 使用正确的数据集ID
                
                if (apiKey.isNullOrEmpty()) {
                    Log.e(TAG, "RAGFlow API密钥为空，请先在设置中配置")
                    throw Exception("API密钥未配置")
                }
                
                val api = createApiService(context)
                api.getDatasetDocumentDetail(
                    bearerToken = "Bearer $apiKey",
                    datasetId = datasetId,
                    docId = documentIdToFetch // 使用 documentIdToFetch
                )
            }
            Log.d(TAG, "API Response received: code=${response.code()}, message=${response.message()}")

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val content = responseBody.string()
                    Log.d(TAG, "Response Body as String: $content")
                    // 尝试从 newsViewModel 获取标题，如果找不到则使用默认标题
                    val articleTitle = newsViewModel.newsList.value.find { it.id == documentIdToFetch }?.title ?: "资讯详情"
                    
                    contentState = ContentState.Success(content, articleTitle)
                    Log.d(TAG, "State set to Success with title: $articleTitle")

                    // 获取到内容后，检查是否已收藏
                    if (currentUserState is UserInfoState.Success) {
                        val userId = (currentUserState as UserInfoState.Success).userInfo.userId
                        favoriteArticleViewModel.checkIfFavorite(userId, documentIdToFetch)
                    }

                } else {
                    contentState = ContentState.Error("响应体为空")
                    Log.d(TAG, "State set to Error: Response body is null")
                }
            } else {
                contentState = ContentState.Error("加载失败: ${response.code()} ${response.message()}")
                Log.d(TAG, "State set to Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            contentState = ContentState.Error("请求异常: ${e.message}")
            Log.d(TAG, "State set to Error due to exception")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // 添加 SnackbarHost
        topBar = {
            TopAppBar(
                title = {
                    val title = when (val state = contentState) {
                        is ContentState.Success -> state.title
                        else -> "资讯详情"
                    }
                    Text(
                        text = title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // 使用 onNavigateBack
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = { // 添加收藏按钮
                    if (documentIdToFetch != null && contentState is ContentState.Success) {
                        val currentTitle = (contentState as ContentState.Success).title
                        IconButton(onClick = {
                            if (currentUserState is UserInfoState.Success) {
                                val userId = (currentUserState as UserInfoState.Success).userInfo.userId
                                if (isFavorite) {
                                    favoriteArticleViewModel.removeFavorite(userId, documentIdToFetch)
                                } else {
                                    favoriteArticleViewModel.addFavorite(userId, documentIdToFetch, currentTitle)
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("请先登录")
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "收藏",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = contentState) {
                is ContentState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ContentState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ContentState.Success -> {
                    val processedContent = remember(state.content) {
                        var content = state.content
                        
                        // 清理可能的HTML标签和样式
                        content = content
                            .replace(Regex("""<style[^>]*>.*?</style>""", RegexOption.DOT_MATCHES_ALL), "")
                            .replace(Regex("""style\s*=\s*["'][^"']*["']""", RegexOption.IGNORE_CASE), "")
                            .replace(Regex("""<[^>]*background[^>]*>""", RegexOption.IGNORE_CASE), "")
                        
                        // Pre-process the content to fix list formatting.
                        // This specifically targets patterns like **1.** and converts them to "1. ",
                        // making it a standard Markdown ordered list.
                        content.replace(Regex("""\*\*(\d+)\.\*\* ?""")) { matchResult ->
                            "${matchResult.groupValues[1]}. "
                        }
                    }
                    val htmlContent = remember(processedContent, isDarkTheme) {
                        // Escape the content for JavaScript
                        val escapedContent = processedContent
                            .replace("\\", "\\\\")
                            .replace("'", "\\'")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "")

                        // 根据主题选择CSS
                        val markdownCss = if (isDarkTheme) {
                            "https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.8.1/github-markdown-dark.min.css"
                        } else {
                            "https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.8.1/github-markdown-light.min.css"
                        }
                        
                        val highlightCss = if (isDarkTheme) {
                            "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github-dark.min.css"
                        } else {
                            "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css"
                        }

                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            
                            <!-- Markdown Style -->
                            <link rel="stylesheet" href="$markdownCss">
                            
                            <!-- Syntax Highlighting Style -->
                            <link rel="stylesheet" href="$highlightCss">
                            
                            <style>
                                body {
                                    margin: 0;
                                    padding: 0;
                                    background-color: ${if (isDarkTheme) "#0d1117" else "#ffffff"} !important;
                                }
                                .markdown-body {
                                    box-sizing: border-box;
                                    min-width: 200px;
                                    max-width: 980px;
                                    margin: 0 auto;
                                    padding: 45px;
                                    background-color: ${if (isDarkTheme) "#0d1117" else "#ffffff"} !important;
                                    color: ${if (isDarkTheme) "#e6edf3" else "#24292f"} !important;
                                }

                                @media (max-width: 767px) {
                                    .markdown-body {
                                        padding: 15px;
                                    }
                                }
                                
                                ${if (isDarkTheme) """
                                /* 强制深色模式样式 */
                                * {
                                    background-color: #0d1117 !important;
                                    color: #e6edf3 !important;
                                }
                                
                                /* 特殊元素处理 */
                                pre, code {
                                    background-color: #21262d !important;
                                    color: #e6edf3 !important;
                                    border: 1px solid #30363d !important;
                                }
                                
                                blockquote {
                                    background-color: #161b22 !important;
                                    border-left: 4px solid #30363d !important;
                                    color: #e6edf3 !important;
                                }
                                
                                table, th, td {
                                    background-color: #0d1117 !important;
                                    color: #e6edf3 !important;
                                    border-color: #30363d !important;
                                }
                                
                                a {
                                    color: #58a6ff !important;
                                }
                                
                                h1, h2, h3, h4, h5, h6 {
                                    color: #f0f6fc !important;
                                }
                                """ else ""}
                            </style>
                        </head>
                        <body>
                            <div id="content" class="markdown-body"></div>

                            <!-- Markdown Parser -->
                            <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
                            
                            <!-- Syntax Highlighter -->
                            <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>

                            <script>
                                // Configure marked to use highlight.js
                                marked.setOptions({
                                  highlight: function(code, lang) {
                                    const language = hljs.getLanguage(lang) ? lang : 'plaintext';
                                    return hljs.highlight(code, { language }).value;
                                  }
                                });

                                // Parse and render the content
                                document.getElementById('content').innerHTML = marked.parse('$escapedContent');
                                
                                // 强制设置深色模式样式（如果需要）
                                ${if (isDarkTheme) """
                                function forceDarkMode() {
                                    document.body.style.backgroundColor = '#0d1117';
                                    document.body.style.color = '#e6edf3';
                                    var content = document.getElementById('content');
                                    if (content) {
                                        content.style.backgroundColor = '#0d1117';
                                        content.style.color = '#e6edf3';
                                    }
                                    // 强制设置所有文本元素
                                    var elements = document.querySelectorAll('*');
                                    elements.forEach(function(el) {
                                        if (el.style) {
                                            el.style.backgroundColor = el.style.backgroundColor === 'white' || el.style.backgroundColor === '#ffffff' || el.style.backgroundColor === 'rgb(255, 255, 255)' ? '#0d1117' : el.style.backgroundColor || '';
                                            if (el.tagName === 'P' || el.tagName === 'H1' || el.tagName === 'H2' || el.tagName === 'H3' || el.tagName === 'H4' || el.tagName === 'H5' || el.tagName === 'H6' || el.tagName === 'SPAN' || el.tagName === 'DIV') {
                                                el.style.color = '#e6edf3';
                                            }
                                        }
                                    });
                                }
                                
                                // 页面加载完成后执行
                                if (document.readyState === 'loading') {
                                    document.addEventListener('DOMContentLoaded', forceDarkMode);
                                } else {
                                    forceDarkMode();
                                }
                                
                                // 延迟执行确保所有内容都已渲染
                                setTimeout(forceDarkMode, 100);
                                setTimeout(forceDarkMode, 500);
                                """ else ""}
                            </script>
                        </body>
                        </html>
                        """.trimIndent()
                    }

                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                
                                // 配置深色模式支持
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                    @Suppress("DEPRECATION")
                                    settings.forceDark = if (isDarkTheme) {
                                        android.webkit.WebSettings.FORCE_DARK_ON
                                    } else {
                                        android.webkit.WebSettings.FORCE_DARK_OFF
                                    }
                                }
                                
                                // 设置背景色
                                setBackgroundColor(if (isDarkTheme) 0xFF0d1117.toInt() else 0xFFffffff.toInt())
                            }
                        },
                        update = { webView ->
                            // 设置背景色（在update中也设置，确保主题变化时生效）
                            webView.setBackgroundColor(if (isDarkTheme) 0xFF0d1117.toInt() else 0xFFffffff.toInt())
                            
                            // 更新深色模式设置
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                @Suppress("DEPRECATION")
                                webView.settings.forceDark = if (isDarkTheme) {
                                    android.webkit.WebSettings.FORCE_DARK_ON
                                } else {
                                    android.webkit.WebSettings.FORCE_DARK_OFF
                                }
                            }
                            
                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}