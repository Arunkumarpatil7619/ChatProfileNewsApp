package com.assisment.newschatprofileapp.di



import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.assisment.newschatprofileapp.SyncWorker
import com.assisment.newschatprofileapp.utils.SecurePrefs
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class App : Application(){


    override fun onCreate() {
        super.onCreate()
        SecurePrefs.init(this)
        SecurePrefs.saveApiKey(
            this,
            "7780d475c1b1456b84b02dd6b5f77ec2"
        )
        scheduleBackgroundSync()
    }



        private fun scheduleBackgroundSync() {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                4, TimeUnit.HOURS, // Repeat every 4 hours
                30, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                    "background_sync",
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
        }
    }
