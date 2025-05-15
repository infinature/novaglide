package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.remote.dto.toNews
import com.sdu.novaglide.data.remote.service.NewsApiService
import com.sdu.novaglide.domain.model.News
import com.sdu.novaglide.domain.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class NewsRepositoryImpl(
    private val newsApiService: NewsApiService
) : NewsRepository {
    
    override suspend fun getNewsByCategory(
        category: String,
        page: Int,
        pageSize: Int
    ): Result<Pair<List<News>, Boolean>> = withContext(Dispatchers.IO) {
        try {
            val response = newsApiService.getNewsList(category, page, pageSize)
            val news = response.news.map { it.toNews() }
            val hasMorePages = response.nextPage != null
            Result.success(Pair(news, hasMorePages))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchNews(
        query: String,
        category: String?,
        page: Int,
        pageSize: Int
    ): Result<Pair<List<News>, Boolean>> = withContext(Dispatchers.IO) {
        try {
            val response = newsApiService.searchNews(query, category, page, pageSize)
            val news = response.news.map { it.toNews() }
            val hasMorePages = response.nextPage != null
            Result.success(Pair(news, hasMorePages))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
