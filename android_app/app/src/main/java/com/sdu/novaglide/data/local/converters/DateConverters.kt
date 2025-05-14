package com.sdu.novaglide.data.local.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room数据库类型转换器，用于处理Date类型
 */
class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
