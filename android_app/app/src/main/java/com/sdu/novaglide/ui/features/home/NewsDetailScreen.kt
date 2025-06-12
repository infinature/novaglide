package com.sdu.novaglide.ui.features.home

import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sdu.novaglide.data.remote.api.ApiClient
import kotlinx.coroutines.Dispatchers
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
    documentId: String,
    onBack: () -> Unit
) {
    var contentState by remember { mutableStateOf<ContentState>(ContentState.Loading) }

    LaunchedEffect(documentId) {
        Log.d(TAG, "LaunchedEffect started for documentId: $documentId")
        contentState = ContentState.Loading
        try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.instance.getDatasetDocumentDetail(
                    bearerToken = "Bearer ragflow-ExZjM1NmYyNDc3NDExZjBhMTIxZmVjY2",
                    datasetId = "bfd51b5e475d11f0850dfecceaed7a8e",
                    docId = documentId
                )
            }
            Log.d(TAG, "API Response received: code=${response.code()}, message=${response.message()}")

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val content = responseBody.string()
                    Log.d(TAG, "Response Body as String: $content")
                    // Since we can't get title from this endpoint, we use a default one.
                    val title = "资讯详情" 
                    
                    contentState = ContentState.Success(content, title)
                    Log.d(TAG, "State set to Success")
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                    val htmlContent = remember(state.content) {
                        // Escape the content for JavaScript
                        val escapedContent = state.content
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
                            <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
                            <style>
                                body { margin: 16px; font-family: sans-serif; word-wrap: break-word; }
                                code { background-color: #f0f0f0; padding: 2px 4px; border-radius: 4px; }
                                pre { background-color: #f0f0f0; padding: 16px; border-radius: 8px; overflow-x: auto; }
                                img { max-width: 100%; height: auto; }
                            </style>
                        </head>
                        <body>
                            <div id="content"></div>
                            <script>
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