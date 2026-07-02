package com.delivce.kikapu.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.AuthResult
import com.delivce.kikapu.domain.model.User
import com.delivce.kikapu.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    val currentUser: User?
        get() = repository.currentUser

    private val _authUiState = MutableStateFlow(AuthUiState(user = repository.currentUser))
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _signUpUiState = MutableStateFlow(SignUpUiState())
    val signUpUiState: StateFlow<SignUpUiState> = _signUpUiState.asStateFlow()

    fun onLoginEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _loginUiState.update { it.copy(email = event.email) }
            is LoginEvent.PasswordChanged -> _loginUiState.update { it.copy(password = event.password) }
            LoginEvent.LoginClicked -> login()
            is LoginEvent.GoogleSignInClicked -> loginWithGoogle(event.idToken)
            LoginEvent.ClearError -> _loginUiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun login() {
        val email = _loginUiState.value.email
        val password = _loginUiState.value.password
        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.login(email, password)) {
                is AuthResult.Success -> {
                    _authUiState.update { it.copy(user = result.data) }
                    _loginUiState.update { it.copy(isLoading = false) }
                }
                is AuthResult.Error -> {
                    _loginUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                AuthResult.Loading -> Unit
            }
        }
    }

    private fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.loginWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    _authUiState.update { it.copy(user = result.data) }
                    _loginUiState.update { it.copy(isLoading = false) }
                }
                is AuthResult.Error -> {
                    _loginUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                AuthResult.Loading -> Unit
            }
        }
    }

    fun onSignUpEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> _signUpUiState.update { it.copy(email = event.email) }
            is SignUpEvent.PasswordChanged -> _signUpUiState.update { it.copy(password = event.password) }
            is SignUpEvent.FullNameChanged -> _signUpUiState.update { it.copy(fullName = event.fullName) }
            SignUpEvent.SignUpClicked -> signUp()
            is SignUpEvent.GoogleSignInClicked -> signUpWithGoogle(event.idToken)
            SignUpEvent.ClearError -> _signUpUiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun signUp() {
        val state = _signUpUiState.value
        viewModelScope.launch {
            _signUpUiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.signUp(state.email, state.password, state.fullName)) {
                is AuthResult.Success -> {
                    _authUiState.update { it.copy(user = result.data) }
                    _signUpUiState.update { it.copy(isLoading = false) }
                }
                is AuthResult.Error -> {
                    _signUpUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                AuthResult.Loading -> Unit
            }
        }
    }

    private fun signUpWithGoogle(idToken: String) {
        viewModelScope.launch {
            _signUpUiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.loginWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    _authUiState.update { it.copy(user = result.data) }
                    _signUpUiState.update { it.copy(isLoading = false) }
                }
                is AuthResult.Error -> {
                    _signUpUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                AuthResult.Loading -> Unit
            }
        }
    }

    // --- Session ---

    fun logout() {
        repository.logout()
        _authUiState.value = AuthUiState()
        _loginUiState.value = LoginUiState()
        _signUpUiState.value = SignUpUiState()
    }
}