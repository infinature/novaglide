package com.sdu.novaglide.domain.model

import java.util.Date

/**
 * 用户信息领域模型
 */
data class UserInfo(
    val userId: String,
    val username: String,
    val nickname: String,
    val email: String,
    val phone: String,
    val avatar: String, // 头像URL
    val bio: String, // 个人简介
    val registrationDate: Date,
    val lastLoginDate: Date,
    val eduLevel: String, // 学历级别
    val institution: String, // 教育机构/学校
    val graduationYear: Int? // 毕业年份
)
