package com.delivce.kikapu.data.repository

import com.delivce.kikapu.domain.model.AuthResult
import com.delivce.kikapu.domain.model.User


interface AuthRepository {
    val currentUser: User?
    suspend fun login(email: String, password: String): AuthResult<User>
    suspend fun signUp(email: String, password: String, fullName: String): AuthResult<User>
    suspend fun loginWithGoogle(idToken: String): AuthResult<User>
    fun logout()
}