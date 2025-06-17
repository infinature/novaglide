package com.sdu.novaglide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey
    val articleId: String,
    val title: String,
    val contentSnippet: String, // A short snippet or summary
    val category: String,       // e.g., Technology, Sports, Health
    val publishDate: Long,      // Store as Long (timestamp)
    val imageUrl: String?,
    val source: String?,
    val recommendationScore: Double // For simple weighted recommendation
)
