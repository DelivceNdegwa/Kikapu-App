package com.delivce.kikapu.data.repository

import com.delivce.kikapu.data.local.dao.ItemDao
import com.delivce.kikapu.data.mappers.toDomain
import com.delivce.kikapu.data.mappers.toEntity
import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao
) : ItemRepository {

    override fun getItems(userId: String): Flow<List<Item>> =
        itemDao.getItems(userId).map { list -> list.map { it.toDomain() } }

    override fun getItemById(itemId: String): Flow<Item?> =
        itemDao.getItemById(itemId).map { it?.toDomain() }

    override fun getRestockDueCount(userId: String): Flow<Int> =
        itemDao.getRestockDueCount(userId, System.currentTimeMillis())

    override suspend fun upsertItem(item: Item): Result<Item> = runCatching {
        itemDao.upsertItem(item.toEntity())
        item
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        itemDao.deleteItem(itemId)
    }

    override suspend fun markShopped(itemId: String, timestamp: Long): Result<Unit> = runCatching {
        itemDao.markShopped(itemId, timestamp)
    }
}
