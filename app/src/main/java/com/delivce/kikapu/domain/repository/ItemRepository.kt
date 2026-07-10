package com.delivce.kikapu.domain.repository

import com.delivce.kikapu.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems(userId: String): Flow<List<Item>>
    fun getItemById(itemId: String): Flow<Item?>
    fun getRestockDueCount(userId: String): Flow<Int>
    suspend fun upsertItem(item: Item): Result<Item>
    suspend fun deleteItem(itemId: String): Result<Unit>
    suspend fun markShopped(itemId: String, timestamp: Long = System.currentTimeMillis()): Result<Unit>
    suspend fun updateItemPrice(itemId: String, price: Double): Result<Unit>
    suspend fun syncWithFirestore(userId: String)
}
