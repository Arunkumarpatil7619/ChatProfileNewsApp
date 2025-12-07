package com.assisment.newschatprofileapp.data.remote.api


import com.assisment.newschatprofileapp.data.remote.dto.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchArticles(
        @Query("q") q: String,
        @Query("apiKey") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<NewsResponse>
}