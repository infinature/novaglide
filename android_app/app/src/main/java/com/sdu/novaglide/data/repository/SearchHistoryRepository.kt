package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 搜索历史仓储接口
 */
interface SearchHistoryRepository {
    
    /**
     * 添加搜索历史记录
     */
    suspend fun addSearchHistory(userId: String, query: String)
    
    /**
     * 获取用户的搜索历史记录
     */
    fun getUserSearchHistory(userId: String, limit: Int = 20): Flow<List<SearchHistoryEntity>>
    
    /**
     * 获取热门搜索
     */
    fun getHotSearches(limit: Int = 10): Flow<List<SearchHistoryEntity>>
    
    /**
     * 删除搜索历史记录
     */
    suspend fun deleteSearchHistory(userId: String, query: String)
    
    /**
     * 清空用户的所有搜索历史
     */
    suspend fun clearUserSearchHistory(userId: String)
} 