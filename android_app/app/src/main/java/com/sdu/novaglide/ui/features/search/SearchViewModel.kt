package com.sdu.novaglide.ui.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.data.local.entity.SearchHistoryEntity
import com.sdu.novaglide.data.repository.SearchHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 搜索界面ViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    
    // 搜索历史
    val searchHistory: StateFlow<List<SearchHistoryEntity>> = _currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            searchHistoryRepository.getUserSearchHistory(userId, 20)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 热门搜索
    val hotSearches: StateFlow<List<SearchHistoryEntity>> = searchHistoryRepository
        .getHotSearches(10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 搜索建议（基于搜索历史的筛选）
    val searchSuggestions: StateFlow<List<String>> = combine(
        searchQuery,
        searchHistory
    ) { query, history ->
        if (query.isBlank()) {
            emptyList()
        } else {
            history
                .filter { it.searchQuery.contains(query, ignoreCase = true) }
                .map { it.searchQuery }
                .distinct()
                .take(5)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    /**
     * 设置当前用户ID
     */
    fun setCurrentUserId(userId: String) {
        _currentUserId.value = userId
    }
    
    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * 执行搜索并添加到历史记录
     */
    fun performSearch(query: String): String {
        if (query.isBlank()) return ""
        
        val trimmedQuery = query.trim()
        _searchQuery.value = trimmedQuery
        
        // 添加到搜索历史
        _currentUserId.value?.let { userId ->
            android.util.Log.d("SearchViewModel", "添加搜索历史: userId=$userId, query=$trimmedQuery")
            viewModelScope.launch {
                searchHistoryRepository.addSearchHistory(userId, trimmedQuery)
                android.util.Log.d("SearchViewModel", "搜索历史已添加")
            }
        } ?: run {
            android.util.Log.w("SearchViewModel", "用户ID为空，无法添加搜索历史")
        }
        
        return trimmedQuery
    }
    
    /**
     * 删除搜索历史记录
     */
    fun deleteSearchHistory(query: String) {
        _currentUserId.value?.let { userId ->
            viewModelScope.launch {
                searchHistoryRepository.deleteSearchHistory(userId, query)
            }
        }
    }
    
    /**
     * 清空所有搜索历史
     */
    fun clearAllSearchHistory() {
        _currentUserId.value?.let { userId ->
            viewModelScope.launch {
                searchHistoryRepository.clearUserSearchHistory(userId)
            }
        }
    }
    
    /**
     * 获取默认热门搜索关键词
     */
    fun getDefaultHotSearches(): List<String> {
        return listOf(
            "保研经验分享",
            "考研复习计划",
            "留学申请攻略",
            "公务员考试技巧",
            "研究生生活",
            "奖学金申请",
            "求职面试",
            "学术论文写作"
        )
    }
    
    /**
     * ViewModel工厂
     */
    class Factory(
        private val searchHistoryRepository: SearchHistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                return SearchViewModel(searchHistoryRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 