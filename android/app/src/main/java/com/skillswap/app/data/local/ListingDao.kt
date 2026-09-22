package com.skillswap.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ListingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<ListingEntity>)

    @Query("SELECT * FROM cached_listings ORDER BY cachedAt DESC")
    suspend fun getAll(): List<ListingEntity>

    @Query("DELETE FROM cached_listings")
    suspend fun clear()
}
