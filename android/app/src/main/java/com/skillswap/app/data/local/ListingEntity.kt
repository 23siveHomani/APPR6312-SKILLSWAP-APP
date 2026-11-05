package com.skillswap.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local cache of listings so the Home/Browse screen can still show data when
 * offline, per Part 1 section 3 ("Offline mode with sync"). Populated whenever
 * a live fetch from the API succeeds; read from when a fetch fails.
 */
@Entity(tableName = "cached_listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val description: String?,
    val ownerName: String?,
    val imageUrl: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
