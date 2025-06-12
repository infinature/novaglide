package com.sdu.novaglide.data.mapper

import com.sdu.novaglide.data.local.entity.UserEntity
import com.sdu.novaglide.domain.model.UserInfo
import java.util.Date // 确保导入Date

/**
 * 用户数据映射器，负责实体类与领域模型之间的转换
 */
object UserMapper {
    
    /**
     * 将实体类转换为领域模型
     */
    fun mapEntityToDomain(entity: UserEntity): UserInfo {
        return UserInfo(
            userId = entity.userId,
            username = entity.username, 
            nickname = entity.nickname,
            email = entity.email,
            phone = entity.phone,
            avatar = entity.avatar,
            bio = entity.bio,
            registrationDate = entity.registrationDate,
            lastLoginDate = entity.lastLoginDate,
            eduLevel = entity.eduLevel,
            institution = entity.institution,
            graduationYear = entity.graduationYear
        )
    }
    
    /**
     * 将领域模型转换为实体类
     */
    fun mapDomainToEntity(domain: UserInfo): UserEntity {
        // 警告: 此处为密码设置了空字符串。
        // 如果此方法用于创建新用户，密码将为空。
        // 如果用于更新用户，并且希望保留现有密码，则应采用不同策略，
        // 例如，先加载现有实体，然后更新其字段。
        return UserEntity(
            userId = domain.userId,
            username = domain.username,
            nickname = domain.nickname,
            email = domain.email,
            phone = domain.phone,
            avatar = domain.avatar,
            bio = domain.bio,
            registrationDate = domain.registrationDate,
            lastLoginDate = domain.lastLoginDate,
            eduLevel = domain.eduLevel,
            institution = domain.institution,
            graduationYear = domain.graduationYear,
            password = "" // <--- 为 password 提供一个值
        )
    }
}
