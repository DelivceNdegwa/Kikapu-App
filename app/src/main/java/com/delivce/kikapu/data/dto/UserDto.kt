package com.delivce.kikapu.data.dto

/**
 * Data Transfer Object representing the user data as received from the data source (Firebase)
 */
data class UserDto(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)
