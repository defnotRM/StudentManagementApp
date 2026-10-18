package com.example.studentmanagementapp.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {

    fun syncNow(context: Context) {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val request =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueue(request)
    }

    fun schedulePeriodicSync(context: Context) {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val request =
            PeriodicWorkRequestBuilder<SyncWorker>(
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "student-sync",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }
}
