package com.sdu.novaglide.domain.usecase

import com.sdu.novaglide.data.repository.UserRepository
import com.sdu.novaglide.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * 获取用户信息的用例
 */
class GetUserInfoUseCase constructor(
    private val userRepository: UserRepository
) {
    /**
     * 执行用例 - 获取当前登录用户信息
     * @return 包含用户信息的Flow
     */
    operator fun invoke(): Flow<Result<UserInfo>> {
        return userRepository.getCurrentUserInfo()
    }
    
    /**
     * 执行用例 - 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 包含用户信息的Flow
     */
    operator fun invoke(userId: String): Flow<Result<UserInfo>> {
        return userRepository.getUserInfoById(userId)
    }
}
