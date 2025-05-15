package com.sdu.novaglide.data.remote.service

import com.sdu.novaglide.data.remote.dto.NewsListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("news")
    suspend fun getNewsList(
        @Query("category") category: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): NewsListResponse

    @GET("news/search")
    suspend fun searchNews(
        @Query("query") query: String,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): NewsListResponse
}
