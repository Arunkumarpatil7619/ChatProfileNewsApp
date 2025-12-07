package com.assisment.newschatprofileapp.di



import com.assisment.newschatprofileapp.data.repository.MessageRepository
import com.assisment.newschatprofileapp.data.repository.NewsRepository
import com.assisment.newschatprofileapp.data.repository.ProfileRepository
import com.assisment.newschatprofileapp.domain.usecase.GetCachedArticlesUseCase
import com.assisment.newschatprofileapp.domain.usecase.GetCurrentLocationUseCase
import com.assisment.newschatprofileapp.domain.usecase.GetMessagesUseCase
import com.assisment.newschatprofileapp.domain.usecase.GetTopHeadlinesUseCase
import com.assisment.newschatprofileapp.domain.usecase.GetUserProfileUseCase
import com.assisment.newschatprofileapp.domain.usecase.SaveProfileImageUseCase
import com.assisment.newschatprofileapp.domain.usecase.SearchArticlesUseCase
import com.assisment.newschatprofileapp.domain.usecase.SendImageMessageUseCase
import com.assisment.newschatprofileapp.domain.usecase.SendTextMessageUseCase
import com.assisment.newschatprofileapp.domain.usecase.SimulateReceivedMessageUseCase
import com.assisment.newschatprofileapp.domain.usecase.UpdateProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Singleton
    @Provides
    fun provideGetTopHeadlinesUseCase(
        repository: NewsRepository
    ): GetTopHeadlinesUseCase {
        return GetTopHeadlinesUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideSearchArticlesUseCase(
        repository: NewsRepository
    ): SearchArticlesUseCase {
        return SearchArticlesUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideGetCachedArticlesUseCase(
        repository: NewsRepository
    ): GetCachedArticlesUseCase {
        return GetCachedArticlesUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideGetMessagesUseCase(
        repository: MessageRepository
    ): GetMessagesUseCase {
        return GetMessagesUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideSendTextMessageUseCase(
        repository: MessageRepository
    ): SendTextMessageUseCase {
        return SendTextMessageUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideSendImageMessageUseCase(
        repository: MessageRepository
    ): SendImageMessageUseCase {
        return SendImageMessageUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideSimulateReceivedMessageUseCase(
        repository: MessageRepository
    ): SimulateReceivedMessageUseCase {
        return SimulateReceivedMessageUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideGetUserProfileUseCase(
        repository: ProfileRepository
    ): GetUserProfileUseCase {
        return GetUserProfileUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideUpdateProfileUseCase(
        repository: ProfileRepository
    ): UpdateProfileUseCase {
        return UpdateProfileUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideGetCurrentLocationUseCase(
        repository: ProfileRepository
    ): GetCurrentLocationUseCase {
        return GetCurrentLocationUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideSaveProfileImageUseCase(
        repository: ProfileRepository
    ): SaveProfileImageUseCase {
        return SaveProfileImageUseCase(repository)
    }
}