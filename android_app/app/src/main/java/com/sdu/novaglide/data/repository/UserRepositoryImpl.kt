package com.sdu.novaglide.data.repository

import android.util.Log
import com.sdu.novaglide.data.local.dao.UserDao
import com.sdu.novaglide.data.local.entity.UserEntity
import com.sdu.novaglide.data.mapper.UserMapper
import com.sdu.novaglide.domain.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map 
import kotlinx.coroutines.withContext
import java.util.Date

private const val TAG = "UserRepositoryImpl"

/**
 * 用户信息仓库实现类
 */
class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    
    override fun getCurrentUserInfo(): Flow<Result<UserInfo>> = flow {
        try {
            val userEntity = userDao.getCurrentUser() // 这现在会获取 isLoggedIn = true 的用户
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
    // fun getUserByIdAsFlow(userId: String): Flow<UserInfo?> {
    //     return userDao.getUserByIdAsFlow(userId) // 假设UserDao有此方法
    //         .map { entity -> entity?.let { UserMapper.mapEntityToDomain(it) } }
    //         .flowOn(Dispatchers.IO)
    // }
    
    override suspend fun getUserByUsername(username: String): UserInfo? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByUsername(username)?.let {
                UserMapper.mapEntityToDomain(it)
            }
        }
    }

    override suspend fun registerUser(userEntity: UserEntity): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 再次检查用户名是否已存在，以防并发问题（虽然ViewModel层已检查）
                val existing = userDao.getUserByUsername(userEntity.username)
                if (existing != null) {
                    Log.w(TAG, "注册失败，用户名 ${userEntity.username} 已存在于数据库")
                    return@withContext false
                }
                userDao.insertUser(userEntity) // UserEntity 已包含密码
                Log.d(TAG, "用户 ${userEntity.username} 注册成功并已存入数据库")
                true
            } catch (e: Exception) {
                Log.e(TAG, "注册用户 ${userEntity.username} 时发生数据库错误", e)
                false
            }
        }
    }
    
    override suspend fun saveUserInfo(userInfo: UserInfo) {
        withContext(Dispatchers.IO) {
            // 此方法现在假设用于更新或插入，但不处理密码的初始设置。
            // 如果是新用户，密码会是 UserMapper.mapDomainToEntity 中的默认值（空字符串）。
            // 强烈建议新用户通过 registerUser 方法创建。
            val existingEntity = userDao.getUserById(userInfo.userId)
            val passwordToSave = existingEntity?.password ?: "" // 保留旧密码，或为空（不推荐）

            val userEntity = UserMapper.mapDomainToEntity(userInfo).copy(password = passwordToSave)
            userDao.insertUser(userEntity)
            Log.d(TAG, "用户信息已保存/更新: ${userInfo.username}")
        }
    }
    
    override suspend fun updateUserInfo(userInfo: UserInfo): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val existingEntity = userDao.getUserById(userInfo.userId)
                if (existingEntity == null) {
                    Log.w(TAG, "更新失败，未找到用户ID: ${userInfo.userId}")
                    return@withContext false
                }
                // 创建新的Entity，只更新UserInfo中的字段，密码保持不变
                val updatedEntity = existingEntity.copy(
                    nickname = userInfo.nickname,
                    email = userInfo.email,
                    phone = userInfo.phone,
                    avatar = userInfo.avatar, // 如果头像也允许编辑，需要处理
                    bio = userInfo.bio,
                    eduLevel = userInfo.eduLevel,
                    institution = userInfo.institution,
                    graduationYear = userInfo.graduationYear
                    // username 和 password 字段保持 existingEntity 的不变
                    // lastLoginDate 通常在登录时更新
                    // registrationDate 是固定的
                )
                userDao.insertUser(updatedEntity) // insertUser会替换现有用户 (OnConflictStrategy.REPLACE)
                Log.d(TAG, "用户信息已更新: ${userInfo.username}，密码未更改")
                true
            } catch (e: Exception) {
                Log.e(TAG, "更新用户信息 ${userInfo.username} 时发生数据库错误", e)
                false
            }
        }
    }
    
    override suspend fun login(username: String, password: String): UserInfo? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "尝试登录，用户名: $username")
                val userEntity = userDao.getUserByUsername(username)
                if (userEntity != null) {
                    // ！！！重要安全警告：此处为明文密码比较，极不安全！！！
                    // ！！！在生产环境中，应比较哈希后的密码！！！
                    if (userEntity.password == password) {
                        Log.d(TAG, "密码验证成功，用户: ${userEntity.username}")
                        // 更新最后登录时间
                        val updatedUserEntity = userEntity.copy(lastLoginDate = Date())
                        userDao.insertUser(updatedUserEntity)
                        Log.d(TAG, "已更新用户 ${userEntity.username} 的最后登录时间")
                        // 映射到UserInfo领域模型（不包含密码）
                        UserInfo(
                            userId = userEntity.userId,
                            username = userEntity.username,
                            nickname = userEntity.nickname,
                            email = userEntity.email,
                            phone = userEntity.phone,
                            avatar = userEntity.avatar,
                            bio = userEntity.bio,
                            registrationDate = userEntity.registrationDate,
                            lastLoginDate = updatedUserEntity.lastLoginDate, // 使用更新后的时间
                            eduLevel = userEntity.eduLevel,
                            institution = userEntity.institution,
                            graduationYear = userEntity.graduationYear
                        )
                    } else {
                        Log.w(TAG, "密码错误，用户: $username")
                        null // 密码不匹配
                    }
                } else {
                    Log.w(TAG, "未找到用户: $username")
                    null // 用户不存在
                }
            } catch (e: Exception) {
                Log.e(TAG, "登录时发生错误，用户: $username", e)
                null // 发生异常
            }
        }
    }

    override suspend fun getMaxUserId(): String? {
        return withContext(Dispatchers.IO) {
            userDao.getMaxUserId()
        }
    }

    // 实现新增的方法
    override suspend fun updateLoginStatus(userId: String, isLoggedIn: Boolean) {
        withContext(Dispatchers.IO) {
            userDao.updateLoginStatus(userId, isLoggedIn)
            Log.d(TAG, "用户 $userId 的登录状态更新为: $isLoggedIn")
        }
    }

    override suspend fun clearAllLoginStatus() {
        withContext(Dispatchers.IO) {
            userDao.clearAllLoginStatus()
            Log.d(TAG, "所有用户的登录状态已清除")
        }
    }
}
