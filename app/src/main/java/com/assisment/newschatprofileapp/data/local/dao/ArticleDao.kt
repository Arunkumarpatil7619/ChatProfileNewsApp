package com.assisment.newschatprofileapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.assisment.newschatprofileapp.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isFeatured = 1 ORDER BY publishedAt DESC LIMIT 5")
    suspend fun getFeaturedArticles(): List<ArticleEntity>

    @Query("SELECT * FROM articles WHERE page = :page ORDER BY publishedAt DESC")
    suspend fun getArticlesByPage(page: Int): List<ArticleEntity>

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY publishedAt DESC LIMIT 20")
    suspend fun searchArticles(query: String): List<ArticleEntity>

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC LIMIT 20")
    suspend fun getLatestArticles(): List<ArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    @Query("DELETE FROM articles WHERE cachedAt < :timestamp")
    suspend fun deleteOldArticles(timestamp: Long)

    @Query("SELECT COUNT(*) FROM articles WHERE page = :page")
    suspend fun getArticleCountByPage(page: Int): Int
}