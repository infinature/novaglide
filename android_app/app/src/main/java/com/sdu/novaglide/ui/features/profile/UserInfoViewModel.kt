package com.sdu.novaglide.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.domain.model.UserInfo // 导入正确的UserInfo模型
import com.sdu.novaglide.data.repository.UserRepository // 导入正确的UserRepository接口
import com.sdu.novaglide.data.repository.UserRepositoryImpl // 导入正确的UserRepository实现
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 用户信息状态
 */
sealed class UserInfoState {
    object Loading : UserInfoState()
    data class Success(val userInfo: com.sdu.novaglide.domain.model.UserInfo) : UserInfoState() // 使用导入的UserInfo
    data class Error(val message: String) : UserInfoState()
}

/**
 * 用户信息ViewModel - 不再使用Hilt依赖注入
 */
class UserInfoViewModel(
    private val userRepository: com.sdu.novaglide.data.repository.UserRepository // 使用导入的UserRepository
) : ViewModel() {
    
    // 用户信息状态Flow
    private val _userInfoState = MutableStateFlow<UserInfoState>(UserInfoState.Loading)
    val userInfoState: StateFlow<UserInfoState> = _userInfoState.asStateFlow()
    
    /**
     * 加载当前用户信息
     */
    fun loadCurrentUserInfo() {
        viewModelScope.launch {
            userRepository.getCurrentUserInfo()
                .onStart { _userInfoState.value = UserInfoState.Loading }
                .collect { result ->
                    _userInfoState.value = result.fold(
                        onSuccess = { UserInfoState.Success(it) },
                        onFailure = { UserInfoState.Error(it.message ?: "未知错误") }
                    )
                }
        }
    }
    
    /**
     * 根据ID加载用户信息
     */
    fun loadUserInfoById(userId: String) {
        viewModelScope.launch {
            userRepository.getUserInfoById(userId)
                .onStart { _userInfoState.value = UserInfoState.Loading }
                .collect { result ->
                    _userInfoState.value = result.fold(
                        onSuccess = { UserInfoState.Success(it) },
                        onFailure = { UserInfoState.Error(it.message ?: "未知错误") }
                    )
                }
        }
    }
    
    /**
     * ViewModel工厂，用于创建UserInfoViewModel实例
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserInfoViewModel::class.java)) {
                // 使用导入的UserRepositoryImpl
                return UserInfoViewModel(UserRepositoryImpl()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
