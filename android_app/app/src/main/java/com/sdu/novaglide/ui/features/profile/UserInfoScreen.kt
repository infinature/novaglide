package com.sdu.novaglide.ui.features.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.domain.model.UserInfo
import java.text.SimpleDateFormat
import java.util.*

private const val TAG_SCREEN = "UserInfoScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    viewModel: UserInfoViewModel, // 接收共享的 ViewModel 作为参数
    onNavigateBack: () -> Unit
) {
    val userInfoState by viewModel.userInfoState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // 获取用户数据
    LaunchedEffect(userInfoState) { // 观察 userInfoState
        if (userInfoState !is UserInfoState.Success && userInfoState !is UserInfoState.Loading) {
            Log.d(TAG_SCREEN, "UserInfoScreen: userInfoState 不是 Success 或 Loading，尝试加载当前用户信息。")
            viewModel.loadCurrentUserInfo()
        } else if (userInfoState is UserInfoState.Success) {
            Log.d(TAG_SCREEN, "UserInfoScreen: userInfoState 已经是 Success，用户: ${(userInfoState as UserInfoState.Success).userInfo.username}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "个人资料",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when (val state = userInfoState) {
                is UserInfoState.Loading -> {
                    Log.d(TAG_SCREEN, "UserInfoScreen: 显示加载中...")
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UserInfoState.Error -> {
                    Log.e(TAG_SCREEN, "UserInfoScreen: 显示错误 - ${state.message}")
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "加载失败: ${state.message}",
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                Log.d(TAG_SCREEN, "UserInfoScreen: 点击重试按钮")
                                viewModel.loadCurrentUserInfo() 
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("重试")
                        }
                    }
                }
                is UserInfoState.Success -> {
                    Log.d(TAG_SCREEN, "UserInfoScreen: 显示成功，用户: ${state.userInfo.username}")
                    UserInfoContent(userData = state.userInfo, dateFormat = dateFormat)
                }
            }
        }
    }
}

@Composable
fun UserInfoContent(userData: UserInfo, dateFormat: SimpleDateFormat) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 用户头像和基本信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) { // Card的content lambda开始
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 头像
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    // 可以添加实际头像显示
                    if (userData.nickname.isNotEmpty()) { // 添加空检查
                        Text(
                            text = userData.nickname.first().toString(),
                            fontSize = 36.sp,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 昵称
                Text(
                    text = userData.nickname,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 用户名
                Text(
                    text = "@${userData.username}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 个人简介
                Text(
                    text = userData.bio,
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } // Card的content lambda结束
        
        // 详细信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) { // Card的content lambda开始
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                InfoItem("用户ID", userData.userId)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                InfoItem("邮箱", userData.email)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                InfoItem("手机号", userData.phone)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                InfoItem("注册时间", dateFormat.format(userData.registrationDate))
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                InfoItem("最近登录", dateFormat.format(userData.lastLoginDate))
            }
        } // Card的content lambda结束
        
        // 教育信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) { // Card的content lambda开始
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "教育信息",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                InfoItem("学历", userData.eduLevel)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                InfoItem("学校", userData.institution)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                InfoItem("毕业年份", userData.graduationYear?.toString() ?: "未设置")
            }
        } // Card的content lambda结束
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color.Gray
        )
        
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
