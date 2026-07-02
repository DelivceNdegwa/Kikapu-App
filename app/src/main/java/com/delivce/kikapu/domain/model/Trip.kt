package com.delivce.kikapu.domain.model

data class Trip(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val budget: Double = 0.0,
    val date: Long = 0L,
    val reminderTime: Long? = null,
    val status: TripStatus = TripStatus.UPCOMING,
    val totalSpent: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TripStatus { UPCOMING, ACTIVE, COMPLETED, CANCELLED }
