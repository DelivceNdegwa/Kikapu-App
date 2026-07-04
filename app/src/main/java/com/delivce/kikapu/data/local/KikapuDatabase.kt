package com.delivce.kikapu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.delivce.kikapu.data.local.dao.ItemDao
import com.delivce.kikapu.data.local.dao.SettingsDao
import com.delivce.kikapu.data.local.dao.TripDao
import com.delivce.kikapu.data.local.entity.ItemEntity
import com.delivce.kikapu.data.local.entity.SettingsEntity
import com.delivce.kikapu.data.local.entity.ShoppingItemEntity
import com.delivce.kikapu.data.local.entity.TripEntity

@Database(
    entities = [TripEntity::class, ShoppingItemEntity::class, ItemEntity::class, SettingsEntity::class],
    version = 4,
    exportSchema = false
)
abstract class KikapuDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun itemDao(): ItemDao
    abstract fun settingsDao(): SettingsDao
}
