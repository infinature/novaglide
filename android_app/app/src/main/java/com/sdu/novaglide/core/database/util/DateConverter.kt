package com.sdu.novaglide.core.database.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room 类型转换器，用于在 Date 和 Long (时间戳) 之间进行转换。
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
