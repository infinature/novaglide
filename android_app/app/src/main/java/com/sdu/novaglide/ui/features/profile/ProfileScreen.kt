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
    onNavigateToLogout: () -> Unit, // 登出回调，由 AppNavigation 实现导航栈清理
    onNavigateToEditUserInfo: () -> Unit, // 新增导航到编辑页面的回调
    onNavigateToBrowsingHistory: () -> Unit, // 确保此参数存在
    onNavigateToFavorites: () -> Unit, // 新增导航到收藏页面的回调
    onNavigateToLogin: () -> Unit = {}, // 新增登录导航回调
    onNavigateToRegister: () -> Unit = {}, // 新增注册导航回调
    onNavigateToAbout: () -> Unit = {}, // 新增关于页面导航回调
    onNavigateToSettings: () -> Unit = {} // 新增设置页面导航回调
) {
    val userInfoState by viewModel.userInfoState.collectAsState()

    // 在应用启动时尝试加载用户信息
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
                .background(MaterialTheme.colorScheme.background), // 使用主题背景色
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 用户信息卡片
            when (val state = userInfoState) {
                is UserInfoState.Success -> {
                    // 已登录状态：显示用户信息
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onNavigateToUserInfo() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 用户头像
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.userInfo.nickname.isNotEmpty()) {
                                    Text(
                                        text = state.userInfo.nickname.first().toString(),
                                        fontSize = 24.sp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = state.userInfo.nickname,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = state.userInfo.bio.take(30) + if (state.userInfo.bio.length > 30) "..." else "",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                else -> {
                    // 未登录状态：显示登录/注册选项
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 游客头像
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "游客",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "欢迎使用 NovaGlide",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "登录后享受更多功能",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 登录/注册按钮
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToLogin,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("登录")
                                }
                                
                                OutlinedButton(
                                    onClick = onNavigateToRegister,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("注册")
                                }
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
                when (userInfoState) {
                    is UserInfoState.Success -> {
                        // 已登录状态：显示完整功能列表
                        ProfileMenuItem(icon = Icons.Filled.Favorite, title = "我的收藏", onClick = onNavigateToFavorites)
                        ProfileMenuItem(icon = Icons.Filled.History, title = "浏览历史", onClick = onNavigateToBrowsingHistory)
                        ProfileMenuItem(icon = Icons.Filled.Edit, title = "信息编辑", onClick = onNavigateToEditUserInfo)
                        
                        // 分隔线
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 应用设置相关
                        ProfileMenuItem(icon = Icons.Filled.Settings, title = "设置", onClick = onNavigateToSettings)
                        ProfileMenuItem(icon = Icons.Filled.Info, title = "关于应用", onClick = onNavigateToAbout)
                        
                        // 分隔线
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ProfileMenuItem(icon = Icons.Filled.ExitToApp, title = "退出登录", onClick = {
                            viewModel.logout()
                            onNavigateToLogout()
                        })
                    }
                    else -> {
                        // 未登录状态：显示基础功能和提示信息
                        ProfileMenuItem(
                            icon = Icons.Filled.Info,
                            title = "关于应用",
                            onClick = onNavigateToAbout
                        )
                        ProfileMenuItem(
                            icon = Icons.Filled.Settings,
                            title = "设置",
                            onClick = onNavigateToSettings
                        )
                        
                        // 登录提示卡片
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.LockOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "登录后解锁更多功能",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• 个人收藏管理\n• 浏览历史记录\n• 个性化推荐\n• 用户资料编辑",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = "进入", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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