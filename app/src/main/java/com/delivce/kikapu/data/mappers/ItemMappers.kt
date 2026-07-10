package com.delivce.kikapu.data.mappers

import com.delivce.kikapu.data.local.entity.ItemEntity
import com.delivce.kikapu.domain.model.Item

fun ItemEntity.toDomain(): Item = Item(
    id = id,
    userId = userId,
    name = name,
    quantity = quantity,
    estimatedPrice = estimatedPrice,
    priorityIndex = priorityIndex,
    durationDays = durationDays,
    lastShoppedAt = lastShoppedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Item.toEntity(): ItemEntity = ItemEntity(
    id = id,
    userId = userId,
    name = name,
    quantity = quantity,
    estimatedPrice = estimatedPrice,
    priorityIndex = priorityIndex,
    durationDays = durationDays,
    lastShoppedAt = lastShoppedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// --- Domain <-> Firestore ---

fun Item.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "name" to name,
    "quantity" to quantity,
    "estimatedPrice" to estimatedPrice,
    "priorityIndex" to priorityIndex,
    "durationDays" to durationDays,
    "lastShoppedAt" to lastShoppedAt,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toItem(): Item = Item(
    id = this["id"] as? String ?: "",
    userId = this["userId"] as? String ?: "",
    name = this["name"] as? String ?: "",
    quantity = (this["quantity"] as? Number)?.toInt() ?: 1,
    estimatedPrice = (this["estimatedPrice"] as? Number)?.toDouble() ?: 0.0,
    priorityIndex = (this["priorityIndex"] as? Number)?.toInt() ?: 3,
    durationDays = (this["durationDays"] as? Number)?.toInt() ?: 30,
    lastShoppedAt = (this["lastShoppedAt"] as? Number)?.toLong(),
    createdAt = (this["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
    updatedAt = (this["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
)
