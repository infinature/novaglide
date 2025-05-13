package com.sdu.novaglide.data.remote

import com.sdu.novaglide.data.model.NewsItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface NewsApiService {

    companion object {
        // TODO: Replace with your actual API base URL
        const val BASE_URL = "https://api.example.com/v1/"
    }

    @GET("news")
    suspend fun getNewsByCategory(
        @Query("category") category: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 10
    ): Response<NewsListResponse> // Assuming API returns a wrapper object

    @GET("search")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 10
    ): Response<NewsListResponse>

    @GET("news/{id}")
    suspend fun getNewsDetail(@Path("id") newsId: String): Response<NewsItem> // Assuming API returns a single NewsItem

    // TODO: Add endpoint for news detail if needed for a dedicated detail screen
    // @GET("news/{id}")
    // suspend fun getNewsDetail(@Path("id") newsId: String): Response<NewsItem>
}

// Define a generic response wrapper if your API uses one
data class NewsListResponse(
    val articles: List<NewsItem>,
    val totalResults: Int,
    // Add other relevant fields like currentPage, totalPages if available
) 