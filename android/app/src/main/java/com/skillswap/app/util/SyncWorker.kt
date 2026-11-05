package com.skillswap.app.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skillswap.app.data.local.AppDatabase
import com.skillswap.app.data.model.CreateListingRequest

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting offline sync worker...")
        val db = AppDatabase.getInstance(applicationContext)
        val pendingDao = db.pendingSyncDao()
        val listingDao = db.listingDao()
        val api = ServiceLocator.apiService(applicationContext)

        val pendingItems = pendingDao.getAllPending()
        if (pendingItems.isEmpty()) {
            Log.i(TAG, "No pending items to sync.")
            return Result.success()
        }

        var hasFailure = false
        for (item in pendingItems) {
            try {
                val response = api.createListing(
                    CreateListingRequest(
                        title = item.title,
                        category = item.category,
                        description = item.description,
                        imageUrl = item.imageUrl
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val created = response.body()!!
                    listingDao.insertAll(
                        listOf(
                            com.skillswap.app.data.local.ListingEntity(
                                id = created._id,
                                title = created.title,
                                category = created.category,
                                description = created.description,
                                ownerName = created.ownerId?.name ?: "Me",
                                imageUrl = created.imageUrl
                            )
                        )
                    )
                    pendingDao.deleteById(item.id)
                    Log.i(TAG, "Synced item ${item.id} successfully")
                } else {
                    hasFailure = true
                    Log.w(TAG, "Failed to sync item ${item.id}: ${response.code()}")
                }
            } catch (e: Exception) {
                hasFailure = true
                Log.e(TAG, "Error syncing item ${item.id}", e)
            }
        }

        return if (hasFailure) Result.retry() else Result.success()
    }

    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "SkillSwapOfflineSyncWorker"
    }
}
