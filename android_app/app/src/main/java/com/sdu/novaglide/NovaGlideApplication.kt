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
                val db = database
                
                val user = userDao.getCurrentUser()
                Log.d(TAG, "检查用户数据: ${if (user == null) "未找到用户" else "找到用户 ${user.userId}"}")
                
                // 如果您希望从一个干净的数据库开始，并且第一个注册用户是 U000001，
                // 可以考虑注释掉 createTestUser() 的调用。
                // 或者，如果 AppDatabase 的 Room.databaseBuilder().addCallback 中有预填充逻辑，
                // 也需要检查那里的 userId 是否符合新规则。
                if (user == null) {
                    Log.d(TAG, "未找到用户数据，考虑是否创建测试用户或依赖注册流程。")
                    // createTestUser() // <--- 考虑是否需要这个测试用户
                }
                
                Log.d(TAG, "数据库初始化完成")
            } catch (e: Exception) {
                Log.e(TAG, "初始化数据库失败: ${e.message}", e)
            }
        }
    }
    
    private suspend fun createTestUser() {
        // 如果保留此方法，请确保 userId "U12345678" 不会与新的 "U%06d" 格式冲突，
        // 或者修改它以适应新格式，例如，如果这是唯一的预置用户，可以设为 "U000000" 或其他特殊值。
        // 为了确保第一个注册用户是 U000001，最好在数据库为空时，不创建此测试用户。
        try {
            val currentTime = System.currentTimeMillis()
            val demoUser = UserEntity(
                userId = "U12345678", // 这个ID与新的自增逻辑可能不兼容
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
                graduationYear = 2025,
                password = "password123"
            )
            // userDao.insertUser(demoUser)
            // Log.d(TAG, "测试用户创建成功: ${demoUser.userId}，密码: ${demoUser.password}")
            Log.d(TAG, "createTestUser: 已被注释或需要调整以适应新的userId逻辑。")
        } catch (e: Exception) {
            Log.e(TAG, "创建测试用户失败: ${e.message}", e)
        }
    }
}