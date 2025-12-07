package com.assisment.newschatprofileapp.data.repository


import com.assisment.newschatprofileapp.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getTopHeadlines(
        country: String,
        page: Int,
        forceRefresh: Boolean = false
    ): List<Article>

    suspend fun searchArticles(query: String): List<Article>

    fun getCachedArticles(): Flow<List<Article>>

    suspend fun refreshCache()

    suspend fun clearCache()
}