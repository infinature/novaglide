package com.sdu.novaglide.data.local.dao

import androidx.room.*
import com.sdu.novaglide.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据访问对象(DAO)
 * 定义了对用户表的所有数据库操作
 */
@Dao
interface UserDao {
    /**
     * 插入一个用户，如果存在则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    /**
     * 更新用户信息
     */
    @Update
    suspend fun updateUser(user: UserEntity)
    
    /**
     * 根据ID查询用户（挂起函数版本）
     */
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    /**
     * 根据ID查询用户（Flow版本，用于观察变化）
     */
    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUserByIdAsFlow(userId: String): Flow<UserEntity?>
    
    /**
     * 获取当前用户（查询 isLoggedIn = 1 的用户）
     */
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1") // 修改查询条件
    suspend fun getCurrentUser(): UserEntity?
    
    /**
     * 获取所有用户（Flow版本，用于观察变化）
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    /**
     * 根据ID删除用户
     */
    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)

    /**
     * 插入多个用户，如果已存在则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<UserEntity>)

    /**
     * 检查数据库中是否有数据
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    /**
     * 根据用户名获取用户信息
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    /**
     * 获取所有用户ID中数字部分最大的那个ID。
     * 假设 userId 格式为 "U" + 数字，例如 "U000001".
     * SUBSTR(userId, 2) 提取数字部分。
     * CAST(SUBSTR(userId, 2) AS INTEGER) 将其转换为整数进行比较。
     */
    @Query("SELECT userId FROM users WHERE userId LIKE 'U%' ORDER BY CAST(SUBSTR(userId, 2) AS INTEGER) DESC LIMIT 1")
    suspend fun getMaxUserId(): String?

    /**
     * 更新指定用户的登录状态
     */
    @Query("UPDATE users SET isLoggedIn = :isLoggedIn WHERE userId = :userId")
    suspend fun updateLoginStatus(userId: String, isLoggedIn: Boolean)

    /**
     * 将所有用户的登录状态isLoggedIn设置为false
     */
    @Query("UPDATE users SET isLoggedIn = 0") // 0 代表 false
    suspend fun clearAllLoginStatus()

    // 如果需要更新用户信息（例如，在UserMapper中不处理密码时，在UserRepositoryImpl中更新）
    // @Update
    // suspend fun updateUser(user: UserEntity)

    // 如果需要Flow版本的getUserById (之前在UserRepositoryImpl中被注释掉了)
    // @Query("SELECT * FROM users WHERE userId = :userId")
    // fun getUserByIdAsFlow(userId: String): Flow<UserEntity?>
}
