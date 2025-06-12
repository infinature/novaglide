package com.sdu.novaglide.ui.features.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sdu.novaglide.domain.model.UserInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserInfoScreen(
    viewModel: UserInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userInfoState by viewModel.userInfoState.collectAsState()
    val editUserInfoResult by viewModel.editUserInfoResult.collectAsState()

    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var eduLevel by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var graduationYearStr by remember { mutableStateOf("") }

    var initialUserInfo: UserInfo? by remember { mutableStateOf(null) }

    LaunchedEffect(userInfoState) {
        if (userInfoState is UserInfoState.Success) {
            val currentUserInfo = (userInfoState as UserInfoState.Success).userInfo
            if (initialUserInfo == null) { // 仅在首次加载时填充
                initialUserInfo = currentUserInfo
                nickname = currentUserInfo.nickname
                email = currentUserInfo.email
                phone = currentUserInfo.phone
                bio = currentUserInfo.bio
                eduLevel = currentUserInfo.eduLevel
                institution = currentUserInfo.institution
                graduationYearStr = currentUserInfo.graduationYear?.toString() ?: ""
            }
        }
    }

    LaunchedEffect(editUserInfoResult) {
        when (val result = editUserInfoResult) {
            is EditUserInfoResult.Success -> {
                Toast.makeText(context, "信息更新成功", Toast.LENGTH_SHORT).show()
                viewModel.resetEditUserInfoResult() // 重置状态
                onNavigateBack() // 可以选择返回上一页
            }
            is EditUserInfoResult.Error -> {
                Toast.makeText(context, "更新失败: ${result.message}", Toast.LENGTH_LONG).show()
                viewModel.resetEditUserInfoResult() // 重置状态
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑个人信息") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        initialUserInfo?.let { currentUser ->
                            val updatedUserInfo = currentUser.copy(
                                nickname = nickname,
                                email = email,
                                phone = phone,
                                bio = bio,
                                eduLevel = eduLevel,
                                institution = institution,
                                graduationYear = graduationYearStr.toIntOrNull()
                            )
                            viewModel.updateEditableUserInfo(updatedUserInfo)
                        }
                    }, enabled = editUserInfoResult !is EditUserInfoResult.Loading) {
                        Icon(Icons.Filled.Done, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (initialUserInfo == null || userInfoState is UserInfoState.Loading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (userInfoState is UserInfoState.Error) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("加载用户信息失败: ${(userInfoState as UserInfoState.Error).message}")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("昵称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("手机号") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("简介") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = eduLevel,
                    onValueChange = { eduLevel = it },
                    label = { Text("学历") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("学校/机构") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = graduationYearStr,
                    onValueChange = { graduationYearStr = it.filter { char -> char.isDigit() } },
                    label = { Text("毕业年份 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                if (editUserInfoResult is EditUserInfoResult.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}
