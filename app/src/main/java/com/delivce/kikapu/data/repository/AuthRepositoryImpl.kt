package com.delivce.kikapu.data.repository

import com.delivce.kikapu.data.dto.UserDto
import com.delivce.kikapu.domain.model.AuthResult
import com.delivce.kikapu.domain.model.User
import com.delivce.kikapu.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: User?
        get() = firebaseAuth.currentUser?.toDto()?.toDomain()

    override suspend fun login(email: String, password: String): AuthResult<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it.toDto().toDomain())
            } ?: AuthResult.Error("Login failed")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }

    override suspend fun signUp(email: String, password: String, fullName: String): AuthResult<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                user.updateProfile(profileUpdates).await()
                AuthResult.Success(user.toDto().toDomain())
            } ?: AuthResult.Error("Registration failed")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResult<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.let {
                AuthResult.Success(it.toDto().toDomain())
            } ?: AuthResult.Error("Google login failed")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    // Mapper: Firebase -> DTO
    private fun FirebaseUser.toDto(): UserDto {
        return UserDto(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString()
        )
    }

    // Mapper: DTO -> Domain Model
    private fun UserDto.toDomain(): User {
        return User(
            id = uid,
            email = email,
            fullName = displayName,
            profilePictureUrl = photoUrl
        )
    }
}
