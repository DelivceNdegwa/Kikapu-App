package com.delivce.kikapu.ui.screens.trips

import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.model.TripStatus

data class TripsUiState(
    val trips: List<Trip> = emptyList(),
    val filteredTrips: List<Trip> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val filterStatus: TripStatus? = null,
    val sortOrder: SortOrder = SortOrder.DATE_DESC
)

enum class SortOrder { DATE_ASC, DATE_DESC, BUDGET_HIGH, BUDGET_LOW }

sealed class TripsEvent {
    data class FilterByStatus(val status: TripStatus?) : TripsEvent()
    data class SortBy(val order: SortOrder) : TripsEvent()
    data class DeleteTrip(val tripId: String) : TripsEvent()
    object ClearError : TripsEvent()
}
