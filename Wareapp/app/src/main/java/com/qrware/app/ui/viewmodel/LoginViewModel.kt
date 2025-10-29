package com.qrware.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.qrware.app.data.repository.AuthRepository
import com.qrware.app.security.TokenManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            val result = authRepository.login(com.qrware.app.data.model.LoginRequest(username, pass))
            result.onSuccess { authResponse ->
                Log.d("LoginViewModel", "Logowanie udane. Otrzymany token: ${authResponse.token}")
                tokenManager.saveToken(authResponse.token)
                _uiState.value = LoginUiState(loginSuccess = true)
            }.onFailure {
                _uiState.value = LoginUiState(error = it.message ?: "Unknown error")
            }
        }
    }
}

// Factory dla ViewModel
class LoginViewModelFactory(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}