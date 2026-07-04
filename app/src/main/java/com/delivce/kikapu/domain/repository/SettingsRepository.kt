package com.delivce.kikapu.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** The share of trips a user wants to stay within budget for, as a whole percent (e.g. 80). */
    fun getSuccessRateTarget(userId: String): Flow<Int>
    suspend fun setSuccessRateTarget(userId: String, percent: Int)
}
