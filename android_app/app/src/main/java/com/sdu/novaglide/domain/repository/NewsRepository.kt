package com.sdu.novaglide.domain.repository

import com.sdu.novaglide.domain.model.News
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getNewsByCategory(category: String, page: Int, pageSize: Int): Result<Pair<List<News>, Boolean>>
    suspend fun searchNews(query: String, category: String?, page: Int, pageSize: Int): Result<Pair<List<News>, Boolean>>
}
