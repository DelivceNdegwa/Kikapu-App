package com.delivce.kikapu.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.TripStatus
import com.delivce.kikapu.domain.repository.SettingsRepository
import com.delivce.kikapu.domain.repository.TripRepository
import com.delivce.kikapu.domain.usecase.currentSavingsStreakTrips
import com.delivce.kikapu.domain.usecase.improvementSincePreviousTrip
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: TripRepository,
    private val settingsRepository: SettingsRepository,
    auth: FirebaseAuth
) : ViewModel() {

    private val userId = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSuccessRateTarget(userId).collect { target ->
                _uiState.update { it.copy(successRateTarget = target) }
            }
        }
        viewModelScope.launch {
            repository.getTrips(userId).collect { trips ->
                // Ordered by when the trip was actually completed (updatedAt), not the date it was
                // originally scheduled for — a trip planned for last week but only wrapped up
                // today should show as the newest one on the trend, not get buried by its
                // scheduled date.
                val completed = trips
                    .filter { it.status == TripStatus.COMPLETED }
                    .sortedByDescending { it.updatedAt }
                _uiState.update { state ->
                    state.copy(
                        completedTrips = completed,
                        // Default to the latest trip so the detail panel is never empty, but
                        // don't clobber a selection the user already made by tapping a bar.
                        selectedTripId = state.selectedTripId ?: completed.firstOrNull()?.id,
                        streakTrips = currentSavingsStreakTrips(completed),
                        improvementPercent = improvementSincePreviousTrip(completed),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: ProgressEvent) {
        when (event) {
            is ProgressEvent.WindowChanged -> _uiState.update { it.copy(chartWindow = event.window) }
            is ProgressEvent.OutcomeFilterChanged -> _uiState.update { it.copy(outcomeFilter = event.filter) }
            is ProgressEvent.TripSelected -> _uiState.update { it.copy(selectedTripId = event.tripId) }
        }
    }
}
