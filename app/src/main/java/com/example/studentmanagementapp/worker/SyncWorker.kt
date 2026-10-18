package com.example.studentmanagementapp.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.example.studentmanagementapp.domain.usecase.SyncStudentsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncStudentsUseCase: SyncStudentsUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "Starting background sync...")
            syncStudentsUseCase()
            Log.d("SyncWorker", "Background sync completed successfully")
            Result.success()
        } catch (e: IOException) {
            Log.e("SyncWorker", "Network error during sync, retrying...", e)
            Result.retry()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            Result.failure()
        }
    }
}
