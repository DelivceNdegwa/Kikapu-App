package com.delivce.kikapu.domain.repository

import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.SummaryStats
import com.delivce.kikapu.domain.model.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getTrips(userId: String): Flow<List<Trip>>
    fun getTripById(tripId: String): Flow<Trip?>
    fun getItemsForTrip(tripId: String): Flow<List<ShoppingItem>>
    suspend fun createTrip(trip: Trip): Result<Trip>
    suspend fun updateTrip(trip: Trip): Result<Trip>
    suspend fun deleteTrip(tripId: String): Result<Unit>
    suspend fun addItem(item: ShoppingItem): Result<ShoppingItem>
    suspend fun updateItem(item: ShoppingItem): Result<ShoppingItem>
    suspend fun deleteItem(itemId: String, tripId: String): Result<Unit>
    fun getSummaryStats(userId: String): Flow<SummaryStats>
    suspend fun syncWithFirestore(userId: String)
}
