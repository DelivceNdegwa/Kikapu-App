package com.delivce.kikapu.domain.model

data class User(
    val id: String,
    val email: String?,
    val fullName: String?,
    val profilePictureUrl: String? = null
)
