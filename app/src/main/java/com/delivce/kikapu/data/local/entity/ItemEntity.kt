package com.delivce.kikapu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val quantity: Int,
    val durationDays: Int,
    val lastShoppedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)
