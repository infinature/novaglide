package com.sdu.novaglide.ui.features.qna

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.sdu.novaglide.ui.theme.scaledSp

@Composable
fun MarkdownText(
    content: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val htmlContent = remember(content, isDarkTheme, textColor) {
        // 清理内容
        var processedContent = content
            .replace(Regex("""<style[^>]*>.*?</style>""", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("""style\s*=\s*["'][^"']*["']""", RegexOption.IGNORE_CASE), "")
        
        // 转义内容用于JavaScript
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

        // 获取文字颜色的RGB值
        val colorHex = String.format("#%06X", (0xFFFFFF and textColor.value.toInt()))

        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link rel="stylesheet" href="$markdownCss">
            <link rel="stylesheet" href="$highlightCss">
            <style>
                body {
                    margin: 0;
                    padding: 8px;
                    background-color: transparent !important;
                    font-size: 14px;
                }
                .markdown-body {
                    box-sizing: border-box;
                    background-color: transparent !important;
                    color: $colorHex !important;
                    font-size: 14px;
                }
                
                ${if (isDarkTheme) """
                /* 强制深色模式样式 */
                * {
                    background-color: transparent !important;
                    color: $colorHex !important;
                }
                
                pre, code {
                    background-color: rgba(33, 38, 45, 0.5) !important;
                    color: $colorHex !important;
                    border: 1px solid rgba(48, 54, 61, 0.5) !important;
                }
                
                blockquote {
                    background-color: rgba(22, 27, 34, 0.5) !important;
                    border-left: 4px solid rgba(48, 54, 61, 0.5) !important;
                    color: $colorHex !important;
                }
                
                table, th, td {
                    background-color: transparent !important;
                    color: $colorHex !important;
                    border-color: rgba(48, 54, 61, 0.5) !important;
                }
                """ else "/* 浅色模式 */"}
            </style>
        </head>
        <body>
            <div id="content" class="markdown-body"></div>
            <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
            <script>
                marked.setOptions({
                  highlight: function(code, lang) {
                    const language = hljs.getLanguage(lang) ? lang : 'plaintext';
                    return hljs.highlight(code, { language }).value;
                  }
                });
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
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                // 配置深色模式支持
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    @Suppress("DEPRECATION")
                    settings.forceDark = if (isDarkTheme) {
                        android.webkit.WebSettings.FORCE_DARK_ON
                    } else {
                        android.webkit.WebSettings.FORCE_DARK_OFF
                    }
                }
            }
        },
        update = { webView ->
            webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            
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
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QnaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: QnaViewModel
) {
    var userInput by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isApiConfigured by viewModel.isApiConfigured.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 当新消息添加时，滚动到底部
    var uiUpdateTrigger by remember { mutableStateOf(0) }
    
    LaunchedEffect(messages.size, uiUpdateTrigger) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
                // 强制重组以确保显示最新内容
                delay(100)
                uiUpdateTrigger = uiUpdateTrigger + 1
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "智能问答",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp.scaledSp()
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // API未配置提示
            /* 
            if (!isApiConfigured) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = errorMessage ?: "API未配置，请检查设置",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            */
            
            // 聊天消息列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState
            ) {
                items(messages) { message ->
                    DomainMessageItem(message = message)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // 模型选择器
            var selectedModel by remember { mutableStateOf("DeepSeek") } // 默认使用DeepSeek
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // DeepSeek 按钮
                Button(
                    onClick = { selectedModel = "DeepSeek" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedModel == "DeepSeek") 
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedModel == "DeepSeek") 
                            MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("DeepSeek", fontSize = 14.sp.scaledSp())
                }
                
                // RAGFlow 按钮
                Button(
                    onClick = { selectedModel = "RAGFlow" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedModel == "RAGFlow") 
                            MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedModel == "RAGFlow") 
                            MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("RAGFLOW", fontSize = 14.sp.scaledSp())
                }
            }
            
            // 底部输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    placeholder = { Text("请输入问题", fontSize = 16.sp.scaledSp()) },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    trailingIcon = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    if (userInput.isNotEmpty() && !isLoading) {
                                        // 根据选择的模型发送消息
                                        if (selectedModel == "DeepSeek") {
                                            viewModel.sendMessageToDeepSeek(userInput)
                                        } else {
                                            viewModel.sendQueryToRagFlow(userInput)
                                        }
                                        userInput = ""
                                    }
                                },
                                enabled = userInput.isNotEmpty() && !isLoading
                            ) {
                                Icon(
                                    Icons.Filled.Send,
                                    contentDescription = "发送",
                                    tint = if (userInput.isNotEmpty() && !isLoading)
                                        if (selectedModel == "DeepSeek") 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DomainMessageItem(message: com.sdu.novaglide.domain.model.ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER) 
            Alignment.End else Alignment.Start
    ) {
        if (message.role != com.sdu.novaglide.domain.model.MessageRole.USER) {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = if (message.role == com.sdu.novaglide.domain.model.MessageRole.ASSISTANT) "规划小助手" else "系统",
                style = MaterialTheme.typography.labelSmall
            )
        }
        
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER) 16.dp else 4.dp,
                        bottomEnd = if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER) 4.dp else 16.dp
                    )
                )
                .background(
                    if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            if (message.isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "思考中",
                        color = if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER) 
                            MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER)
                            MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                // 添加日志跟踪消息内容
                val displayContent = if (message.content.isBlank()) "(空内容)" else message.content
                val textColor = if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER) 
                    MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                
                // 如果是用户消息，使用普通Text；如果是AI回复，使用MarkdownText
                if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER) {
                    Text(
                        text = displayContent,
                        color = textColor,
                        fontSize = 14.sp
                    )
                } else {
                    // AI回复使用Markdown渲染
                    MarkdownText(
                        content = displayContent,
                        textColor = textColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 强制重组，确保内容显示
                DisposableEffect(message.content) {
                    onDispose { }
                }
            }
        }
        
        // 显示文档引用
        if (message.references.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 4.dp, end = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "文档引用",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    message.references.forEach { reference ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Description,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${reference.documentName}${if (reference.pageNumber != null) " (第${reference.pageNumber}页)" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
} 