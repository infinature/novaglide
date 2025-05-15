package com.sdu.novaglide.data.remote.dto

import com.sdu.novaglide.domain.model.News
import java.util.Date

data class NewsDto(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val source: String,
    val publishDate: String,
    val category: String,
    val imageUrl: String?
)

fun NewsDto.toNews(): News {
    return News(
        id = id,
        title = title,
        summary = summary,
        content = content,
        source = source,
        publishDate = Date(publishDate.toLongOrNull() ?: 0),
        category = category,
        imageUrl = imageUrl
    )
}

data class NewsListResponse(
    val news: List<NewsDto>,
    val totalCount: Int,
    val nextPage: Int?
)
