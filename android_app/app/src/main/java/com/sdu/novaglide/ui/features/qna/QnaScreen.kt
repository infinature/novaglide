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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                        fontSize = 18.sp
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
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
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
            
            // 错误消息提示
            errorMessage?.let { error ->
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
                            .padding(16.dp)
                            .clickable { viewModel.clearError() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
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
                            MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
                        contentColor = if (selectedModel == "DeepSeek") 
                            Color.White else Color.Black
                    ),
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("DeepSeek")
                }
                
                // RAGFlow 按钮
                Button(
                    onClick = { selectedModel = "RAGFlow" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedModel == "RAGFlow") 
                            MaterialTheme.colorScheme.secondary else Color(0xFFE0E0E0),
                        contentColor = if (selectedModel == "RAGFlow") 
                            Color.White else Color.Black
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("RAGFLOW")
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
                    placeholder = { Text("请输入问题") },
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.LightGray
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
                                        Color.Gray
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
                text = if (message.role == com.sdu.novaglide.domain.model.MessageRole.ASSISTANT) "秦媛" else "系统",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
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
                        Color(0xFF2196F3) 
                    else 
                        Color(0xFFE0E0E0)
                )
                .padding(12.dp)
        ) {
            if (message.isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "思考中",
                        color = if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER) 
                            Color.White else Color.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER)
                            Color.White else Color(0xFF2196F3),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                // 添加日志跟踪消息内容
                val displayContent = if (message.content.isBlank()) "(空内容)" else message.content
                
                Text(
                    text = displayContent,
                    color = if (message.role == com.sdu.novaglide.domain.model.MessageRole.USER) 
                        Color.White else Color.Black,
                    fontSize = 14.sp
                )
                
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