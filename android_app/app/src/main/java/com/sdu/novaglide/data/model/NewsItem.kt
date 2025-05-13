package com.sdu.novaglide.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class NewsItem(
    val id: String, // Unique ID for navigation/database
    val title: String,
    val summary: String,
    val source: String,
    val publishedAt: String, // Or a Date/Timestamp object depending on API and parsing
    val category: String,
    val imageUrl: String?, // Optional image URL
    val articleUrl: String // Link to the full article
) 