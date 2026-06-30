package com.delivce.kikapu.ui.screens.auth

import com.delivce.kikapu.domain.model.AuthResult
import com.delivce.kikapu.domain.model.User

data class AuthUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val authResult: AuthResult<User>? = null
)

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)