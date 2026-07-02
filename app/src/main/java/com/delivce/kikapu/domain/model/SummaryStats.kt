package com.delivce.kikapu.domain.model

data class SummaryStats(
    val upcomingTripsCount: Int = 0,
    val completedTripsCount: Int = 0,
    val itemsDueCount: Int = 0, // catalog items past their restock duration
    val totalSavedOrOverspent: Double = 0.0 // positive = saved, negative = overspent
)
