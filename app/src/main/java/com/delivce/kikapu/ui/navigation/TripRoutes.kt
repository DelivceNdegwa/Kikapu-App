package com.delivce.kikapu.ui.navigation

object TripRoutes {
    const val CREATE_TRIP = "create_trip"
    const val ACTIVE_TRIP = "active_trip/{tripId}"

    fun activeTripRoute(tripId: String) = "active_trip/$tripId"
}
