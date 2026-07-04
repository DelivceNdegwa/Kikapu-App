package com.delivce.kikapu.ui.screens.progress

import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.usecase.savingsPercent

sealed class ChartWindow(val label: String) {
    data class LastN(val n: Int) : ChartWindow("LAST $n")
    object All : ChartWindow("ALL")
    data class Custom(val startMillis: Long, val endMillis: Long) : ChartWindow("CUSTOM")

    companion object {
        val presets: List<ChartWindow> = listOf(LastN(5), LastN(10), LastN(20), All)
    }
}

enum class OutcomeFilter(val label: String) {
    ALL("ALL"),
    SAVED_ONLY("SAVED ONLY"),
    OVERSPENT_ONLY("OVERSPENT ONLY")
}

data class ProgressUiState(
    val completedTrips: List<Trip> = emptyList(), // newest first
    val chartWindow: ChartWindow = ChartWindow.LastN(10),
    val outcomeFilter: OutcomeFilter = OutcomeFilter.ALL,
    val selectedTripId: String? = null,
    val streakTrips: List<Trip> = emptyList(), // newest first, the trips making up the current streak
    val improvementPercent: Double? = null,
    val successRateTarget: Int = 80,
    val isLoading: Boolean = true
) {
    val streak: Int get() = streakTrips.size

    // Track record across every completed trip, independent of the chart's own filters — a
    // moving goalpost that only shows "the last 5" would be a strange thing to hold yourself to.
    val successRate: Int get() =
        if (completedTrips.isEmpty()) 0
        else (completedTrips.count { (it.savingsPercent() ?: 0.0) >= 0 } * 100 / completedTrips.size)
}

/**
 * Trips matching both filters, still newest-first. Outcome is applied first, then the time
 * window — "LAST 5" + "SAVED ONLY" reads as "the last 5 trips I saved on," not "of my last 5
 * trips, whichever happened to save."
 */
fun ProgressUiState.windowedTrips(): List<Trip> {
    val byOutcome = when (outcomeFilter) {
        OutcomeFilter.ALL -> completedTrips
        OutcomeFilter.SAVED_ONLY -> completedTrips.filter { (it.savingsPercent() ?: 0.0) >= 0 }
        OutcomeFilter.OVERSPENT_ONLY -> completedTrips.filter { (it.savingsPercent() ?: 0.0) < 0 }
    }
    return when (val window = chartWindow) {
        is ChartWindow.LastN -> byOutcome.take(window.n)
        ChartWindow.All -> byOutcome
        is ChartWindow.Custom -> byOutcome.filter { it.updatedAt in window.startMillis..window.endMillis }
    }
}

sealed class ProgressEvent {
    data class WindowChanged(val window: ChartWindow) : ProgressEvent()
    data class OutcomeFilterChanged(val filter: OutcomeFilter) : ProgressEvent()
    data class TripSelected(val tripId: String) : ProgressEvent()
}
