package com.sdu.novaglide.domain.model

import java.util.Date

data class News(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val source: String,
    val publishDate: Date,
    val category: String,
    val imageUrl: String? = null
)
