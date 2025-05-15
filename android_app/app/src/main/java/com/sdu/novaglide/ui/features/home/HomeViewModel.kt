package com.sdu.novaglide.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.domain.model.News
import com.sdu.novaglide.domain.usecase.GetNewsByCategoryUseCase
import com.sdu.novaglide.domain.usecase.SearchNewsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(
    private val getNewsByCategoryUseCase: GetNewsByCategoryUseCase,
    private val searchNewsUseCase: SearchNewsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private var currentJob: Job? = null

    init {
        loadNews()
    }

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.TabSelected -> {
                _state.update { 
                    it.copy(
                        selectedTabIndex = event.index,
                        searchQuery = "",
                        isSearchActive = false
                    )
                }
                resetAndLoadNews()
            }
            
            is HomeScreenEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            
            is HomeScreenEvent.SearchSubmitted -> {
                if (event.query.isNotBlank()) {
                    _state.update { it.copy(isSearchActive = true) }
                    resetAndSearchNews()
                }
            }
            
            HomeScreenEvent.ClearSearch -> {
                _state.update { it.copy(searchQuery = "", isSearchActive = false) }
                resetAndLoadNews()
            }
            
            HomeScreenEvent.RefreshNews -> resetAndLoadNews()
            
            HomeScreenEvent.LoadMoreNews -> {
                if (!_state.value.isLoading && _state.value.hasMorePages) {
                    if (_state.value.isSearchActive) {
                        searchNews(loadMore = true)
                    } else {
                        loadNews(loadMore = true)
                    }
                }
            }
        }
    }

    private fun resetAndLoadNews() {
        _state.update { it.copy(page = 1, news = emptyList()) }
        loadNews()
    }

    private fun resetAndSearchNews() {
        _state.update { it.copy(page = 1, news = emptyList()) }
        searchNews()
    }

    private fun loadNews(loadMore: Boolean = false) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                val category = getCategoryName(_state.value.selectedTabIndex)
                val result = getNewsByCategoryUseCase(
                    category = category,
                    page = _state.value.page,
                    pageSize = PAGE_SIZE
                )
                
                result.fold(
                    onSuccess = { (newsList, hasMore) ->
                        val updatedList = if (loadMore) {
                            _state.value.news + newsList
                        } else {
                            newsList
                        }
                        
                        _state.update { 
                            it.copy(
                                news = updatedList,
                                isLoading = false,
                                hasMorePages = hasMore,
                                page = it.page + 1,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "未知错误"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "未知错误"
                    )
                }
            }
        }
    }

    private fun searchNews(loadMore: Boolean = false) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                val category = if (_state.value.selectedTabIndex == 4) null // 推荐标签不过滤类别
                    else getCategoryName(_state.value.selectedTabIndex)
                
                val result = searchNewsUseCase(
                    query = _state.value.searchQuery,
                    category = category,
                    page = _state.value.page,
                    pageSize = PAGE_SIZE
                )
                
                result.fold(
                    onSuccess = { (newsList, hasMore) ->
                        val updatedList = if (loadMore) {
                            _state.value.news + newsList
                        } else {
                            newsList
                        }
                        
                        _state.update { 
                            it.copy(
                                news = updatedList,
                                isLoading = false,
                                hasMorePages = hasMore,
                                page = it.page + 1,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "未知错误"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "未知错误"
                    )
                }
            }
        }
    }

    private fun getCategoryName(index: Int): String {
        return when (index) {
            0 -> "保研"
            1 -> "考研"
            2 -> "留学"
            3 -> "考公"
            4 -> "推荐"
            else -> "推荐"
        }
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}
