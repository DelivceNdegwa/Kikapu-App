package com.delivce.kikapu.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.delivce.kikapu.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE userId = :userId")
    fun getSettings(userId: String): Flow<SettingsEntity?>

    @Upsert
    suspend fun upsertSettings(settings: SettingsEntity)
}
