package com.sdu.novaglide.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sdu.novaglide.data.local.entity.FavoriteArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteArticleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // 如果已收藏则忽略
    suspend fun insertFavorite(favorite: FavoriteArticleEntity): Long

    @Query("DELETE FROM favorite_articles WHERE userId = :userId AND newsId = :newsId")
    suspend fun deleteFavorite(userId: String, newsId: String)

    @Query("SELECT * FROM favorite_articles WHERE userId = :userId AND newsId = :newsId LIMIT 1")
    fun getFavorite(userId: String, newsId: String): Flow<FavoriteArticleEntity?>

    @Query("SELECT * FROM favorite_articles WHERE userId = :userId ORDER BY favoritedAt DESC")
    fun getFavoriteArticlesByUserId(userId: String): Flow<List<FavoriteArticleEntity>>
}
