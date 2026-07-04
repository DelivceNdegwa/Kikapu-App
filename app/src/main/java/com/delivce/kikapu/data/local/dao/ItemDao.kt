package com.delivce.kikapu.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.delivce.kikapu.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items WHERE userId = :userId ORDER BY name ASC")
    fun getItems(userId: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId")
    fun getItemById(itemId: String): Flow<ItemEntity?>

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemByIdOnce(itemId: String): ItemEntity?

    @Upsert
    suspend fun upsertItem(item: ItemEntity)

    @Upsert
    suspend fun upsertItems(items: List<ItemEntity>)

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("UPDATE items SET lastShoppedAt = :timestamp, updatedAt = :timestamp WHERE id = :itemId")
    suspend fun markShopped(itemId: String, timestamp: Long)

    @Query("UPDATE items SET estimatedPrice = :price, updatedAt = :timestamp WHERE id = :itemId")
    suspend fun updatePrice(itemId: String, price: Double, timestamp: Long)

    @Query(
        """
        SELECT COUNT(*) FROM items
        WHERE userId = :userId
        AND lastShoppedAt IS NOT NULL
        AND (lastShoppedAt + durationDays * 86400000) <= :now
        """
    )
    fun getRestockDueCount(userId: String, now: Long): Flow<Int>
}
