package com.assisment.newschatprofileapp.domain.usecase




import com.assisment.newschatprofileapp.data.repository.NewsRepository
import com.assisment.newschatprofileapp.domain.model.Article
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCachedArticlesUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(): Flow<List<Article>> {
        return repository.getCachedArticles()
    }
}