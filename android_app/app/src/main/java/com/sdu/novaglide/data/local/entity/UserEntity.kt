package com.sdu.novaglide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 用户信息实体类，对应数据库表
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey // 主键注解应在属性声明处
    val userId: String,
    val username: String,
    val nickname: String,
    val email: String,
    val phone: String,
    val avatar: String,
    val bio: String,
    val registrationDate: Date,
    val lastLoginDate: Date,
    val eduLevel: String,
    val institution: String,
    val graduationYear: Int?
)
