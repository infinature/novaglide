package com.sdu.novaglide.ui.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sdu.novaglide.data.local.entity.FavoriteArticleEntity
import com.sdu.novaglide.data.repository.FavoriteArticleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoriteArticleViewModel(
    private val favoriteArticleRepository: FavoriteArticleRepository
) : ViewModel() {
    private val TAG = "FavoriteVM"

    private val _favoriteArticles = MutableStateFlow<List<FavoriteArticleEntity>>(emptyList())
    val favoriteArticles: StateFlow<List<FavoriteArticleEntity>> = _favoriteArticles.asStateFlow()

    // 用于跟踪特定文章是否被收藏的状态
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun loadUserFavorites(userId: String) {
        viewModelScope.launch {
            favoriteArticleRepository.getFavorites(userId)
                .catch { e -> Log.e(TAG, "Error loading favorites", e) }
                .collect { _favoriteArticles.value = it }
        }
    }

    fun checkIfFavorite(userId: String, newsId: String) {
        viewModelScope.launch {
            favoriteArticleRepository.isFavorite(userId, newsId)
                .catch { e -> Log.e(TAG, "Error checking if favorite", e); _isFavorite.value = false }
                .collect { _isFavorite.value = it }
        }
    }

    fun addFavorite(userId: String, newsId: String, newsTitle: String) {
        viewModelScope.launch {
            try {
                favoriteArticleRepository.addFavorite(userId, newsId, newsTitle)
                _isFavorite.value = true // Optimistically update
            } catch (e: Exception) {
                Log.e(TAG, "Error adding favorite", e)
            }
        }
    }

    fun removeFavorite(userId: String, newsId: String) {
        viewModelScope.launch {
            try {
                favoriteArticleRepository.removeFavorite(userId, newsId)
                _isFavorite.value = false // Optimistically update
            } catch (e: Exception) {
                Log.e(TAG, "Error removing favorite", e)
            }
        }
    }

    class Factory(
        private val favoriteArticleRepository: FavoriteArticleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoriteArticleViewModel::class.java)) {
                return FavoriteArticleViewModel(favoriteArticleRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for FavoriteArticleViewModel")
        }
    }
}
