package com.assisment.newschatprofileapp.domain.model


data class Article(
    val url: String,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val sourceName: String,
    val author: String?,
    val content: String?,
    val isFeatured: Boolean = false
)
data class Source(
    val id: String? = null,
    val name: String? = null
)