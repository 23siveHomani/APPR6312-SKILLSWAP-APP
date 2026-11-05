package com.skillswap.app.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.skillswap.app.data.api.ApiService
import com.skillswap.app.data.local.ListingDao
import com.skillswap.app.data.local.ListingEntity
import com.skillswap.app.data.local.PendingSyncDao
import com.skillswap.app.data.local.PendingSyncEntity
import com.skillswap.app.data.model.CreateListingRequest
import com.skillswap.app.data.model.SkillListing
import com.skillswap.app.util.SyncWorker

/**
 * Fetches listings from the live API and mirrors them into Room. If the live
 * fetch fails (e.g. no network), falls back to whatever was last cached, so
 * the Home screen can still show cached results when offline. Also handles
 * offline creation and WorkManager synchronization.
 */
class ListingRepository(
    private val api: ApiService,
    private val listingDao: ListingDao,
    private val pendingSyncDao: PendingSyncDao,
    private val context: Context
) {

    /** @return Pair of (listings, isFromCache) */
    suspend fun getListings(category: String? = null): Pair<List<SkillListing>, Boolean> {
        return try {
            val response = api.getListings(category)
            if (response.isSuccessful && response.body() != null) {
                val listings = response.body()!!.listings
                listingDao.clear()
                listingDao.insertAll(listings.map {
                    ListingEntity(
                        id = it._id,
                        title = it.title,
                        category = it.category,
                        description = it.description,
                        ownerName = it.ownerId?.name,
                        imageUrl = it.imageUrl
                    )
                })
                listings to false
            } else {
                fallbackToCache()
            }
        } catch (e: Exception) {
            fallbackToCache()
        }
    }

    private suspend fun fallbackToCache(): Pair<List<SkillListing>, Boolean> {
        val cached = listingDao.getAll().map {
            SkillListing(
                _id = it.id,
                title = it.title,
                category = it.category,
                description = it.description,
                ownerId = null,
                imageUrl = it.imageUrl
            )
        }
        return cached to true
    }

    suspend fun createListing(
        title: String,
        category: String,
        description: String?,
        imageUrl: String?
    ): Result<SkillListing> {
        return try {
            val response = api.createListing(
                CreateListingRequest(
                    title = title,
                    category = category,
                    description = description,
                    imageUrl = imageUrl
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!
                listingDao.insertAll(
                    listOf(
                        ListingEntity(
                            id = created._id,
                            title = created.title,
                            category = created.category,
                            description = created.description,
                            ownerName = created.ownerId?.name ?: "Me",
                            imageUrl = created.imageUrl
                        )
                    )
                )
                Result.success(created)
            } else {
                saveOffline(title, category, description, imageUrl)
            }
        } catch (e: Exception) {
            saveOffline(title, category, description, imageUrl)
        }
    }

    private suspend fun saveOffline(
        title: String,
        category: String,
        description: String?,
        imageUrl: String?
    ): Result<SkillListing> {
        val pendingEntity = PendingSyncEntity(
            title = title,
            category = category,
            description = description,
            imageUrl = imageUrl
        )
        pendingSyncDao.insert(pendingEntity)
        scheduleSyncWorker()

        return Result.failure(Exception("OFFLINE_SAVED"))
    }

    fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }

    suspend fun getPendingSyncCount(): Int = pendingSyncDao.getPendingCount()
}
