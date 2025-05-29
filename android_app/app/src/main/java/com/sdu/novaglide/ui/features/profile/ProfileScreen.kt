package com.sdu.novaglide.ui.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
// import androidx.compose.ui.res.painterResource // 暂时注释掉，如果实际使用需要取消注释
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.R // 假设 R 文件存在且包含 avatar_placeholder
import com.sdu.novaglide.ui.components.BottomNavBar

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserInfo: () -> Unit,
    viewModel: UserInfoViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val userInfoState by viewModel.userInfoState.collectAsState()

    // 当 ProfileScreen 可见时，加载或刷新用户信息
    LaunchedEffect(Unit) {
        viewModel.loadCurrentUserInfo()
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedIndex = 2, // 个人页面是第三个选项卡
                onHomeClick = onNavigateToHome,
                onChatClick = onNavigateToChat,
                onProfileClick = { /* 当前页面，无需操作 */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)), // 淡灰色背景
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 用户信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onNavigateToUserInfo() },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 临时头像占位符
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        // 如果有头像 URL，可以使用 Coil 等库加载
                        // Image(painter = painterResource(id = R.drawable.avatar_placeholder), contentDescription = "头像")
                        when (val state = userInfoState) {
                            is UserInfoState.Success -> {
                                if (state.userInfo.nickname.isNotEmpty()) {
                                    Text(state.userInfo.nickname.first().toString(), fontSize = 24.sp, color = Color.White)
                                }
                            }
                            else -> {
                                // 加载中或错误时可以显示默认字符或图标
                                Icon(Icons.Filled.Person, contentDescription = "默认头像", tint = Color.Gray, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        // 昵称
                        when (val state = userInfoState) {
                            is UserInfoState.Success -> {
                                Text(
                                    text = state.userInfo.nickname, 
                                    fontSize = 20.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                                // 简介或状态 - 从 state.userInfo.bio 获取
                                Text(
                                    text = state.userInfo.bio.take(30) + if (state.userInfo.bio.length > 30) "..." else "", // 显示部分简介
                                    fontSize = 14.sp, 
                                    color = Color.Gray
                                )
                            }
                            is UserInfoState.Loading -> {
                                Text(text = "加载中...", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            is UserInfoState.Error -> {
                                Text(text = "获取失败", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                    }
                }
            }
            
            // 功能列表
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ProfileMenuItem(icon = Icons.Filled.Favorite, title = "我的收藏", onClick = { /* TODO */ })
                ProfileMenuItem(icon = Icons.Filled.History, title = "浏览历史", onClick = { /* TODO */ })
                ProfileMenuItem(icon = Icons.Filled.Settings, title = "偏好设置", onClick = { /* TODO */ })
                ProfileMenuItem(icon = Icons.Filled.ExitToApp, title = "退出登录", onClick = { /* TODO */ })
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = "进入", tint = Color.Gray)
        }
    }
}

// 为了预览，可能需要一个 ViewModel 的 Fake 实现
// class FakeUserInfoViewModel : UserInfoViewModel(FakeUserRepository()) {}
// class FakeUserRepository : UserRepository { /* ... 实现接口 ... */ }

/*
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    NovaGlideTheme {
        ProfileScreen(
            onNavigateBack = {},
            onNavigateToUserInfo = {},
            viewModel = FakeUserInfoViewModel(), // 使用 Fake ViewModel
            onNavigateToHome = {},
            onNavigateToChat = {}
        )
    }
}
*/