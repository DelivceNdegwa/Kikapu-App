package com.delivce.kikapu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val budget: Double,
    val date: Long,
    val reminderTime: Long?,
    val status: String,
    val totalSpent: Double,
    val createdAt: Long,
    val updatedAt: Long
)
