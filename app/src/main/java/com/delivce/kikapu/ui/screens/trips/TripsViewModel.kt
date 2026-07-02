package com.delivce.kikapu.ui.screens.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.Trip
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
                        filteredTrips = applyFilters(trips, it.filterStatus, it.sortOrder),
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
                    filteredTrips = applyFilters(it.trips, event.status, it.sortOrder)
                )
            }
            is TripsEvent.SortBy -> _uiState.update {
                it.copy(
                    sortOrder = event.order,
                    filteredTrips = applyFilters(it.trips, it.filterStatus, event.order)
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
        status: com.delivce.kikapu.domain.model.TripStatus?,
        sortOrder: SortOrder
    ): List<Trip> {
        val filtered = if (status != null) trips.filter { it.status == status } else trips
        return when (sortOrder) {
            SortOrder.DATE_ASC -> filtered.sortedBy { it.date }
            SortOrder.DATE_DESC -> filtered.sortedByDescending { it.date }
            SortOrder.BUDGET_HIGH -> filtered.sortedByDescending { it.budget }
            SortOrder.BUDGET_LOW -> filtered.sortedBy { it.budget }
        }
    }
}
