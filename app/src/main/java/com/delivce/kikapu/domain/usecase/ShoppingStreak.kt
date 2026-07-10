package com.delivce.kikapu.domain.usecase

import com.delivce.kikapu.domain.model.Trip

/** Savings % for a completed trip; null if budget is 0 (there's nothing to measure against). */
fun Trip.savingsPercent(): Double? {
    if (budget <= 0) return null
    return ((budget - totalSpent) / budget) * 100.0
}

/**
 * Consecutive most-recent completed trips (newest first, by when each was actually completed —
 * `updatedAt` — not the date it was originally scheduled for) that stayed within budget — the
 * Duolingo-style "current streak." Breaks at the first trip that either overspent or has no
 * budget to measure against.
 */
fun currentSavingsStreakTrips(trips: List<Trip>): List<Trip> {
    val ordered = trips.sortedByDescending { it.updatedAt }
    val streakTrips = mutableListOf<Trip>()
    for (trip in ordered) {
        val percent = trip.savingsPercent() ?: break
        if (percent < 0) break
        streakTrips += trip
    }
    return streakTrips
}

fun currentSavingsStreak(trips: List<Trip>): Int = currentSavingsStreakTrips(trips).size

/**
 * Percentage-point delta between the latest completed trip's savings % and the one before it.
 * Null if there aren't at least two completed trips with a usable budget to compare.
 */
fun improvementSincePreviousTrip(trips: List<Trip>): Double? {
    val ordered = trips.sortedByDescending { it.updatedAt }
    if (ordered.size < 2) return null
    val latest = ordered[0].savingsPercent() ?: return null
    val previous = ordered[1].savingsPercent() ?: return null
    return latest - previous
}
