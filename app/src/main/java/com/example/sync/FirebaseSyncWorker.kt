package com.example.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker that reliably syncs offline orders to Firebase Realtime Database
 * and Cloud Firestore once network connectivity is connected.
 */
class FirebaseSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "FirebaseSyncWorker triggered. Checking for pending offline orders...")
        val database = AppDatabase.getDatabase(applicationContext)
        val syncManager = FirebaseSyncManager(applicationContext, database.tshirtOrderDao())

        return@withContext try {
            val count = syncManager.syncPendingOrders()
            Log.d(TAG, "FirebaseSyncWorker completed: $count orders synced.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseSyncWorker failed during sync, will retry.", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "FirebaseSyncWorker"
        private const val UNIQUE_WORK_NAME = "firebase_pending_orders_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<FirebaseSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
