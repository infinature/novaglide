package com.sdu.novaglide.ui.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.domain.model.UserInfo
import com.sdu.novaglide.data.repository.UserRepository
import com.sdu.novaglide.data.repository.UserRepositoryImpl
import com.sdu.novaglide.data.local.dao.UserDao
import com.sdu.novaglide.data.local.entity.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 用户信息状态
 */
sealed class UserInfoState {
    object Loading : UserInfoState()
    data class Success(val userInfo: UserInfo) : UserInfoState()
    data class Error(val message: String) : UserInfoState()
}

/**
 * 登录结果状态
 */
sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class Success(val userInfo: UserInfo) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

/**
 * 注册结果状态
 */
sealed class RegisterResult {
    object Idle : RegisterResult()
    object Loading : RegisterResult()
    data class Success(val userId: String) : RegisterResult() // 成功时返回用户ID
    data class Error(val message: String) : RegisterResult()
}

/**
 * 编辑用户信息结果状态
 */
sealed class EditUserInfoResult {
    object Idle : EditUserInfoResult()
    object Loading : EditUserInfoResult()
    object Success : EditUserInfoResult()
    data class Error(val message: String) : EditUserInfoResult()
}

/**
 * 用户信息ViewModel
 */
class UserInfoViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val TAG = "UserInfoViewModel"
    
    // 用户信息状态Flow
    private val _userInfoState = MutableStateFlow<UserInfoState>(UserInfoState.Loading) // 初始可以是Loading
    val userInfoState: StateFlow<UserInfoState> = _userInfoState

    // 登录状态Flow
    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginState: StateFlow<LoginResult> = _loginState.asStateFlow()

    // 注册状态Flow
    private val _registerState = MutableStateFlow<RegisterResult>(RegisterResult.Idle)
    val registerState: StateFlow<RegisterResult> = _registerState.asStateFlow()

    // 编辑用户信息状态Flow
    private val _editUserInfoResult = MutableStateFlow<EditUserInfoResult>(EditUserInfoResult.Idle)
    val editUserInfoResult: StateFlow<EditUserInfoResult> = _editUserInfoResult.asStateFlow()
    
    /**
     * 加载当前用户信息
     */
    fun loadCurrentUserInfo() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始加载当前用户信息 (isLoggedIn=true)")
                _userInfoState.value = UserInfoState.Loading
                
                userRepository.getCurrentUserInfo() // 这个方法现在会获取 isLoggedIn = true 的用户
                    .catch { exception -> 
                        Log.e(TAG, "加载用户数据出错: ${exception.message}", exception)
                        _userInfoState.value = UserInfoState.Error(exception.message ?: "加载用户数据时出错") 
                    }
                    .collect { result ->
                        when {
                            result.isSuccess -> {
                                val userInfo = result.getOrNull()
                                if (userInfo != null) {
                                    Log.d(TAG, "成功加载已登录用户: ${userInfo.userId}")
                                    _userInfoState.value = UserInfoState.Success(userInfo)
                                } else {
                                    Log.w(TAG, "没有找到已登录的用户 (isLoggedIn=true)")
                                    _userInfoState.value = UserInfoState.Error("用户未登录或会话已过期")
                                }
                            }
                            else -> {
                                val exception = result.exceptionOrNull()
                                Log.e(TAG, "加载用户失败: ${exception?.message}", exception)
                                _userInfoState.value = UserInfoState.Error(exception?.message ?: "未知错误")
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "加载用户信息时发生系统错误: ${e.message}", e)
                _userInfoState.value = UserInfoState.Error("系统错误: ${e.message}")
            }
        }
    }
    
    /**
     * 根据ID加载用户信息
     */
    fun loadUserInfoById(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.getUserInfoById(userId)
                    .onStart { _userInfoState.value = UserInfoState.Loading }
                    .catch { exception -> 
                        _userInfoState.value = UserInfoState.Error(exception.message ?: "加载用户数据时出错") 
                    }
                    .collect { result ->
                        // 修改Result处理逻辑，使用when表达式替代fold
                        when {
                            result.isSuccess -> {
                                val userInfo = result.getOrNull()
                                if (userInfo != null) {
                                    _userInfoState.value = UserInfoState.Success(userInfo)
                                } else {
                                    _userInfoState.value = UserInfoState.Error("用户数据为空")
                                }
                            }
                            else -> {
                                val exception = result.exceptionOrNull()
                                _userInfoState.value = UserInfoState.Error(exception?.message ?: "未知错误")
                            }
                        }
                    }
            } catch (e: Exception) {
                _userInfoState.value = UserInfoState.Error("系统错误: ${e.message}")
            }
        }
    }

    /**
     * 尝试用户登录
     */
    fun attemptLogin(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginResult.Loading
            Log.d(TAG, "ViewModel: 开始尝试登录，用户名: $username")
            try {
                val userInfo = userRepository.login(username, password)
                if (userInfo != null) {
                    Log.d(TAG, "ViewModel: 登录成功，用户: ${userInfo.username}")
                    // 登录成功后，将该用户的 isLoggedIn 状态更新为 true
                    userRepository.updateLoginStatus(userInfo.userId, true)
                    _loginState.value = LoginResult.Success(userInfo)
                    _userInfoState.value = UserInfoState.Success(userInfo)
                } else {
                    Log.w(TAG, "ViewModel: 登录失败，用户名或密码错误: $username")
                    _loginState.value = LoginResult.Error("用户名或密码错误")
                }
            } catch (e: Exception) {
                Log.e(TAG, "ViewModel: 登录时发生异常，用户名: $username", e)
                _loginState.value = LoginResult.Error("登录时发生错误: ${e.message}")
            }
        }
    }

    /**
     * 重置登录状态，以便下次登录时UI能正确响应
     */
    fun resetLoginState() {
        _loginState.value = LoginResult.Idle
    }

    /**
     * 用户登出
     */
    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "ViewModel: 开始登出")
            val currentState = _userInfoState.value
            val userIdToLogout: String? = if (currentState is UserInfoState.Success) {
                currentState.userInfo.userId
            } else {
                // 如果当前状态不是Success, 可能意味着用户状态已经不一致或未加载
                // 尝试从数据库获取最后一次标记为登录的用户并登出他
                // 但更安全的做法是清除所有用户的登录标记，以防万一
                null 
            }

            if (userIdToLogout != null) {
                userRepository.updateLoginStatus(userIdToLogout, false)
                Log.d(TAG, "用户 $userIdToLogout 已登出，isLoggedIn 设置为 false")
            } else {
                // 如果无法获取当前用户ID（例如，状态已经是Error或Loading），
                // 作为安全措施，可以将所有用户的登录状态清除。
                userRepository.clearAllLoginStatus()
                Log.w(TAG, "无法获取当前登录用户ID或状态非Success，已清除所有用户的登录标记")
            }
            
            // 重置 ViewModel 中的状态
            // 设置为一个明确的“已登出”状态，HomeScreen应该能识别并显示适当的UI（例如，不显示用户信息）
            // 而不是显示加载指示器，这可能导致闪动。
            _userInfoState.value = UserInfoState.Error("用户已登出") 
            _loginState.value = LoginResult.Idle
            Log.d(TAG, "ViewModel: 用户状态已设置为 Error('用户已登出')")
        }
    }

    /**
     * 尝试用户注册
     */
    fun attemptRegistration(
        // userId: String, // userId 将在内部生成
        username: String,
        nickname: String,
        email: String,
        password: String, // 接收明文密码
        phone: String,
        avatar: String,
        bio: String,
        registrationDate: Date,
        lastLoginDate: Date,
        eduLevel: String,
        institution: String,
        graduationYear: Int?
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterResult.Loading
            Log.d(TAG, "ViewModel: 开始尝试注册，用户名: $username")
            try {
                val existingUser = userRepository.getUserByUsername(username) // 需要在UserRepository添加此方法
                if (existingUser != null) {
                    _registerState.value = RegisterResult.Error("用户名 '$username' 已被注册")
                    return@launch
                }

                // 生成新的 userId
                val maxUserIdStr = userRepository.getMaxUserId()
                val newUserIdNumber = if (maxUserIdStr != null && maxUserIdStr.startsWith("U") && maxUserIdStr.length > 1) {
                    try {
                        maxUserIdStr.substring(1).toIntOrNull()?.plus(1) ?: 1
                    } catch (e: NumberFormatException) {
                        1 // 如果解析失败，则从1开始
                    }
                } else {
                    1 // 如果没有用户或格式不符，则从1开始
                }
                val newUserId = "U%06d".format(newUserIdNumber) // 格式化为 U000001, U000002 等

                val newUserEntity = UserEntity( // 直接创建UserEntity，包含密码
                    userId = newUserId, // 使用新生成的userId
                    username = username,
                    nickname = nickname,
                    email = email,
                    password = password, // 存储明文密码
                    phone = phone,
                    avatar = avatar,
                    bio = bio,
                    registrationDate = registrationDate,
                    lastLoginDate = lastLoginDate,
                    eduLevel = eduLevel,
                    institution = institution,
                    graduationYear = graduationYear
                )
                val success = userRepository.registerUser(newUserEntity) // 修改UserRepository接口和实现
                if (success) {
                    Log.d(TAG, "ViewModel: 注册成功，用户ID: $newUserId")
                    _registerState.value = RegisterResult.Success(newUserId)
                } else {
                    Log.w(TAG, "ViewModel: 注册失败，用户名: $username (可能是数据库插入问题或ID冲突-理论上已处理)")
                    _registerState.value = RegisterResult.Error("注册失败，请稍后重试")
                }
            } catch (e: Exception) {
                Log.e(TAG, "ViewModel: 注册时发生异常，用户名: $username", e)
                _registerState.value = RegisterResult.Error("注册时发生错误: ${e.message}")
            }
        }
    }

    /**
     * 更新用户可编辑信息
     */
    fun updateEditableUserInfo(updatedUserInfo: UserInfo) {
        viewModelScope.launch {
            _editUserInfoResult.value = EditUserInfoResult.Loading
            Log.d(TAG, "ViewModel: 开始更新用户信息，用户ID: ${updatedUserInfo.userId}")
            try {
                val success = userRepository.updateUserInfo(updatedUserInfo)
                if (success) {
                    Log.d(TAG, "ViewModel: 用户信息更新成功，用户ID: ${updatedUserInfo.userId}")
                    _editUserInfoResult.value = EditUserInfoResult.Success
                    // 更新成功后，也刷新 _userInfoState
                    _userInfoState.value = UserInfoState.Success(updatedUserInfo)
                } else {
                    Log.w(TAG, "ViewModel: 用户信息更新失败，用户ID: ${updatedUserInfo.userId}")
                    _editUserInfoResult.value = EditUserInfoResult.Error("更新失败，请稍后重试")
                }
            } catch (e: Exception) {
                Log.e(TAG, "ViewModel: 更新用户信息时发生异常，用户ID: ${updatedUserInfo.userId}", e)
                _editUserInfoResult.value = EditUserInfoResult.Error("更新时发生错误: ${e.message}")
            }
        }
    }

    /**
     * 重置注册状态
     */
    fun resetRegisterState() {
        _registerState.value = RegisterResult.Idle
    }

    /**
     * 重置编辑用户信息状态
     */
    fun resetEditUserInfoResult() {
        _editUserInfoResult.value = EditUserInfoResult.Idle
    }

    /**
     * 设置注册错误（用于客户端验证）
     */
    fun setRegisterError(message: String) {
        _registerState.value = RegisterResult.Error(message)
    }
    
    /**
     * ViewModel工厂，用于创建UserInfoViewModel实例
     */
    class Factory(private val userDao: UserDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserInfoViewModel::class.java)) {
                val repository = UserRepositoryImpl(userDao)
                return UserInfoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
