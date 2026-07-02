package com.delivce.kikapu.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.SummaryStats
import com.delivce.kikapu.domain.model.User
import com.delivce.kikapu.domain.repository.AuthRepository
import com.delivce.kikapu.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    authRepository: AuthRepository,
    tripRepository: TripRepository
) : ViewModel() {

    val user: User? = authRepository.currentUser

    val stats: StateFlow<SummaryStats> = tripRepository.getSummaryStats(user?.id ?: "")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryStats())
}
