package com.delivce.kikapu.ui.screens.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.model.TripStatus
import com.delivce.kikapu.domain.repository.TripRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val repository: TripRepository,
    auth: FirebaseAuth
) : ViewModel() {

    private val userId = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTrips(userId).collect { trips ->
                _uiState.update {
                    it.copy(
                        trips = trips,
                        filteredTrips = applyFilters(trips, it.filterStatus, it.sortOrder, it.searchQuery),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: TripsEvent) {
        when (event) {
            is TripsEvent.FilterByStatus -> _uiState.update {
                it.copy(
                    filterStatus = event.status,
                    filteredTrips = applyFilters(it.trips, event.status, it.sortOrder, it.searchQuery)
                )
            }
            is TripsEvent.SortBy -> _uiState.update {
                it.copy(
                    sortOrder = event.order,
                    filteredTrips = applyFilters(it.trips, it.filterStatus, event.order, it.searchQuery)
                )
            }
            is TripsEvent.SearchQueryChanged -> _uiState.update {
                it.copy(
                    searchQuery = event.query,
                    filteredTrips = applyFilters(it.trips, it.filterStatus, it.sortOrder, event.query)
                )
            }
            is TripsEvent.DeleteTrip -> deleteTrip(event.tripId)
            TripsEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            repository.deleteTrip(tripId).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to delete trip") }
            }
        }
    }

    private fun applyFilters(
        trips: List<Trip>,
        status: TripStatus?,
        sortOrder: SortOrder,
        searchQuery: String
    ): List<Trip> {
        val filtered = trips
            .filter { status == null || it.status == status }
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
        return when (sortOrder) {
            SortOrder.SMART -> filtered.sortedWith(smartTripOrder)
            SortOrder.DATE_ASC -> filtered.sortedBy { it.date }
            SortOrder.DATE_DESC -> filtered.sortedByDescending { it.date }
            SortOrder.BUDGET_HIGH -> filtered.sortedByDescending { it.budget }
            SortOrder.BUDGET_LOW -> filtered.sortedBy { it.budget }
        }
    }

    companion object {
        /**
         * Active trips (in progress) first, then upcoming ones soonest-first, then everything
         * else — completed and cancelled together — most-recently-touched first. Status group
         * always wins; the date field used to break a tie within a group depends on the group,
         * since "soonest" only makes sense for what hasn't happened yet.
         */
        private fun statusPriority(status: TripStatus): Int = when (status) {
            TripStatus.ACTIVE -> 0
            TripStatus.UPCOMING -> 1
            TripStatus.COMPLETED, TripStatus.CANCELLED -> 2
        }

        private val smartTripOrder = Comparator<Trip> { a, b ->
            val priority = statusPriority(a.status).compareTo(statusPriority(b.status))
            if (priority != 0) {
                priority
            } else if (a.status == TripStatus.ACTIVE || a.status == TripStatus.UPCOMING) {
                a.date.compareTo(b.date)
            } else {
                b.updatedAt.compareTo(a.updatedAt)
            }
        }
    }
}
