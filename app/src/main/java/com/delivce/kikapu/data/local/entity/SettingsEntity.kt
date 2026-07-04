package com.delivce.kikapu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val userId: String,
    val successRateTarget: Int
)
