package com.delivce.kikapu.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.SummaryStats
import com.delivce.kikapu.domain.model.User
import com.delivce.kikapu.domain.repository.AuthRepository
import com.delivce.kikapu.domain.repository.SettingsRepository
import com.delivce.kikapu.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    authRepository: AuthRepository,
    tripRepository: TripRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val user: User? = authRepository.currentUser
    private val userId = user?.id ?: ""

    val stats: StateFlow<SummaryStats> = tripRepository.getSummaryStats(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryStats())

    val successRateTarget: StateFlow<Int> = settingsRepository.getSuccessRateTarget(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 80)

    fun setSuccessRateTarget(percent: Int) {
        viewModelScope.launch {
            settingsRepository.setSuccessRateTarget(userId, percent.coerceIn(0, 100))
        }
    }
}
