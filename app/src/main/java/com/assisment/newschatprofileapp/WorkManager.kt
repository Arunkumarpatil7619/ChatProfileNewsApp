package com.assisment.newschatprofileapp


import android.content.Context
import androidx.hilt.work.HiltWorker

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.assisment.newschatprofileapp.data.repository.NewsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val newsRepository: NewsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            newsRepository.refreshCache()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}