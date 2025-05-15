package com.sdu.novaglide.ui.features.home

import com.sdu.novaglide.domain.model.News

data class HomeScreenState(
    val news: List<News> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTabIndex: Int = 0,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val page: Int = 1,
    val hasMorePages: Boolean = true
)
