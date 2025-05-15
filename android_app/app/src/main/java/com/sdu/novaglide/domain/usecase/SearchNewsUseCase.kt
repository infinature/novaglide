package com.sdu.novaglide.domain.usecase

import com.sdu.novaglide.domain.model.News
import com.sdu.novaglide.domain.repository.NewsRepository

class SearchNewsUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(
        query: String,
        category: String? = null,
        page: Int = 1,
        pageSize: Int = 10
    ): Result<Pair<List<News>, Boolean>> {
        return newsRepository.searchNews(query, category, page, pageSize)
    }
}
