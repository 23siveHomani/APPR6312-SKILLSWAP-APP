package com.skillswap.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PendingSyncEntity)

    @Query("SELECT * FROM pending_sync_items ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingSyncEntity>

    @Query("DELETE FROM pending_sync_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_sync_items")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM pending_sync_items")
    suspend fun getPendingCount(): Int
}
