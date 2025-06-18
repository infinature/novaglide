package com.sdu.novaglide.ui.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.ui.theme.scaledSp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于应用", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 应用图标和名称
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 应用图标占位
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = "NovaGlide",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "NovaGlide",
                        fontSize = 24.sp.scaledSp(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "职业生涯规划助手",
                        fontSize = 16.sp.scaledSp(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Text(
                        text = "版本 1.0.0",
                        fontSize = 14.sp.scaledSp(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            // 应用介绍
            AboutCard(
                title = "应用介绍",
                icon = Icons.Filled.Info
            ) {
                Text(
                    text = "NovaGlide 是一款专为大学生设计的职业生涯规划应用，提供保研、考研、留学、考公等多方向的资讯和智能问答服务，帮助你找到最适合的发展道路。",
                    fontSize = 14.sp.scaledSp(),
                    lineHeight = 20.sp.scaledSp(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 主要功能
            AboutCard(
                title = "主要功能",
                icon = Icons.Filled.Star
            ) {
                val features = listOf(
                    "📰 最新资讯推送",
                    "🔍 智能搜索功能", 
                    "💬 AI智能问答",
                    "❤️ 个人收藏管理",
                    "📚 浏览历史记录",
                    "🎯 个性化推荐"
                )
                
                features.forEach { feature ->
                    Text(
                        text = feature,
                        fontSize = 14.sp.scaledSp(),
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 开发团队
            AboutCard(
                title = "开发团队",
                icon = Icons.Filled.Group
            ) {
                Text(
                    text = "由山东大学计算机学院团队开发\n致力于为大学生提供优质的职业规划服务",
                    fontSize = 14.sp.scaledSp(),
                    lineHeight = 20.sp.scaledSp(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            // 联系我们
            AboutCard(
                title = "联系我们",
                icon = Icons.Filled.ContactMail
            ) {
                Column {
                    ContactItem(Icons.Filled.Email, "邮箱", "novaglide@sdu.edu.cn")
                    ContactItem(Icons.Filled.BugReport, "问题反馈", "GitHub Issues")
                    ContactItem(Icons.Filled.Update, "版本更新", "应用商店")
                }
            }
            
            // 法律信息
            AboutCard(
                title = "法律信息",
                icon = Icons.Filled.Gavel
            ) {
                Column {
                    TextButton(
                        onClick = { /* 打开隐私政策 */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("隐私政策", color = MaterialTheme.colorScheme.primary)
                    }
                    
                    TextButton(
                        onClick = { /* 打开用户协议 */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("用户协议", color = MaterialTheme.colorScheme.primary)
                    }
                    
                    TextButton(
                        onClick = { /* 打开开源许可 */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("开源许可", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 版权信息
            Text(
                text = "© 2024 NovaGlide Team\nAll Rights Reserved",
                fontSize = 12.sp.scaledSp(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp.scaledSp()
            )
        }
    }
}

@Composable
private fun AboutCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp.scaledSp(),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            content()
        }
    }
}

@Composable
private fun ContactItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 14.sp.scaledSp(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp.scaledSp(),
            color = MaterialTheme.colorScheme.primary
        )
    }
} 