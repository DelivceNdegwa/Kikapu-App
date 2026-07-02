package com.delivce.kikapu.data.repository

import com.delivce.kikapu.data.local.dao.ItemDao
import com.delivce.kikapu.data.local.dao.TripDao
import com.delivce.kikapu.data.mappers.toDomain
import com.delivce.kikapu.data.mappers.toEntity
import com.delivce.kikapu.data.mappers.toFirestoreMap
import com.delivce.kikapu.data.mappers.toShoppingItem
import com.delivce.kikapu.data.mappers.toTrip
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.SummaryStats
import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TRIPS_COLLECTION = "trips"
private const val ITEMS_COLLECTION = "items"

class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val itemDao: ItemDao,
    private val firestore: FirebaseFirestore
) : TripRepository {

    override fun getTrips(userId: String): Flow<List<Trip>> =
        tripDao.getTripsWithItems(userId).map { list -> list.map { it.trip.toDomain() } }

    override fun getTripById(tripId: String): Flow<Trip?> =
        tripDao.getTripWithItemsById(tripId).map { it?.trip?.toDomain() }

    override fun getItemsForTrip(tripId: String): Flow<List<ShoppingItem>> =
        tripDao.getItemsForTrip(tripId).map { list -> list.map { it.toDomain() } }

    override suspend fun createTrip(trip: Trip): Result<Trip> = runCatching {
        tripDao.upsertTrip(trip.toEntity())
        runCatching {
            firestore.collection(TRIPS_COLLECTION).document(trip.id)
                .set(trip.toFirestoreMap()).await()
        }
        trip
    }

    override suspend fun updateTrip(trip: Trip): Result<Trip> = runCatching {
        tripDao.upsertTrip(trip.toEntity())
        runCatching {
            firestore.collection(TRIPS_COLLECTION).document(trip.id)
                .set(trip.toFirestoreMap()).await()
        }
        trip
    }

    override suspend fun deleteTrip(tripId: String): Result<Unit> = runCatching {
        tripDao.deleteTrip(tripId)
        runCatching {
            firestore.collection(TRIPS_COLLECTION).document(tripId).delete().await()
        }
    }

    override suspend fun addItem(item: ShoppingItem): Result<ShoppingItem> = runCatching {
        tripDao.upsertItem(item.toEntity())
        runCatching {
            firestore.collection(TRIPS_COLLECTION).document(item.tripId)
                .collection(ITEMS_COLLECTION).document(item.id)
                .set(item.toFirestoreMap()).await()
        }
        item
    }

    override suspend fun updateItem(item: ShoppingItem): Result<ShoppingItem> = runCatching {
        tripDao.upsertItem(item.toEntity())
        runCatching {
            firestore.collection(TRIPS_COLLECTION).document(item.tripId)
                .collection(ITEMS_COLLECTION).document(item.id)
                .set(item.toFirestoreMap()).await()
        }
        item
    }

    override suspend fun deleteItem(itemId: String, tripId: String): Result<Unit> = runCatching {
        tripDao.deleteItem(itemId)
        runCatching {
            firestore.collection(TRIPS_COLLECTION).document(tripId)
                .collection(ITEMS_COLLECTION).document(itemId)
                .delete().await()
        }
    }

    override fun getSummaryStats(userId: String): Flow<SummaryStats> = combine(
        tripDao.getUpcomingTripsCount(userId),
        tripDao.getCompletedTripsCount(userId),
        itemDao.getRestockDueCount(userId, System.currentTimeMillis()),
        tripDao.getTotalSavedOrOverspent(userId)
    ) { upcoming, completed, itemsDue, savedOrOverspent ->
        SummaryStats(
            upcomingTripsCount = upcoming,
            completedTripsCount = completed,
            itemsDueCount = itemsDue,
            totalSavedOrOverspent = savedOrOverspent ?: 0.0
        )
    }

    override suspend fun syncWithFirestore(userId: String) {
        if (userId.isBlank()) return
        try {
            val tripDocs = firestore.collection(TRIPS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            for (tripDoc in tripDocs.documents) {
                val trip = tripDoc.data?.toTrip() ?: continue
                tripDao.upsertTrip(trip.toEntity())

                val itemDocs = firestore.collection(TRIPS_COLLECTION).document(trip.id)
                    .collection(ITEMS_COLLECTION)
                    .get()
                    .await()
                val items = itemDocs.documents.mapNotNull { it.data?.toShoppingItem() }
                if (items.isNotEmpty()) {
                    tripDao.upsertItems(items.map { it.toEntity() })
                }
            }
        } catch (_: Exception) {
            // Offline is a valid state - swallow and keep existing Room data.
        }
    }
}
