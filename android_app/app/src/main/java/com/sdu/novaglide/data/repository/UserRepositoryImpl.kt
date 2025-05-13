package com.sdu.novaglide.data.repository

// UserDao的导入暂时保留，但构造函数中的参数被注释掉了
// import com.sdu.novaglide.data.local.dao.UserDao // 暂时注释掉未使用的导入
import com.sdu.novaglide.domain.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Date
// 移除了 javax.inject.Inject 的导入，因为它不再被使用

/**
 * 用户信息仓库实现类
 */
// 移除了 @Inject 注解，因为我们暂时不使用Hilt
class UserRepositoryImpl( // 移除了不必要的 "constructor" 关键字
    // private val userDao: UserDao, // 实际项目中注入DAO
    // private val userApiService: UserApiService // 实际项目中注入API服务
) : UserRepository {
    
    override fun getCurrentUserInfo(): Flow<Result<UserInfo>> = flow {
        try {
            // 模拟网络延迟
            delay(800)
            
            // TODO: 实际项目中应从本地数据库或远程API获取
            // val userEntity = userDao.getCurrentUser()
            // val userInfo = mapEntityToDomain(userEntity)
            
            // 模拟数据
            val userInfo = UserInfo(
                userId = "U12345678",
                username = "student2024",
                nickname = "学习达人",
                email = "student2024@example.com",
                phone = "138****1234",
                avatar = "", // 实际应用中应该有头像URL
                bio = "热爱学习，备战考研",
                registrationDate = Date(System.currentTimeMillis() - 90 * 24 * 60 * 60 * 1000L), // 90天前注册
                lastLoginDate = Date(),
                eduLevel = "本科",
                institution = "山东大学",
                graduationYear = 2025
            )
            
            emit(Result.success(userInfo))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    override fun getUserInfoById(userId: String): Flow<Result<UserInfo>> = flow {
        try {
            // 模拟网络延迟
            delay(800)
            
            // TODO: 实际项目中应从本地数据库或远程API获取
            // val userEntity = userDao.getUserById(userId)
            // val userInfo = mapEntityToDomain(userEntity)
            
            // 模拟数据 - 此处简单返回与getCurrentUserInfo相同的数据
            val userInfo = UserInfo(
                userId = userId,
                username = "student2024",
                nickname = "学习达人",
                email = "student2024@example.com",
                phone = "138****1234",
                avatar = "",
                bio = "热爱学习，备战考研",
                registrationDate = Date(System.currentTimeMillis() - 90 * 24 * 60 * 60 * 1000L),
                lastLoginDate = Date(),
                eduLevel = "本科",
                institution = "山东大学",
                graduationYear = 2025
            )
            
            emit(Result.success(userInfo))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
