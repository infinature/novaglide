package com.sdu.novaglide.data.repository

import com.sdu.novaglide.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * 用户信息仓库接口
 */
interface UserRepository {
    /**
     * 获取当前登录用户信息
     * @return 包含用户信息的Flow
     */
    fun getCurrentUserInfo(): Flow<Result<UserInfo>>
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 包含用户信息的Flow
     */
    fun getUserInfoById(userId: String): Flow<Result<UserInfo>>
}
