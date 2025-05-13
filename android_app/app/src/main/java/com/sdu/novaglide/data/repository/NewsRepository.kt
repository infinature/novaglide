package com.sdu.novaglide.data.repository

import com.sdu.novaglide.data.model.NewsItem
// import com.sdu.novaglide.data.remote.NewsApiService // Commented out for mock data
import com.sdu.novaglide.data.remote.NewsListResponse
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

// Define a Result wrapper for handling success/error states
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

interface NewsRepository {
    suspend fun getNewsByCategory(category: String, page: Int): Result<NewsListResponse>
    suspend fun searchNews(query: String, page: Int): Result<NewsListResponse>
    suspend fun getNewsDetail(newsId: String): Result<NewsItem>
}

@Singleton
class NewsRepositoryImpl @Inject constructor(
    // private val newsApiService: NewsApiService // Commented out for mock data
) : NewsRepository {

    private val mockImages = listOf(
        "https://via.placeholder.com/300/09f/fff.png",
        "https://via.placeholder.com/300/e91e63/fff.png",
        "https://via.placeholder.com/300/4caf50/fff.png",
        "https://via.placeholder.com/300/ffc107/000.png",
        null // Simulate items without images
    )

    override suspend fun getNewsByCategory(category: String, page: Int): Result<NewsListResponse> {
        // Simulate network delay
        delay(1000)

        // Simulate error for a specific category/page for testing
        if (category == "留学" && page == 1) {
            return Result.Error(Exception("Mock API Error: Failed to load 留学 category"))
        }

        // Simulate end of pagination
        if (page > 2) {
            return Result.Success(NewsListResponse(articles = emptyList(), totalResults = 25)) // Total results can be arbitrary
        }

        // Create mock data
        val mockItems = (1..10).map { index ->
            val itemIndex = (page - 1) * 10 + index
            NewsItem(
                id = "${category}_${itemIndex}",
                title = "[$category] 资讯标题 $itemIndex (第 $page 页)",
                summary = "这是分类 '$category' 下第 $itemIndex 条资讯的摘要内容。这里是一些模拟的文本来填充空间。",
                source = "模拟来源 ${itemIndex % 3 + 1}",
                publishedAt = "${itemIndex % 12 + 1}小时前", // Simple mock time
                category = category,
                imageUrl = mockImages.random(),
                articleUrl = "https://example.com/news/${category}_${itemIndex}"
            )
        }

        return Result.Success(NewsListResponse(articles = mockItems, totalResults = 25))
    }

    override suspend fun searchNews(query: String, page: Int): Result<NewsListResponse> {
        // Simulate network delay
        delay(1500)

        // Simulate no results for a specific query
        if (query.contains("空", ignoreCase = true)) {
             return Result.Success(NewsListResponse(articles = emptyList(), totalResults = 0))
        }
        
        // Simulate end of pagination for search
        if (page > 1) {
            return Result.Success(NewsListResponse(articles = emptyList(), totalResults = 5))
        }

        // Create mock search results
        val mockItems = (1..5).map { index ->
            NewsItem(
                id = "search_${query}_${index}",
                title = "搜索结果: '$query' 相关资讯 $index",
                summary = "这是与 '$query' 相关的第 $index 条搜索结果的摘要。",
                source = "搜索来源 ${index % 2 + 1}",
                publishedAt = "${index}分钟前",
                category = "搜索", // Or derive from item if API provides it
                imageUrl = mockImages.random(),
                articleUrl = "https://example.com/search?q=$query&id=$index"
            )
        }

        return Result.Success(NewsListResponse(articles = mockItems, totalResults = 5))
    }

    override suspend fun getNewsDetail(newsId: String): Result<NewsItem> {
        // Simulate network delay
        delay(800)

        // Simulate not found for a specific ID
        if (newsId == "not_found") {
            return Result.Error(Exception("Mock API Error: News item with ID $newsId not found"))
        }

        // Create mock detail data based on ID (simple derivation)
        val parts = newsId.split('_')
        val category = parts.getOrElse(0) { "Unknown" }
        val itemIndex = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val mockDetail = NewsItem(
            id = newsId,
            title = "详细资讯标题: [$category] $itemIndex",
            summary = "这是 ID 为 $newsId 的资讯的**详细**摘要内容。这里可以包含更丰富的信息，甚至是一些简单的 *Markdown* 格式。\n\n段落分隔。",
            source = "详细来源 $category",
            publishedAt = "${itemIndex % 24}小时前",
            category = category,
            imageUrl = mockImages[itemIndex % mockImages.size], // Use a predictable image from the list
            articleUrl = "https://example.com/news/detail/$newsId"
        )

        return Result.Success(mockDetail)
    }
} 