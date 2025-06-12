package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.local.dao.BrowsingHistoryDao
import com.sdu.novaglide.data.local.entity.BrowsingHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date

class BrowsingHistoryRepositoryImpl(
    private val browsingHistoryDao: BrowsingHistoryDao
) : BrowsingHistoryRepository {

    private val MAX_HISTORY_COUNT = 100

    override suspend fun addHistory(userId: String, newsId: String, newsTitle: String) { // 添加 newsTitle 参数
        withContext(Dispatchers.IO) {
            browsingHistoryDao.upsertHistory(userId, newsId, newsTitle, Date()) // 传递 newsTitle

            val currentCount = browsingHistoryDao.countHistoryByUserId(userId)
            if (currentCount > MAX_HISTORY_COUNT) {
                browsingHistoryDao.deleteOldestHistoryByUserId(userId)
            }
        }
    }

    override fun getHistory(userId: String): Flow<List<BrowsingHistoryEntity>> {
        return browsingHistoryDao.getHistoryByUserId(userId)
    }
}
