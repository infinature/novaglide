package com.sdu.novaglide.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.data.model.NewsItem
import com.sdu.novaglide.data.repository.NewsRepository
import com.sdu.novaglide.data.repository.Result // Ensure this is the correct Result class
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class to represent the UI state of the HomeScreen
data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val newsItems: List<NewsItem> = emptyList(),
    val error: String? = null,
    val selectedTabIndex: Int = 0,
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val tabs: List<String> = listOf("保研", "考研", "留学", "考公", "推荐") // Moved tabs here
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentNewsList = mutableListOf<NewsItem>()

    init {
        // Load initial data for the default selected tab
        loadNews(isRefresh = false, isLoadMore = false)
    }

    fun selectCategory(tabIndex: Int) {
        if (_uiState.value.selectedTabIndex == tabIndex && _uiState.value.searchQuery.isEmpty()) return // Avoid reload if already selected and not searching

        _uiState.update {
            it.copy(
                selectedTabIndex = tabIndex,
                searchQuery = "", // Clear search query when changing category
                currentPage = 1,
                newsItems = emptyList(), // Clear previous items
                canLoadMore = true,
                error = null
            )
        }
        currentNewsList.clear()
        loadNews(isRefresh = false, isLoadMore = false)
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Optionally trigger search immediately or wait for a submit action
        if (query.length > 2 || query.isEmpty()) { // Trigger search on significant change or clear
             performSearch()
        }
    }
    
    fun performSearch() {
        if (_uiState.value.searchQuery.isBlank()) {
             // If search query is blank, load news for the current category instead
             selectCategory(_uiState.value.selectedTabIndex)
             return
        }
        _uiState.update {
            it.copy(
                currentPage = 1,
                newsItems = emptyList(),
                canLoadMore = true,
                error = null
            )
        }
        currentNewsList.clear()
        loadNews(isRefresh = false, isLoadMore = false, isSearch = true)
    }

    fun refreshNews() {
        _uiState.update {
            it.copy(
                isRefreshing = true,
                currentPage = 1,
                canLoadMore = true,
                error = null
            )
        }
        currentNewsList.clear()
        loadNews(isRefresh = true, isLoadMore = false, isSearch = _uiState.value.searchQuery.isNotBlank())
    }

    fun loadMoreNews() {
        if (_uiState.value.isLoadingMore || !_uiState.value.canLoadMore) return
        _uiState.update { it.copy(isLoadingMore = true, currentPage = it.currentPage + 1, error = null) }
        loadNews(isRefresh = false, isLoadMore = true, isSearch = _uiState.value.searchQuery.isNotBlank())
    }

    private fun loadNews(isRefresh: Boolean, isLoadMore: Boolean, isSearch: Boolean = false) {
        viewModelScope.launch {
            if (!isLoadMore && !isRefresh) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            val category = _uiState.value.tabs[_uiState.value.selectedTabIndex]
            val page = _uiState.value.currentPage
            val query = _uiState.value.searchQuery

            val result = if (isSearch && query.isNotBlank()) {
                newsRepository.searchNews(query, page)
            } else {
                newsRepository.getNewsByCategory(category, page)
            }

            when (result) {
                is Result.Success -> {
                    val newItems = result.data.articles
                    if (isLoadMore) {
                        currentNewsList.addAll(newItems)
                    } else {
                        currentNewsList.clear()
                        currentNewsList.addAll(newItems)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            newsItems = currentNewsList.toList(), // Create new list for compose recomposition
                            canLoadMore = newItems.isNotEmpty() && newItems.size >= 10 // Assuming page size is 10
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            error = result.exception.message ?: "An unknown error occurred"
                        )
                    }
                }
            }
        }
    }
} 