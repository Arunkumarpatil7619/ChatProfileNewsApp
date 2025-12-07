package com.assisment.newschatprofileapp.di



import android.content.Context
import com.assisment.newschatprofileapp.data.local.dao.ArticleDao
import com.assisment.newschatprofileapp.data.local.dao.MessageDao
import com.assisment.newschatprofileapp.data.remote.api.NewsApi
import com.assisment.newschatprofileapp.data.repository.MessageRepository
import com.assisment.newschatprofileapp.data.repository.MessageRepositoryImpl
import com.assisment.newschatprofileapp.data.repository.NewsRepository
import com.assisment.newschatprofileapp.data.repository.NewsRepositoryImpl
import com.assisment.newschatprofileapp.data.repository.ProfileRepository
import com.assisment.newschatprofileapp.data.repository.ProfileRepositoryImpl

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideNewsRepository(
        newsApi: NewsApi,
        articleDao: ArticleDao,
    ): NewsRepository {
        return NewsRepositoryImpl(newsApi, articleDao)
    }

    @Singleton
    @Provides
    fun provideMessageRepository(
        messageDao: MessageDao
    ): MessageRepository {
        return MessageRepositoryImpl(messageDao)
    }

    @Singleton
    @Provides
    fun provideProfileRepository(
        @ApplicationContext context: Context
    ): ProfileRepository {
        return ProfileRepositoryImpl(context)
    }
}