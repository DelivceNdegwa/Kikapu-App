package com.delivce.kikapu.ui.navigation

object TripRoutes {
    const val CREATE_TRIP = "create_trip"
    const val ACTIVE_TRIP = "active_trip/{tripId}"
    const val COMPLETE_TRIP = "complete_trip/{tripId}"

    fun activeTripRoute(tripId: String) = "active_trip/$tripId"
    fun completeTripRoute(tripId: String) = "complete_trip/$tripId"
}
