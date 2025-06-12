package com.sdu.novaglide.ui.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.data.local.entity.BrowsingHistoryEntity
import com.sdu.novaglide.data.repository.BrowsingHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class BrowsingHistoryViewModel(
    private val browsingHistoryRepository: BrowsingHistoryRepository
) : ViewModel() {

    private val TAG = "BrowsingHistoryVM"

    private val _browsingHistoryState = MutableStateFlow<List<BrowsingHistoryEntity>>(emptyList())
    val browsingHistoryState: StateFlow<List<BrowsingHistoryEntity>> = _browsingHistoryState.asStateFlow()

    fun addBrowsingHistory(userId: String, newsId: String, newsTitle: String) { // 添加 newsTitle 参数
        viewModelScope.launch {
            try {
                Log.d(TAG, "Adding browsing history: userId=$userId, newsId=$newsId, title=$newsTitle")
                browsingHistoryRepository.addHistory(userId, newsId, newsTitle) // 传递 newsTitle
            } catch (e: Exception) {
                Log.e(TAG, "Error adding browsing history", e)
            }
        }
    }

    fun loadBrowsingHistory(userId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Loading browsing history for userId=$userId")
            browsingHistoryRepository.getHistory(userId)
                .catch { e ->
                    Log.e(TAG, "Error loading browsing history", e)
                    _browsingHistoryState.value = emptyList() // Emit empty list on error
                }
                .collect { historyList ->
                    _browsingHistoryState.value = historyList
                    Log.d(TAG, "Browsing history loaded: ${historyList.size} items")
                }
        }
    }

    class Factory(
        private val browsingHistoryRepository: BrowsingHistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BrowsingHistoryViewModel::class.java)) {
                return BrowsingHistoryViewModel(browsingHistoryRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for BrowsingHistoryViewModel")
        }
    }
}
