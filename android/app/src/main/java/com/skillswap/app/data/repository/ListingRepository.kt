package com.skillswap.app.data.repository

import com.skillswap.app.data.api.ApiService
import com.skillswap.app.data.local.ListingDao
import com.skillswap.app.data.local.ListingEntity
import com.skillswap.app.data.model.SkillListing

/**
 * Fetches listings from the live API and mirrors them into Room. If the live
 * fetch fails (e.g. no network), falls back to whatever was last cached, so
 * the Home screen can still show a "you're viewing cached results" list
 * instead of an empty error state - Part 1 section 4.2.
 */
class ListingRepository(
    private val api: ApiService,
    private val listingDao: ListingDao
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
                        ownerName = it.ownerId?.name
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
                ownerId = null
            )
        }
        return cached to true
    }
}
