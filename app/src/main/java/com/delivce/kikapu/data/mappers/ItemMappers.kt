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
