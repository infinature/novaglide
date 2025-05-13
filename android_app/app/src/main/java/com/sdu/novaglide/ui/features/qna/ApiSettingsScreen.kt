package com.sdu.novaglide.ui.features.qna

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiSettingsViewModel
) {
    val deepSeekApiKey by viewModel.deepSeekApiKey.collectAsState(initial = "")
    val ragFlowApiKey by viewModel.ragFlowApiKey.collectAsState(initial = "")
    val ragFlowServerUrl by viewModel.ragFlowServerUrl.collectAsState(initial = "")
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()
    
    var showDeepSeekApiKey by remember { mutableStateOf(false) }
    var showRagFlowApiKey by remember { mutableStateOf(false) }
    
    // 设置初始密钥，只在界面中设置，不存储在代码中
    LaunchedEffect(Unit) {
        // 设置DeepSeek初始密钥
        if (deepSeekApiKey.isEmpty()) {
            viewModel.updateDeepSeekApiKey("sk-d82d71e0e54a4caca57f60e3c471a803")
        }
        
        // 设置RagFlow初始密钥
        if (ragFlowApiKey.isEmpty()) {
            viewModel.updateRagFlowApiKey("ragflow-RhZjEzMjlhMmZlZTExZjA4YmUyNDIwMT")
        }

        // 设置RagFlow初始服务器URL
        if (ragFlowServerUrl.isEmpty()) {
            viewModel.updateRagFlowServerUrl("https://demo.ragflow.io")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            viewModel.saveSettings() 
                        },
                        enabled = !isSaving
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 信息提示
            infoMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            // DeepSeek设置
            Text(
                text = "DeepSeek设置",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = deepSeekApiKey,
                onValueChange = { viewModel.updateDeepSeekApiKey(it) },
                label = { Text("API密钥") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showDeepSeekApiKey) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showDeepSeekApiKey = !showDeepSeekApiKey }) {
                        Icon(
                            if (showDeepSeekApiKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showDeepSeekApiKey) "隐藏密钥" else "显示密钥"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // RAGFlow设置
            Text(
                text = "RAGFlow设置",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = ragFlowServerUrl,
                onValueChange = { viewModel.updateRagFlowServerUrl(it) },
                label = { Text("服务器地址") },
                placeholder = { Text("例如: http://localhost") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = ragFlowApiKey,
                onValueChange = { viewModel.updateRagFlowApiKey(it) },
                label = { Text("API密钥") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showRagFlowApiKey) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showRagFlowApiKey = !showRagFlowApiKey }) {
                        Icon(
                            if (showRagFlowApiKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showRagFlowApiKey) "隐藏密钥" else "显示密钥"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 提示信息
            Text(
                text = "DeepSeek API密钥在 https://platform.deepseek.com/api-keys 获取",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "RagFlow API密钥在 http://[服务器地址]/user-setting/api 获取",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "注意：API密钥将安全地加密存储在设备上，不会传输到第三方。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            // 保存成功提示
            if (saveSuccess) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("设置已保存")
                    }
                }
            }
        }
    }
} 