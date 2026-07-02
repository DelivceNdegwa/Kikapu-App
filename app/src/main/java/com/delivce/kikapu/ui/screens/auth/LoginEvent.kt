package com.delivce.kikapu.ui.screens.auth

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object LoginClicked : LoginEvent()
    data class GoogleSignInClicked(val idToken: String) : LoginEvent()
    object ClearError : LoginEvent()
}
