package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.local.dao.NewsArticleDao
import com.sdu.novaglide.data.mapper.NewsArticleMapper
import com.sdu.novaglide.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface NewsArticleRepository {
    fun getRecommendedArticles(): Flow<List<NewsArticle>>
    fun getArticlesByCategory(category: String): Flow<List<NewsArticle>>
    suspend fun getArticleById(articleId: String): NewsArticle?
}

class NewsArticleRepositoryImpl(
    private val newsArticleDao: NewsArticleDao
) : NewsArticleRepository {

    override fun getRecommendedArticles(): Flow<List<NewsArticle>> {
        return newsArticleDao.getAllArticlesSortedByScore()
            .map { entities -> NewsArticleMapper.mapEntityListToDomainList(entities) }
    }

    override fun getArticlesByCategory(category: String): Flow<List<NewsArticle>> {
        return newsArticleDao.getArticlesByCategorySortedByScore(category)
            .map { entities -> NewsArticleMapper.mapEntityListToDomainList(entities) }
    }

    override suspend fun getArticleById(articleId: String): NewsArticle? {
        return newsArticleDao.getArticleById(articleId)?.let {
            NewsArticleMapper.mapEntityToDomain(it)
        }
    }
}
