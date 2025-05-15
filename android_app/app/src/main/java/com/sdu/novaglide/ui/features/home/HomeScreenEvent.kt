package com.sdu.novaglide.ui.features.home

sealed class HomeScreenEvent {
    data class TabSelected(val index: Int) : HomeScreenEvent()
    data class SearchQueryChanged(val query: String) : HomeScreenEvent()
    data class SearchSubmitted(val query: String) : HomeScreenEvent()
    object ClearSearch : HomeScreenEvent()
    object RefreshNews : HomeScreenEvent()
    object LoadMoreNews : HomeScreenEvent()
}
