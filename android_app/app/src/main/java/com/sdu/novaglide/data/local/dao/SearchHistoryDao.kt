package com.sdu.novaglide.data.local.dao

import androidx.room.*
import com.sdu.novaglide.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 搜索历史数据访问对象
 */
@Dao
interface SearchHistoryDao {
    
    /**
     * 插入搜索历史记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(searchHistory: SearchHistoryEntity)
    
    /**
     * 获取用户的搜索历史记录（按时间倒序）
     */
    @Query("SELECT * FROM search_history WHERE user_id = :userId ORDER BY search_time DESC LIMIT :limit")
    fun getUserSearchHistory(userId: String, limit: Int = 20): Flow<List<SearchHistoryEntity>>
    
    /**
     * 获取热门搜索（按搜索次数倒序）
     */
    @Query("SELECT * FROM search_history GROUP BY search_query ORDER BY SUM(search_count) DESC LIMIT :limit")
    fun getHotSearches(limit: Int = 10): Flow<List<SearchHistoryEntity>>
    
    /**
     * 更新搜索次数
     */
    @Query("UPDATE search_history SET search_count = search_count + 1, search_time = :newTime WHERE user_id = :userId AND search_query = :query")
    suspend fun updateSearchCount(userId: String, query: String, newTime: java.util.Date)
    
    /**
     * 检查搜索记录是否存在
     */
    @Query("SELECT COUNT(*) FROM search_history WHERE user_id = :userId AND search_query = :query")
    suspend fun searchExists(userId: String, query: String): Int
    
    /**
     * 删除用户的搜索历史记录
     */
    @Query("DELETE FROM search_history WHERE user_id = :userId AND search_query = :query")
    suspend fun deleteSearchHistory(userId: String, query: String)
    
    /**
     * 清空用户的所有搜索历史
     */
    @Query("DELETE FROM search_history WHERE user_id = :userId")
    suspend fun clearUserSearchHistory(userId: String)
    
    /**
     * 删除旧的搜索记录（保留最近的N条）
     */
    @Query("DELETE FROM search_history WHERE user_id = :userId AND id NOT IN (SELECT id FROM search_history WHERE user_id = :userId ORDER BY search_time DESC LIMIT :keepCount)")
    suspend fun deleteOldSearchHistory(userId: String, keepCount: Int = 50)
} 