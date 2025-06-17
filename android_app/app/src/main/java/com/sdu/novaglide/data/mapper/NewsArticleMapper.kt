package com.sdu.novaglide.data.mapper

import com.sdu.novaglide.data.local.entity.NewsArticleEntity
import com.sdu.novaglide.domain.model.NewsArticle
import java.util.Date

object NewsArticleMapper {

    fun mapEntityToDomain(entity: NewsArticleEntity): NewsArticle {
        return NewsArticle(
            articleId = entity.articleId,
            title = entity.title,
            contentSnippet = entity.contentSnippet,
            category = entity.category,
            publishDate = Date(entity.publishDate),
            imageUrl = entity.imageUrl,
            source = entity.source,
            recommendationScore = entity.recommendationScore
        )
    }

    fun mapDomainToEntity(domain: NewsArticle): NewsArticleEntity {
        return NewsArticleEntity(
            articleId = domain.articleId,
            title = domain.title,
            contentSnippet = domain.contentSnippet,
            category = domain.category,
            publishDate = domain.publishDate.time,
            imageUrl = domain.imageUrl,
            source = domain.source,
            recommendationScore = domain.recommendationScore
        )
    }

    fun mapEntityListToDomainList(entities: List<NewsArticleEntity>): List<NewsArticle> {
        return entities.map { mapEntityToDomain(it) }
    }
}
