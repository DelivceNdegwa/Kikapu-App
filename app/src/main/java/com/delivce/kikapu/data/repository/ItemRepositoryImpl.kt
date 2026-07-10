package com.delivce.kikapu.data.repository

import com.delivce.kikapu.data.local.dao.ItemDao
import com.delivce.kikapu.data.mappers.toDomain
import com.delivce.kikapu.data.mappers.toEntity
import com.delivce.kikapu.data.mappers.toFirestoreMap
import com.delivce.kikapu.data.mappers.toItem
import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.repository.ItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val CATALOG_ITEMS_COLLECTION = "catalog_items"

class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao,
    private val firestore: FirebaseFirestore
) : ItemRepository {

    override fun getItems(userId: String): Flow<List<Item>> =
        itemDao.getItems(userId).map { list -> list.map { it.toDomain() } }

    override fun getItemById(itemId: String): Flow<Item?> =
        itemDao.getItemById(itemId).map { it?.toDomain() }

    override fun getRestockDueCount(userId: String): Flow<Int> =
        itemDao.getRestockDueCount(userId, System.currentTimeMillis())

    override suspend fun upsertItem(item: Item): Result<Item> = runCatching {
        itemDao.upsertItem(item.toEntity())
        runCatching {
            firestore.collection(CATALOG_ITEMS_COLLECTION).document(item.id)
                .set(item.toFirestoreMap()).await()
        }
        item
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        itemDao.deleteItem(itemId)
        runCatching {
            firestore.collection(CATALOG_ITEMS_COLLECTION).document(itemId).delete().await()
        }
    }

    override suspend fun markShopped(itemId: String, timestamp: Long): Result<Unit> = runCatching {
        itemDao.markShopped(itemId, timestamp)
        mirrorToFirestore(itemId)
    }

    override suspend fun updateItemPrice(itemId: String, price: Double): Result<Unit> = runCatching {
        itemDao.updatePrice(itemId, price, System.currentTimeMillis())
        mirrorToFirestore(itemId)
    }

    private suspend fun mirrorToFirestore(itemId: String) {
        runCatching {
            val entity = itemDao.getItemByIdOnce(itemId) ?: return
            firestore.collection(CATALOG_ITEMS_COLLECTION).document(itemId)
                .set(entity.toDomain().toFirestoreMap()).await()
        }
    }

    override suspend fun syncWithFirestore(userId: String) {
        if (userId.isBlank()) return
        try {
            val docs = firestore.collection(CATALOG_ITEMS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val items = docs.documents.mapNotNull { it.data?.toItem() }
            if (items.isNotEmpty()) {
                itemDao.upsertItems(items.map { it.toEntity() })
            }
        } catch (_: Exception) {
            // Offline is a valid state - swallow and keep existing Room data.
        }
    }
}
