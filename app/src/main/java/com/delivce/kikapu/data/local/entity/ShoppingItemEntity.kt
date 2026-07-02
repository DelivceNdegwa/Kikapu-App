package com.delivce.kikapu.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_items",
    foreignKeys = [ForeignKey(
        entity = TripEntity::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("tripId")]
)
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val name: String,
    val estimatedPrice: Double,
    val actualPrice: Double,
    val quantity: Int,
    val priorityIndex: Int,
    val isChecked: Boolean,
    val isOutOfBudget: Boolean,
    val category: String?,
    val isCustom: Boolean,
    val catalogItemId: String? = null
)
