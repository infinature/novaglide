package com.sdu.novaglide.ui.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.domain.model.UserInfo
import com.sdu.novaglide.data.repository.UserRepository
import com.sdu.novaglide.data.repository.UserRepositoryImpl
import com.sdu.novaglide.data.local.dao.UserDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 用户信息状态
 */
sealed class UserInfoState {
    object Loading : UserInfoState()
    data class Success(val userInfo: UserInfo) : UserInfoState()
    data class Error(val message: String) : UserInfoState()
}

/**
 * 用户信息ViewModel
 */
class UserInfoViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val TAG = "UserInfoViewModel"
    
    // 用户信息状态Flow
    private val _userInfoState = MutableStateFlow<UserInfoState>(UserInfoState.Loading)
    val userInfoState: StateFlow<UserInfoState> = _userInfoState
    
    /**
     * 加载当前用户信息
     */
    fun loadCurrentUserInfo() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始加载当前用户信息")
                _userInfoState.value = UserInfoState.Loading
                
                userRepository.getCurrentUserInfo()
                    .catch { exception -> 
                        Log.e(TAG, "加载用户数据出错: ${exception.message}", exception)
                        _userInfoState.value = UserInfoState.Error(exception.message ?: "加载用户数据时出错") 
                    }
                    .collect { result ->
                        when {
                            result.isSuccess -> {
                                val userInfo = result.getOrNull()
                                if (userInfo != null) {
                                    Log.d(TAG, "成功加载用户: ${userInfo.userId}")
                                    _userInfoState.value = UserInfoState.Success(userInfo)
                                } else {
                                    Log.w(TAG, "用户数据为空")
                                    _userInfoState.value = UserInfoState.Error("用户数据为空")
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
