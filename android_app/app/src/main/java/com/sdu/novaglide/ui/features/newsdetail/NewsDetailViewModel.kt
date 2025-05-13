package com.sdu.novaglide.ui.features.newsdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.data.model.NewsItem
import com.sdu.novaglide.data.repository.NewsRepository
import com.sdu.novaglide.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsDetailUiState(
    val isLoading: Boolean = false,
    val newsItem: NewsItem? = null,
    val error: String? = null
)

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    savedStateHandle: SavedStateHandle // Hilt provides this for accessing navigation arguments
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsDetailUiState())
    val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()

    private val newsId: String? = savedStateHandle.get<String>("newsId")

    init {
        newsId?.let {
            loadNewsDetail(it)
        } ?: run {
            _uiState.update { it.copy(error = "News ID not provided", isLoading = false) }
        }
    }

    fun loadNewsDetail(id: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = newsRepository.getNewsDetail(id)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            newsItem = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to load news detail"
                        )
                    }
                }
            }
        }
    }
    
    fun retry() {
        newsId?.let { loadNewsDetail(it) }
    }
} 