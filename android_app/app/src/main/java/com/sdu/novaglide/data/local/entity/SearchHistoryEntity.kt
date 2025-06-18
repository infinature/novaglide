package com.sdu.novaglide.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 搜索历史实体
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "search_query")
    val searchQuery: String,
    
    @ColumnInfo(name = "search_time")
    val searchTime: Date,
    
    @ColumnInfo(name = "search_count")
    val searchCount: Int = 1 // 搜索次数，用于热门搜索排序
) 