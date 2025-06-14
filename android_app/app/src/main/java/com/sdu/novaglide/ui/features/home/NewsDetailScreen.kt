package com.sdu.novaglide.ui.features.home

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
import androidx.compose.ui.graphics.Color // 确保导入
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sdu.novaglide.data.remote.api.ApiClient
import com.sdu.novaglide.ui.features.profile.FavoriteArticleViewModel // 导入
import com.sdu.novaglide.ui.features.profile.UserInfoState // 导入
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel // 导入
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch // 确保导入
import kotlinx.coroutines.withContext

private const val TAG = "NewsDetailScreen"

sealed class ContentState {
    object Loading : ContentState()
    data class Success(val content: String, val title: String) : ContentState()
    data class Error(val message: String) : ContentState()
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
                ApiClient.instance.getDatasetDocumentDetail(
                    bearerToken = "Bearer ragflow-ExZjM1NmYyNDc3NDExZjBhMTIxZmVjY2",
                    datasetId = "bfd51b5e475d11f0850dfecceaed7a8e",
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
                    Text(text = title)
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
                        // Pre-process the content to fix list formatting.
                        // This specifically targets patterns like **1.** and converts them to "1. ",
                        // making it a standard Markdown ordered list.
                        state.content.replace(Regex("""\*\*(\d+)\.\*\* ?""")) { matchResult ->
                            "${matchResult.groupValues[1]}. "
                        }
                    }
                    val htmlContent = remember(processedContent) {
                        // Escape the content for JavaScript
                        val escapedContent = processedContent
                            .replace("\\", "\\\\")
                            .replace("'", "\\'")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "")

                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            
                            <!-- Markdown Style -->
                            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.8.1/github-markdown-light.min.css">
                            
                            <!-- Syntax Highlighting Style -->
                            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css">
                            
                            <style>
                                .markdown-body {
                                    box-sizing: border-box;
                                    min-width: 200px;
                                    max-width: 980px;
                                    margin: 0 auto;
                                    padding: 45px;
                                }

                                @media (max-width: 767px) {
                                    .markdown-body {
                                        padding: 15px;
                                    }
                                }
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
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}