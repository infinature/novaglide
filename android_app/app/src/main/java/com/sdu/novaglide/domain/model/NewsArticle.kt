package com.sdu.novaglide.domain.model

import java.util.Date

data class NewsArticle(
    val articleId: String,
    val title: String,
    val contentSnippet: String,
    val category: String,
    val publishDate: Date,
    val imageUrl: String?,
    val source: String?,
    val recommendationScore: Double
)
