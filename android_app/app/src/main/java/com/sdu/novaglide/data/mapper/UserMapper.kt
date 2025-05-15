package com.sdu.novaglide.data.mapper

import com.sdu.novaglide.data.local.entity.UserEntity
import com.sdu.novaglide.domain.model.UserInfo

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
            graduationYear = domain.graduationYear
        )
    }
}
