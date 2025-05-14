package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.local.dao.UserDao
import com.sdu.novaglide.data.mapper.UserMapper
import com.sdu.novaglide.domain.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * 用户信息仓库实现类
 */
class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    
    override fun getCurrentUserInfo(): Flow<Result<UserInfo>> = flow {
        try {
            val userEntity = userDao.getCurrentUser()
            if (userEntity != null) {
                val userInfo = UserMapper.mapEntityToDomain(userEntity)
                emit(Result.success(userInfo))
            } else {
                emit(Result.failure(Exception("未找到用户信息")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    override fun getUserInfoById(userId: String): Flow<Result<UserInfo>> = flow {
        try {
            val userEntity = userDao.getUserById(userId)
            if (userEntity != null) {
                val userInfo = UserMapper.mapEntityToDomain(userEntity)
                emit(Result.success(userInfo))
            } else {
                emit(Result.failure(Exception("未找到用户")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    // 新增方法：使用Flow直接获取并转换用户信息
    fun getUserByIdAsFlow(userId: String): Flow<UserInfo?> {
        return userDao.getUserByIdAsFlow(userId)
            .map { entity -> entity?.let { UserMapper.mapEntityToDomain(it) } }
            .flowOn(Dispatchers.IO)
    }
    
    // 新增方法：保存用户信息
    suspend fun saveUserInfo(userInfo: UserInfo) {
        val userEntity = UserMapper.mapDomainToEntity(userInfo)
        userDao.insertUser(userEntity)
    }
    
    // 新增方法：更新用户信息
    suspend fun updateUserInfo(userInfo: UserInfo) {
        val userEntity = UserMapper.mapDomainToEntity(userInfo)
        userDao.updateUser(userEntity)
    }
}
