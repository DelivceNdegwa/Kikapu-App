package com.delivce.kikapu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.SummaryStats
import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.repository.TripRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TripRepository,
    auth: FirebaseAuth
) : ViewModel() {

    private val userId = auth.currentUser?.uid ?: ""

    val userName: String = auth.currentUser?.displayName
        ?.trim()
        ?.substringBefore(" ")
        ?.takeIf { it.isNotBlank() }
        ?: "there"

    val summaryStats: StateFlow<SummaryStats> = repository.getSummaryStats(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryStats())

    val recentTrips: StateFlow<List<Trip>> = repository.getTrips(userId)
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
