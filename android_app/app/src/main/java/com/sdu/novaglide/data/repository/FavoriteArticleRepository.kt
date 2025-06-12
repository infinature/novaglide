package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.local.entity.FavoriteArticleEntity
import kotlinx.coroutines.flow.Flow

interface FavoriteArticleRepository {
    suspend fun addFavorite(userId: String, newsId: String, newsTitle: String)
    suspend fun removeFavorite(userId: String, newsId: String)
    fun isFavorite(userId: String, newsId: String): Flow<Boolean>
    fun getFavorites(userId: String): Flow<List<FavoriteArticleEntity>>
}
