package com.sdu.novaglide

import android.app.Application
import android.util.Log
import com.sdu.novaglide.core.database.AppDatabase
import com.sdu.novaglide.data.local.dao.BrowsingHistoryDao
import com.sdu.novaglide.data.local.dao.UserDao
import com.sdu.novaglide.data.local.dao.FavoriteArticleDao
import com.sdu.novaglide.data.local.entity.UserEntity // <--- 检查或添加此导入
import com.sdu.novaglide.data.repository.BrowsingHistoryRepository
import com.sdu.novaglide.data.repository.BrowsingHistoryRepositoryImpl
import com.sdu.novaglide.data.repository.FavoriteArticleRepository
import com.sdu.novaglide.data.repository.FavoriteArticleRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date

private const val TAG = "NovaGlideApplication"

class NovaGlideApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database: AppDatabase by lazy {
        Log.d(TAG, "获取数据库实例")
        AppDatabase.getDatabase(this, applicationScope)
    }

    val userDao: UserDao by lazy {
        Log.d(TAG, "获取UserDao实例")
        database.userDao()
    }

    // Provide BrowsingHistoryDao
    val browsingHistoryDao: BrowsingHistoryDao by lazy {
        Log.d(TAG, "获取BrowsingHistoryDao实例")
        database.browsingHistoryDao()
    }
    
    // Provide BrowsingHistoryRepository
    val browsingHistoryRepository: BrowsingHistoryRepository by lazy {
        Log.d(TAG, "获取BrowsingHistoryRepository实例")
        BrowsingHistoryRepositoryImpl(browsingHistoryDao)
    }
    
    // 提供 FavoriteArticleDao
    val favoriteArticleDao: FavoriteArticleDao by lazy {
        Log.d(TAG, "获取FavoriteArticleDao实例")
        database.favoriteArticleDao()
    }

    // 提供 FavoriteArticleRepository
    val favoriteArticleRepository: FavoriteArticleRepository by lazy {
        Log.d(TAG, "获取FavoriteArticleRepository实例")
        FavoriteArticleRepositoryImpl(favoriteArticleDao)
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "应用创建")
        initializeDatabase()
    }
    
    private fun initializeDatabase() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始初始化数据库")
                val db = database // 确保数据库被访问以触发创建
                
                // 检查用户数据，如果 createTestUser 被调用，这里可能间接相关
                val user = userDao.getCurrentUser() // userDao.getCurrentUser() 可能返回 UserEntity
                Log.d(TAG, "检查用户数据: ${if (user == null) "未找到用户" else "找到用户 ${user.userId}"}")
                
                if (user == null) {
                    Log.d(TAG, "未找到用户数据，考虑是否创建测试用户或依赖注册流程。")
                    // 如果 createTestUser 方法存在并且你正在使用它，请确保其内部 UserEntity 的使用是正确的。
                    // 为了避免与自动生成的 userId 冲突并确保从登录页开始，通常建议注释掉此方法。
                    // createTestUser() 
                }
                
                Log.d(TAG, "数据库初始化完成")
            } catch (e: Exception) {
                Log.e(TAG, "初始化数据库失败: ${e.message}", e)
                // 可以在这里处理错误，例如显示一个通知或记录到更持久的日志
            }
        }
    }
    
    // 如果您有 createTestUser 方法，并且它在第82行附近，请检查它：
    /*
    private suspend fun createTestUser() {
        try {
            val currentTime = System.currentTimeMillis()
            // 确保 UserEntity 被正确引用和导入
            val demoUser = UserEntity(  // <--- 如果这是第82行附近，确保 UserEntity 已导入
                userId = "U_test_001", 
                username = "testuser",
                nickname = "测试用户",
                email = "test@example.com",
                password = "password", // 注意：密码应被哈希处理
                phone = "1234567890",
                avatar = "",
                bio = "这是一个测试用户。",
                registrationDate = Date(currentTime - 86400000L * 5), // 5 days ago
                lastLoginDate = Date(currentTime),
                eduLevel = "本科",
                institution = "测试大学",
                graduationYear = 2023
            )
            // userDao.insertUser(demoUser) // 假设 insertUser 接受 UserEntity
            // Log.d(TAG, "测试用户创建成功: ${demoUser.userId}")
            Log.d(TAG, "createTestUser: 此方法当前被注释掉或需要检查 UserEntity 的使用。")
        } catch (e: Exception) {
            Log.e(TAG, "创建测试用户失败: ${e.message}", e)
        }
    }
    */
}