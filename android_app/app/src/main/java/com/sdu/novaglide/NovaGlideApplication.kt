package com.sdu.novaglide

import android.app.Application
import android.util.Log
import com.sdu.novaglide.core.database.AppDatabase
import com.sdu.novaglide.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date

private const val TAG = "NovaGlideApplication"

class NovaGlideApplication : Application() {
    // 应用程序范围的协程作用域
    val applicationScope = CoroutineScope(SupervisorJob())
    
    // 使用lazy但改进错误处理，不立即抛出异常，因为这可能导致应用崩溃
    val database by lazy { 
        try {
            Log.i(TAG, "初始化数据库")
            AppDatabase.getDatabase(this, applicationScope)
        } catch (e: Exception) {
            Log.e(TAG, "数据库初始化失败: ${e.message}", e)
            // 在生产应用中，考虑实现后备策略而不是重新抛出
            // 但对于教育目的，我们允许它崩溃以便找出根本原因
            throw e
        }
    }
    
    // 使用by lazy懒加载UserDao
    val userDao by lazy { database.userDao() }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "应用启动")
        
        // 初始化数据库并填充测试数据
        initializeDatabase()
    }
    
    private fun initializeDatabase() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始初始化数据库")
                // 触发数据库的懒加载初始化
                val db = database
                
                // 检查是否有用户数据，没有则创建测试用户
                // 这部分可能是多余的，因为Room的Callback会在创建数据库时自动插入测试用户
                // 但保留这个逻辑作为双重保险
                val user = userDao.getCurrentUser()
                Log.d(TAG, "检查用户数据: ${if (user == null) "未找到用户" else "找到用户 ${user.userId}"}")
                
                if (user == null) {
                    Log.d(TAG, "未找到用户数据，创建测试用户")
                    createTestUser()
                }
                
                Log.d(TAG, "数据库初始化完成")
            } catch (e: Exception) {
                Log.e(TAG, "初始化数据库失败: ${e.message}", e)
            }
        }
    }
    
    private suspend fun createTestUser() {
        try {
            val currentTime = System.currentTimeMillis()
            val demoUser = UserEntity(
                userId = "U12345678",
                username = "student2024",
                nickname = "学习达人",
                email = "student2024@example.com",
                phone = "138****1234",
                avatar = "",
                bio = "热爱学习，备战考研",
                registrationDate = Date(currentTime - 90 * 24 * 60 * 60 * 1000L),
                lastLoginDate = Date(currentTime),
                eduLevel = "本科",
                institution = "山东大学",
                graduationYear = 2025
            )
            userDao.insertUser(demoUser)
            Log.d(TAG, "测试用户创建成功: ${demoUser.userId}")
        } catch (e: Exception) {
            Log.e(TAG, "创建测试用户失败: ${e.message}", e)
        }
    }
}