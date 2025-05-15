package com.sdu.novaglide.ui.features.qna

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QnaScreen(
    onNavigateBack: () -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    var chatMessages by remember { 
        mutableStateOf(
            listOf(
                ChatMessage(
                    "用户",
                    "考研有哪些注意事项?",
                    isFromUser = true
                ),
                ChatMessage(
                    "秦媛",
                    "对于考研考生，以下是一些注意事项：\n\n1. 合理规划时间，制定复习计划...",
                    isFromUser = false
                )
            )
        ) 
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
            // 聊天消息列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(chatMessages) { message ->
                    MessageItem(message = message)
                    Spacer(modifier = Modifier.height(16.dp))
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
                        IconButton(
                            onClick = {
                                if (userInput.isNotEmpty()) {
                                    // 添加用户消息
                                    chatMessages = chatMessages + ChatMessage(
                                        "用户",
                                        userInput,
                                        isFromUser = true
                                    )
                                    // 清空输入框
                                    userInput = ""
                                    
                                    // 模拟AI回复
                                    chatMessages = chatMessages + ChatMessage(
                                        "秦媛",
                                        "我正在思考您的问题，稍后会给您回复...",
                                        isFromUser = false
                                    )
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "发送",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        if (!message.isFromUser) {
            Text(
                text = message.sender,
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
                        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (message.isFromUser) Color(0xFF2196F3) else Color(0xFFE0E0E0)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = if (message.isFromUser) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

data class ChatMessage(
    val sender: String,
    val content: String,
    val isFromUser: Boolean
) 