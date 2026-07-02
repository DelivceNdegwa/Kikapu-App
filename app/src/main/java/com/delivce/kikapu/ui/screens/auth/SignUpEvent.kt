package com.delivce.kikapu.ui.screens.auth

sealed class SignUpEvent {
    data class EmailChanged(val email: String) : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    data class FullNameChanged(val fullName: String) : SignUpEvent()
    object SignUpClicked : SignUpEvent()
    data class GoogleSignInClicked(val idToken: String) : SignUpEvent()
    object ClearError : SignUpEvent()
}