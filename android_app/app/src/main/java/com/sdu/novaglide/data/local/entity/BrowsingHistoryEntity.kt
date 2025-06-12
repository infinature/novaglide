package com.sdu.novaglide.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "browsing_history",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Ensures that a user cannot have the same newsId recorded multiple times.
    // If a user views the same news again, the existing record's 'viewedAt' should be updated.
    indices = [Index(value = ["userId", "newsId"], unique = true)]
)
data class BrowsingHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val newsId: String,
    val newsTitle: String, // 新增字段：新闻标题
    val viewedAt: Date
)
