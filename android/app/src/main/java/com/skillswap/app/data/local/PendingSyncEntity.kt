package com.skillswap.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Stores offline actions (e.g., creating a skill listing while offline) so they can be
 * synchronized with the backend REST API once internet connectivity is restored.
 */
@Entity(tableName = "pending_sync_items")
data class PendingSyncEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val description: String?,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
