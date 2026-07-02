package com.delivce.kikapu.data.mappers

import com.delivce.kikapu.data.local.entity.ShoppingItemEntity
import com.delivce.kikapu.data.local.entity.TripEntity
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.model.TripStatus

// --- Entity <-> Domain ---

fun TripEntity.toDomain(): Trip = Trip(
    id = id,
    userId = userId,
    name = name,
    budget = budget,
    date = date,
    reminderTime = reminderTime,
    status = runCatching { TripStatus.valueOf(status) }.getOrDefault(TripStatus.UPCOMING),
    totalSpent = totalSpent,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Trip.toEntity(): TripEntity = TripEntity(
    id = id,
    userId = userId,
    name = name,
    budget = budget,
    date = date,
    reminderTime = reminderTime,
    status = status.name,
    totalSpent = totalSpent,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ShoppingItemEntity.toDomain(): ShoppingItem = ShoppingItem(
    id = id,
    tripId = tripId,
    name = name,
    estimatedPrice = estimatedPrice,
    actualPrice = actualPrice,
    quantity = quantity,
    priorityIndex = priorityIndex,
    isChecked = isChecked,
    isOutOfBudget = isOutOfBudget,
    category = category,
    isCustom = isCustom,
    catalogItemId = catalogItemId
)

fun ShoppingItem.toEntity(): ShoppingItemEntity = ShoppingItemEntity(
    id = id,
    tripId = tripId,
    name = name,
    estimatedPrice = estimatedPrice,
    actualPrice = actualPrice,
    quantity = quantity,
    priorityIndex = priorityIndex,
    isChecked = isChecked,
    isOutOfBudget = isOutOfBudget,
    category = category,
    isCustom = isCustom,
    catalogItemId = catalogItemId
)

// --- Domain <-> Firestore ---

fun Trip.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "name" to name,
    "budget" to budget,
    "date" to date,
    "reminderTime" to reminderTime,
    "status" to status.name,
    "totalSpent" to totalSpent,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toTrip(): Trip = Trip(
    id = this["id"] as? String ?: "",
    userId = this["userId"] as? String ?: "",
    name = this["name"] as? String ?: "",
    budget = (this["budget"] as? Number)?.toDouble() ?: 0.0,
    date = (this["date"] as? Number)?.toLong() ?: 0L,
    reminderTime = (this["reminderTime"] as? Number)?.toLong(),
    status = (this["status"] as? String)?.let {
        runCatching { TripStatus.valueOf(it) }.getOrDefault(TripStatus.UPCOMING)
    } ?: TripStatus.UPCOMING,
    totalSpent = (this["totalSpent"] as? Number)?.toDouble() ?: 0.0,
    createdAt = (this["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
    updatedAt = (this["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
)

fun ShoppingItem.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "tripId" to tripId,
    "name" to name,
    "estimatedPrice" to estimatedPrice,
    "actualPrice" to actualPrice,
    "quantity" to quantity,
    "priorityIndex" to priorityIndex,
    "isChecked" to isChecked,
    "isOutOfBudget" to isOutOfBudget,
    "category" to category,
    "isCustom" to isCustom,
    "catalogItemId" to catalogItemId
)

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toShoppingItem(): ShoppingItem = ShoppingItem(
    id = this["id"] as? String ?: "",
    tripId = this["tripId"] as? String ?: "",
    name = this["name"] as? String ?: "",
    estimatedPrice = (this["estimatedPrice"] as? Number)?.toDouble() ?: 0.0,
    actualPrice = (this["actualPrice"] as? Number)?.toDouble() ?: 0.0,
    quantity = (this["quantity"] as? Number)?.toInt() ?: 1,
    priorityIndex = (this["priorityIndex"] as? Number)?.toInt() ?: 3,
    isChecked = this["isChecked"] as? Boolean ?: false,
    isOutOfBudget = this["isOutOfBudget"] as? Boolean ?: false,
    category = this["category"] as? String,
    isCustom = this["isCustom"] as? Boolean ?: false,
    catalogItemId = this["catalogItemId"] as? String
)
