package com.example.studentmanagementapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.example.studentmanagementapp.StudentApplication
import java.io.IOException

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "Starting background sync...")
            val app =
                applicationContext as StudentApplication

            app.appContainer
                .studentRepository
                .syncPending()

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
