package com.sdu.novaglide.core.util

import java.util.regex.Pattern

/**
 * 输入验证工具类
 */
object ValidationUtils {

    /**
     * 验证邮箱格式
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    /**
     * 验证手机号格式（中国大陆）
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.isBlank()) return false
        // 中国大陆手机号规则：1开头，第二位为3-9，共11位数字
        val phonePattern = Pattern.compile("^1[3-9]\\d{9}$")
        return phonePattern.matcher(phone).matches()
    }

    /**
     * 验证密码强度
     * 要求：至少8位，包含字母和数字
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }

    /**
     * 验证用户名
     * 要求：3-20位，只能包含字母、数字、下划线
     */
    fun isValidUsername(username: String): Boolean {
        if (username.length < 3 || username.length > 20) return false
        val usernamePattern = Pattern.compile("^[a-zA-Z0-9_]+$")
        return usernamePattern.matcher(username).matches()
    }

    /**
     * 验证昵称
     * 要求：1-20位，不能为空
     */
    fun isValidNickname(nickname: String): Boolean {
        return nickname.isNotBlank() && nickname.length <= 20
    }

    /**
     * 验证简介
     * 要求：不超过200字符
     */
    fun isValidBio(bio: String): Boolean {
        return bio.length <= 200
    }

    /**
     * 验证毕业年份
     * 要求：1900-当前年份+10
     */
    fun isValidGraduationYear(year: Int?): Boolean {
        if (year == null) return true // 可选字段
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return year in 1900..(currentYear + 10)
    }

    /**
     * 获取邮箱验证错误信息
     */
    fun getEmailErrorMessage(email: String): String? {
        return when {
            email.isBlank() -> "请输入邮箱地址"
            !isValidEmail(email) -> "请输入有效的邮箱地址"
            else -> null
        }
    }

    /**
     * 获取手机号验证错误信息
     */
    fun getPhoneErrorMessage(phone: String): String? {
        return when {
            phone.isBlank() -> null // 手机号可以为空
            !isValidPhoneNumber(phone) -> "请输入有效的手机号（11位数字，以1开头）"
            else -> null
        }
    }

    /**
     * 获取密码验证错误信息
     */
    fun getPasswordErrorMessage(password: String): String? {
        return when {
            password.isBlank() -> "请输入密码"
            password.length < 8 -> "密码至少需要8位字符"
            !password.any { it.isLetter() } -> "密码必须包含字母"
            !password.any { it.isDigit() } -> "密码必须包含数字"
            else -> null
        }
    }

    /**
     * 获取用户名验证错误信息
     */
    fun getUsernameErrorMessage(username: String): String? {
        return when {
            username.isBlank() -> "请输入用户名"
            username.length < 3 -> "用户名至少需要3位字符"
            username.length > 20 -> "用户名不能超过20位字符"
            !Pattern.compile("^[a-zA-Z0-9_]+$").matcher(username).matches() -> 
                "用户名只能包含字母、数字和下划线"
            else -> null
        }
    }

    /**
     * 获取昵称验证错误信息
     */
    fun getNicknameErrorMessage(nickname: String): String? {
        return when {
            nickname.isBlank() -> "请输入昵称"
            nickname.length > 20 -> "昵称不能超过20位字符"
            else -> null
        }
    }

    /**
     * 获取简介验证错误信息
     */
    fun getBioErrorMessage(bio: String): String? {
        return when {
            bio.length > 200 -> "简介不能超过200字符"
            else -> null
        }
    }

    /**
     * 获取毕业年份验证错误信息
     */
    fun getGraduationYearErrorMessage(yearStr: String): String? {
        if (yearStr.isBlank()) return null // 可选字段
        val year = yearStr.toIntOrNull()
        return when {
            year == null -> "请输入有效的年份"
            !isValidGraduationYear(year) -> "请输入有效的毕业年份（1900-${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + 10}）"
            else -> null
        }
    }
} 