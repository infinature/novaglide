package com.sdu.novaglide.ui.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.core.util.ValidationUtils
import com.sdu.novaglide.ui.features.profile.RegisterResult
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: UserInfoViewModel, // Accept shared ViewModel
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // 验证错误状态
    var usernameError by remember { mutableStateOf<String?>(null) }
    var nicknameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val registerState by viewModel.registerState.collectAsState()

    // 实时验证
    LaunchedEffect(username) {
        usernameError = ValidationUtils.getUsernameErrorMessage(username)
    }
    LaunchedEffect(nickname) {
        nicknameError = ValidationUtils.getNicknameErrorMessage(nickname)
    }
    LaunchedEffect(email) {
        emailError = ValidationUtils.getEmailErrorMessage(email)
    }
    LaunchedEffect(phone) {
        phoneError = ValidationUtils.getPhoneErrorMessage(phone)
    }
    LaunchedEffect(password) {
        passwordError = ValidationUtils.getPasswordErrorMessage(password)
    }
    LaunchedEffect(confirmPassword) {
        confirmPasswordError = if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            "密码不匹配"
        } else null
    }

    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is RegisterResult.Success -> {
                onNavigateToLogin() 
                viewModel.resetRegisterState()
            }
            is RegisterResult.Error -> {
                // errorMessage = state.message // UI上显示错误
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建账户", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("加入 NovaGlide", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                leadingIcon = { Icon(Icons.Filled.PersonOutline, "用户名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = usernameError != null,
                supportingText = usernameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称") },
                leadingIcon = { Icon(Icons.Filled.Face, "昵称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nicknameError != null,
                supportingText = nicknameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") },
                leadingIcon = { Icon(Icons.Filled.Email, "邮箱") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("手机号（可选）") },
                leadingIcon = { Icon(Icons.Filled.Phone, "手机号") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = phoneError != null,
                supportingText = phoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                leadingIcon = { Icon(Icons.Filled.LockOpen, "密码") },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("确认密码") },
                leadingIcon = { Icon(Icons.Filled.Lock, "确认密码") },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showConfirmPassword) "隐藏密码" else "显示密码"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (registerState is RegisterResult.Error) {
                Text(
                    text = (registerState as RegisterResult.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // 检查所有验证错误
                    val hasErrors = usernameError != null || nicknameError != null || 
                                   emailError != null || phoneError != null || 
                                   passwordError != null || confirmPasswordError != null
                    
                    if (hasErrors) {
                        viewModel.setRegisterError("请修正表单中的错误")
                        return@Button
                    }
                    
                    if (username.isBlank() || nickname.isBlank() || email.isBlank() || password.isBlank()) {
                        viewModel.setRegisterError("请填写所有必填字段")
                        return@Button
                    }
                    
                    if (password != confirmPassword) {
                        viewModel.setRegisterError("密码不匹配")
                        return@Button
                    }
                    
                    // 最终验证
                    if (!ValidationUtils.isValidUsername(username) ||
                        !ValidationUtils.isValidNickname(nickname) ||
                        !ValidationUtils.isValidEmail(email) ||
                        !ValidationUtils.isValidPassword(password) ||
                        (phone.isNotEmpty() && !ValidationUtils.isValidPhoneNumber(phone))) {
                        viewModel.setRegisterError("请检查输入信息格式")
                        return@Button
                    }
                    
                    viewModel.attemptRegistration(
                        username = username,
                        nickname = nickname,
                        email = email,
                        password = password,
                        phone = phone,
                        avatar = "",
                        bio = "NovaGlide 新用户!",
                        registrationDate = Date(),
                        lastLoginDate = Date(),
                        eduLevel = "未设置",
                        institution = "未设置",
                        graduationYear = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = registerState !is RegisterResult.Loading && 
                         usernameError == null && nicknameError == null && 
                         emailError == null && phoneError == null && 
                         passwordError == null && confirmPasswordError == null &&
                         username.isNotBlank() && nickname.isNotBlank() && 
                         email.isNotBlank() && password.isNotBlank() &&
                         password == confirmPassword
            ) {
                if (registerState is RegisterResult.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("注册", fontSize = 16.sp)
                }
            }
        }
    }
}
