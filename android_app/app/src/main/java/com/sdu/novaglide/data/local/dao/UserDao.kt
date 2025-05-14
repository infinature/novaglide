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
     * 获取当前用户（限制为1条记录）
     */
    @Query("SELECT * FROM users LIMIT 1")
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
}
