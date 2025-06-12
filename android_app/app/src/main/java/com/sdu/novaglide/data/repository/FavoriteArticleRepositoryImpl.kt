package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.local.dao.FavoriteArticleDao
import com.sdu.novaglide.data.local.entity.FavoriteArticleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date

class FavoriteArticleRepositoryImpl(
    private val favoriteArticleDao: FavoriteArticleDao
) : FavoriteArticleRepository {

    override suspend fun addFavorite(userId: String, newsId: String, newsTitle: String) {
        withContext(Dispatchers.IO) {
            val favorite = FavoriteArticleEntity(
                userId = userId,
                newsId = newsId,
                newsTitle = newsTitle,
                favoritedAt = Date()
            )
            favoriteArticleDao.insertFavorite(favorite)
        }
    }

    override suspend fun removeFavorite(userId: String, newsId: String) {
        withContext(Dispatchers.IO) {
            favoriteArticleDao.deleteFavorite(userId, newsId)
        }
    }

    override fun isFavorite(userId: String, newsId: String): Flow<Boolean> {
        return favoriteArticleDao.getFavorite(userId, newsId).map { it != null }
    }

    override fun getFavorites(userId: String): Flow<List<FavoriteArticleEntity>> {
        return favoriteArticleDao.getFavoriteArticlesByUserId(userId)
    }
}
