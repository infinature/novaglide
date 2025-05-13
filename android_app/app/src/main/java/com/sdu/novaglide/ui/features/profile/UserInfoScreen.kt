package com.sdu.novaglide.ui.features.profile

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
// LocalContext的导入可能不再需要，除非有其他用途
// import androidx.compose.ui.platform.LocalContext 
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sdu.novaglide.domain.model.UserInfo // 导入正确的UserInfo模型
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserInfoViewModel = viewModel(factory = UserInfoViewModel.Factory())
) {
    val userInfoState by viewModel.userInfoState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // 获取用户数据
    LaunchedEffect(Unit) {
        viewModel.loadCurrentUserInfo()
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
            when (val state = userInfoState) { // 使用 'state' 变量以避免重复类型转换
                is UserInfoState.Loading -> {
                    // 加载中状态
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UserInfoState.Error -> {
                    // 错误状态
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message, // 使用 state.message
                            color = Color.Red
                        )
                        Button(
                            onClick = { viewModel.loadCurrentUserInfo() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("重试")
                        }
                    }
                }
                is UserInfoState.Success -> {
                    // 成功状态 - 显示用户信息
                    // val userData = state.userInfo // userData 类型现在是 com.sdu.novaglide.domain.model.UserInfo
                    UserInfoContent(userData = state.userInfo, dateFormat = dateFormat) // 直接传递
                }
            }
        }
    }
}

@Composable
fun UserInfoContent(userData: com.sdu.novaglide.domain.model.UserInfo, dateFormat: SimpleDateFormat) { // 确保类型为导入的UserInfo
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
        ) {
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
        }
        
        // 详细信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
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
        }
        
        // 教育信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
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
        }
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
