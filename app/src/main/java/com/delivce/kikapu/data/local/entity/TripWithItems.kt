package com.delivce.kikapu.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TripWithItems(
    @Embedded val trip: TripEntity,
    @Relation(parentColumn = "id", entityColumn = "tripId")
    val items: List<ShoppingItemEntity>
)
