package com.assisment.newschatprofileapp.domain.usecase

import com.assisment.newschatprofileapp.data.repository.NewsRepository
import com.assisment.newschatprofileapp.domain.model.Article
import com.assisment.newschatprofileapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTopHeadlinesUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(
        country: String = "us",
        page: Int = 1,
        forceRefresh: Boolean = false
    ): Flow<Resource<List<Article>>> = flow {
        emit(Resource.Loading())

        try {
            val articles = repository.getTopHeadlines(
                country = country,
                page = page,
                forceRefresh = forceRefresh
            )

            if (articles.isNotEmpty()) {
                emit(Resource.Success(articles))
            } else {
                emit(Resource.Error("No articles found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load headlines"))
        }
    }
}