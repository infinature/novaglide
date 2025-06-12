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
    onNavigateToChat: () -> Unit,
    onNavigateToLogout: () -> Unit, // 新增登出回调
    onNavigateToEditUserInfo: () -> Unit // 新增导航到编辑页面的回调
) {
    val userInfoState by viewModel.userInfoState.collectAsState()

    // 修改 LaunchedEffect 逻辑
    // 只有当 userInfoState 不是 Success 状态，且不是 Loading 状态时，
    // 才考虑加载当前用户（例如应用启动直接进入此页面，或从后台恢复）
    // 如果是通过登录流程导航到此页面，userInfoState 应该已经是 Success 状态。
    LaunchedEffect(userInfoState) { // 观察 userInfoState 的变化
        if (userInfoState !is UserInfoState.Success && userInfoState !is UserInfoState.Loading) {
            // 避免在登录成功后立即覆盖 userInfoState
            // 如果需要处理应用启动直接进入 Profile 页的场景，
            // 可以在 ViewModel 初始化时或此处进行一次加载。
            // 但要确保这个加载不会覆盖登录成功设置的状态。
            // 一个更安全的做法是，如果 viewModel.userInfoState 初始为 Loading 或 Error，
            // 并且没有正在进行的登录操作，才执行 loadCurrentUserInfo。
            // 此处简化为：如果不是Success也不是Loading，则尝试加载。
            // 这假设ViewModel的初始状态可能是Loading，然后变为Error或Success。
            // 如果直接进入此页面，userInfoState可能是初始的Loading或Error。
            viewModel.loadCurrentUserInfo()
        }
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
                ProfileMenuItem(icon = Icons.Filled.Edit, title = "信息编辑", onClick = onNavigateToEditUserInfo) // 修改标题和onClick
                ProfileMenuItem(icon = Icons.Filled.ExitToApp, title = "退出登录", onClick = onNavigateToLogout)
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