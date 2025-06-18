package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.local.dao.SearchHistoryDao
import com.sdu.novaglide.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

/**
 * 搜索历史仓储实现
 */
class SearchHistoryRepositoryImpl(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {
    
    override suspend fun addSearchHistory(userId: String, query: String) {
        android.util.Log.d("SearchHistoryRepo", "开始添加搜索历史: userId=$userId, query='$query'")
        
        if (query.isBlank()) {
            android.util.Log.w("SearchHistoryRepo", "查询为空，跳过添加")
            return
        }
        
        val currentTime = Date()
        
        // 检查是否已存在相同的查询
        val existsCount = searchHistoryDao.searchExists(userId, query)
        android.util.Log.d("SearchHistoryRepo", "已存在相同查询数量: $existsCount")
        
        if (existsCount > 0) {
            // 更新现有记录的时间和搜索次数
            android.util.Log.d("SearchHistoryRepo", "更新现有搜索记录")
            searchHistoryDao.updateSearchCount(userId, query, currentTime)
        } else {
            // 创建新记录
            android.util.Log.d("SearchHistoryRepo", "创建新搜索记录")
            val searchHistory = SearchHistoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                searchQuery = query,
                searchTime = currentTime,
                searchCount = 1
            )
            searchHistoryDao.insertSearchHistory(searchHistory)
        }
        
        // 清理旧记录，只保留最近的50条
        android.util.Log.d("SearchHistoryRepo", "清理旧记录")
        searchHistoryDao.deleteOldSearchHistory(userId, 50)
        
        android.util.Log.d("SearchHistoryRepo", "搜索历史添加完成")
    }
    
    override fun getUserSearchHistory(userId: String, limit: Int): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getUserSearchHistory(userId, limit)
    }
    
    override fun getHotSearches(limit: Int): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getHotSearches(limit)
    }
    
    override suspend fun deleteSearchHistory(userId: String, query: String) {
        searchHistoryDao.deleteSearchHistory(userId, query)
    }
    
    override suspend fun clearUserSearchHistory(userId: String) {
        searchHistoryDao.clearUserSearchHistory(userId)
    }
} 