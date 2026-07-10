package com.delivce.kikapu.data.repository

import com.delivce.kikapu.data.local.dao.SettingsDao
import com.delivce.kikapu.data.local.entity.SettingsEntity
import com.delivce.kikapu.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val DEFAULT_SUCCESS_RATE_TARGET = 80

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun getSuccessRateTarget(userId: String): Flow<Int> =
        settingsDao.getSettings(userId).map { it?.successRateTarget ?: DEFAULT_SUCCESS_RATE_TARGET }

    override suspend fun setSuccessRateTarget(userId: String, percent: Int) {
        settingsDao.upsertSettings(SettingsEntity(userId = userId, successRateTarget = percent))
    }
}
