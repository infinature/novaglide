package com.sdu.novaglide.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sdu.novaglide.data.local.entity.NewsArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: NewsArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllArticles(articles: List<NewsArticleEntity>)

    @Query("SELECT * FROM news_articles ORDER BY recommendationScore DESC")
    fun getAllArticlesSortedByScore(): Flow<List<NewsArticleEntity>>

    @Query("SELECT * FROM news_articles WHERE category = :category ORDER BY recommendationScore DESC")
    fun getArticlesByCategorySortedByScore(category: String): Flow<List<NewsArticleEntity>>

    @Query("SELECT * FROM news_articles WHERE articleId = :articleId")
    suspend fun getArticleById(articleId: String): NewsArticleEntity?

    @Query("DELETE FROM news_articles")
    suspend fun clearAllArticles()
}
