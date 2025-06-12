package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.local.entity.BrowsingHistoryEntity
import kotlinx.coroutines.flow.Flow

interface BrowsingHistoryRepository {
    suspend fun addHistory(userId: String, newsId: String, newsTitle: String) // 添加 newsTitle 参数
    fun getHistory(userId: String): Flow<List<BrowsingHistoryEntity>>
}
