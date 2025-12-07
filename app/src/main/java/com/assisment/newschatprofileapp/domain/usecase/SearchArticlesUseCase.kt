
package com.assisment.newschatprofileapp.domain.usecase

import com.assisment.newschatprofileapp.data.repository.NewsRepository
import com.assisment.newschatprofileapp.domain.model.Article
import com.assisment.newschatprofileapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchArticlesUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(query: String): Flow<Resource<List<Article>>> = flow {
        emit(Resource.Loading())

        try {
            val articles = repository.searchArticles(query)
            emit(Resource.Success(articles))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Search failed"))
        }
    }
}