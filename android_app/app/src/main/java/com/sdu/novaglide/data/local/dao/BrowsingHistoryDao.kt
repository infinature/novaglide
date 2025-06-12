package com.sdu.novaglide.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sdu.novaglide.data.local.entity.BrowsingHistoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BrowsingHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: BrowsingHistoryEntity): Long

    @Query("SELECT * FROM browsing_history WHERE userId = :userId ORDER BY viewedAt DESC")
    fun getHistoryByUserId(userId: String): Flow<List<BrowsingHistoryEntity>>

    @Query("SELECT COUNT(*) FROM browsing_history WHERE userId = :userId")
    suspend fun countHistoryByUserId(userId: String): Int

    @Query("DELETE FROM browsing_history WHERE userId = :userId AND newsId = :newsId")
    suspend fun deleteHistoryByUserIdAndNewsId(userId: String, newsId: String)

    @Query("DELETE FROM browsing_history WHERE userId = :userId AND id = (SELECT id FROM browsing_history WHERE userId = :userId ORDER BY viewedAt ASC LIMIT 1)")
    suspend fun deleteOldestHistoryByUserId(userId: String)

    // Transaction to ensure atomicity: delete old if exists, then insert new.
    // This handles re-viewing an item by updating its viewedAt timestamp and moving it to the top.
    @Transaction
    suspend fun upsertHistory(userId: String, newsId: String, newsTitle: String, viewedAt: Date) {
        deleteHistoryByUserIdAndNewsId(userId, newsId) // Remove existing record for this newsId
        insertHistory(BrowsingHistoryEntity(userId = userId, newsId = newsId, newsTitle = newsTitle, viewedAt = viewedAt)) // Insert new record
    }
}
