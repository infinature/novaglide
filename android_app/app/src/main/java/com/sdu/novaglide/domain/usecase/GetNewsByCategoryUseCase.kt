package com.sdu.novaglide.domain.usecase

import com.sdu.novaglide.domain.model.News
import com.sdu.novaglide.domain.repository.NewsRepository

class GetNewsByCategoryUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(category: String, page: Int, pageSize: Int): Result<Pair<List<News>, Boolean>> {
        return newsRepository.getNewsByCategory(category, page, pageSize)
    }
}
