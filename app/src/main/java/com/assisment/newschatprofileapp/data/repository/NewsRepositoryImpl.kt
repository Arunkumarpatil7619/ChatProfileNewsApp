package com.assisment.newschatprofileapp.data.repository

import android.content.Context
import com.assisment.newschatprofileapp.data.local.dao.ArticleDao
import com.assisment.newschatprofileapp.data.remote.api.NewsApi
import com.assisment.newschatprofileapp.domain.model.Article
import com.assisment.newschatprofileapp.utils.SecurePrefs
import com.assisment.newschatprofileapp.utils.toArticle
import com.assisment.newschatprofileapp.utils.toArticleEntity
import com.assisment.newschatprofileapp.utils.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val newsApi: NewsApi,
    private val articleDao: ArticleDao,
) : NewsRepository {

    private val apiKey: String
        get() = SecurePrefs.getApiKey() ?: ""


    override suspend fun getTopHeadlines(
        country: String,
        page: Int,
        forceRefresh: Boolean
    ): List<Article> {
        return try {
            // If not forcing refresh and page 1 has 20 cached articles, return cache immediately
            if (!forceRefresh && page == 1) {
                val cachedCount = articleDao.getArticleCountByPage(page)
                if (cachedCount >= 20) {
                    return articleDao.getArticlesByPage(page).map { it.toArticle() }
                }
            }

            // Fetch from API
            val response = newsApi.getTopHeadlines(
                country = country,
                apiKey = apiKey,
                page = page,
                pageSize = 20
            )

            if (response.isSuccessful) {
                val articlesDto = response.body()?.articles ?: emptyList()

                // Map DTO -> Entity -> Domain while marking featured for first page
                val domainArticles = articlesDto.mapIndexed { idx, dto ->
                    val articleDomain = dto.toArticleEntity().toArticle().copy(
                        isFeatured = page == 1 && idx < 5
                    )
                    articleDomain
                }

                // Cache articles using REPLACE strategy -> prevents duplicates
                cacheArticles(domainArticles, page)

                domainArticles
            } else {
                // API returned error -> fallback to cache for requested page
                getArticlesFromCache(page)
            }
        } catch (e: Exception) {
            // Network or parsing exception -> fallback to cache
            getArticlesFromCache(page)
        }
    }

    override suspend fun searchArticles(query: String): List<Article> {
        return try {
            // First try API
            val response = newsApi.searchArticles(
                q = query,
                apiKey = apiKey,
                page = 1,
                pageSize = 20
            )

            if (response.isSuccessful) {
                response.body()?.articles?.map { it.toArticleEntity().toArticle() } ?: emptyList()
            } else {
                // Fallback to cache search
                articleDao.searchArticles(query).map { it.toArticle() }
            }
        } catch (e: Exception) {
            // Fallback to cache search
            articleDao.searchArticles(query).map { it.toArticle() }
        }
    }

    override fun getCachedArticles(): Flow<List<Article>> {
        return articleDao.getAllArticles()
            .map { entities ->
                entities.map { it.toArticle() }
            }
    }

    override suspend fun refreshCache() {
        // Delete articles older than 7 days
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        articleDao.deleteOldArticles(sevenDaysAgo)

        // Refresh page 1 from API
        getTopHeadlines("us", 1, true)
    }

    override suspend fun clearCache() {
        articleDao.deleteAll()
    }

    private suspend fun getArticlesFromCache(page: Int): List<Article> {
        return try {
            if (page == 1) {
                articleDao.getLatestArticles().map { it.toArticle() }
            } else {
                articleDao.getArticlesByPage(page).map { it.toArticle() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun cacheArticles(articles: List<Article>, page: Int) {
        // Convert to entities and use insertAll which has OnConflictStrategy.REPLACE
        val entities = articles.map { article -> article.toEntity(page) }
        // Using replace strategy in DAO prevents duplicates (same primary key will replace)
        articleDao.insertAll(entities)
    }
}
