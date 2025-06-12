package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.local.entity.UserEntity
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

    /**
     * 尝试用户登录
     * @param username 用户名
     * @param password 密码
     * @return 如果登录成功，返回UserInfo；否则返回null。
     */
    suspend fun login(username: String, password: String): UserInfo?

    /**
     * 根据用户名获取用户信息（主要用于检查用户名是否存在）
     * @param username 用户名
     * @return UserInfo 如果找到，否则 null
     */
    suspend fun getUserByUsername(username: String): UserInfo?

    /**
     * 注册新用户
     * @param userEntity 包含所有用户信息（包括密码）的实体
     * @return Boolean 表示注册是否成功
     */
    suspend fun registerUser(userEntity: UserEntity): Boolean

    /**
     * 更新用户信息
     * @param userInfo 要更新的用户领域模型（不含密码）
     * @return Boolean 表示更新是否成功
     */
    suspend fun updateUserInfo(userInfo: UserInfo): Boolean

    /**
     * 保存用户信息（可以是新建或更新）
     * 如果是新建，需要确保密码被正确处理。
     * 建议使用 registerUser 处理新用户创建。
     * @param userInfo 用户领域模型
     */
    suspend fun saveUserInfo(userInfo: UserInfo)

    /**
     * 获取当前数据库中最大的用户ID（格式如 "U000001"）
     */
    suspend fun getMaxUserId(): String?
}
