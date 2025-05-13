package com.sdu.novaglide.ui.features.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History

import androidx.compose.material.icons.filled.History

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import com.sdu.novaglide.ui.components.BottomNavBar

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserInfo: () -> Unit = {} // 添加导航到用户信息页面的回调
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedIndex = 2, // 个人页面是第三个选项卡
                onHomeClick = { onNavigateBack() },
                onChatClick = { /* 导航到聊天页面 */ },
                onProfileClick = { /* 当前页面，无需操作 */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // 用户信息卡片，添加点击事件导航到用户信息页面
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onNavigateToUserInfo() }, // 添加点击事件
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 用户头像
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        // 可以替换为实际头像图片
                        // Image(
                        //     painter = painterResource(id = R.drawable.avatar_placeholder),
                        //     contentDescription = "用户头像",
                        //     modifier = Modifier.fillMaxSize(),
                        //     contentScale = ContentScale.Crop
                        // )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 用户名
                    Text(
                        text = "昵称",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 用户简介
                    Text(
                        text = "欢迎访问",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // 功能列表
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileMenuItem(
                        icon = Icons.Filled.Favorite,
                        title = "我的收藏",
                        onClick = { /* 导航到收藏页面 */ }
                    )
                    
                    Divider(modifier = Modifier.padding(start = 56.dp, end = 16.dp))
                    
                    ProfileMenuItem(
                        icon = Icons.Filled.History,
                        title = "浏览历史",
                        onClick = { /* 导航到浏览历史页面 */ }
                    )
                    
                    Divider(modifier = Modifier.padding(start = 56.dp, end = 16.dp))
                    
                    ProfileMenuItem(
                        icon = Icons.Filled.Settings,
                        title = "偏好设置",
                        onClick = { /* 导航到设置页面 */ }
                    )
                }
            }
            
            // 退出登录
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                ProfileMenuItem(
                    icon = Icons.Filled.ExitToApp,
                    title = "退出登录",
                    onClick = { /* 执行退出登录操作 */ },
                    showArrow = false,
                    textColor = Color.Red
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    showArrow: Boolean = true,
    textColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }  // 修复：使用正确的 lambda 语法
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        if (showArrow) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "更多",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}