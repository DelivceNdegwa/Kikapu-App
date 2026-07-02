package com.delivce.kikapu.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.delivce.kikapu.data.local.entity.ShoppingItemEntity
import com.delivce.kikapu.data.local.entity.TripEntity
import com.delivce.kikapu.data.local.entity.TripWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Transaction
    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY date DESC")
    fun getTripsWithItems(userId: String): Flow<List<TripWithItems>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getTripWithItemsById(tripId: String): Flow<TripWithItems?>

    @Query("SELECT * FROM shopping_items WHERE tripId = :tripId")
    fun getItemsForTrip(tripId: String): Flow<List<ShoppingItemEntity>>

    @Upsert
    suspend fun upsertTrip(trip: TripEntity)

    @Upsert
    suspend fun upsertItems(items: List<ShoppingItemEntity>)

    @Upsert
    suspend fun upsertItem(item: ShoppingItemEntity)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: String)

    @Query("DELETE FROM shopping_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("SELECT COUNT(*) FROM trips WHERE userId = :userId AND status = 'UPCOMING'")
    fun getUpcomingTripsCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM trips WHERE userId = :userId AND status = 'COMPLETED'")
    fun getCompletedTripsCount(userId: String): Flow<Int>

    @Query("SELECT SUM(budget - totalSpent) FROM trips WHERE userId = :userId AND status = 'COMPLETED'")
    fun getTotalSavedOrOverspent(userId: String): Flow<Double?>
}
