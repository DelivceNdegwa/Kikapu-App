package com.delivce.kikapu.data.mappers

import com.delivce.kikapu.data.dto.UserDto
import com.delivce.kikapu.domain.model.User
import com.google.firebase.auth.FirebaseUser


fun FirebaseUser.toDto(): UserDto {
    return UserDto(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString()
    )
}

fun UserDto.toDomain(): User {
    return User(
        id = uid,
        email = email,
        fullName = displayName,
        profilePictureUrl = photoUrl
    )
}