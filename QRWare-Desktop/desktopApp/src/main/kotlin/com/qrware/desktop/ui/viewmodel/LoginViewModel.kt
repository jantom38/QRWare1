package com.qrware.desktop.ui.viewmodel

import com.qrware.shared.data.model.AuthState
import com.qrware.shared.data.repository.AuthRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class LoginUiState(
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    // Expose auth state from repository
    val authState: StateFlow<AuthState> = authRepository.authState
    
    /**
     * Attempt to log in with provided credentials
     */
    suspend fun login(username: String, password: String) {
        // Clear previous errors
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            usernameError = null,
            passwordError = null,
            errorMessage = null
        )
        
        // Validate input
        val validationError = validateLoginInput(username, password)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                usernameError = validationError.usernameError,
                passwordError = validationError.passwordError
            )
            return
        }
        
        try {
            val result = authRepository.login(username, password)
            
            result.fold(
                onSuccess = { userInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    val errorMessage = when {
                        error.message?.contains("401") == true || 
                        error.message?.contains("Unauthorized") == true -> 
                            "Invalid username or password"
                        error.message?.contains("timeout") == true ||
                        error.message?.contains("network") == true -> 
                            "Network error. Please check your connection."
                        error.message?.contains("500") == true -> 
                            "Server error. Please try again later."
                        else -> error.message ?: "Login failed. Please try again."
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Unexpected error: ${e.message}"
            )
        }
    }
    
    /**
     * Clear any error messages
     */
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            usernameError = null,
            passwordError = null,
            errorMessage = null
        )
    }
    
    /**
     * Reset UI state
     */
    fun resetState() {
        _uiState.value = LoginUiState()
    }
    
    /**
     * Check server connectivity
     */
    suspend fun checkServerConnection(): Result<Boolean> {
        return try {
            val result = authRepository.checkServerHealth()
            result.fold(
                onSuccess = { Result.success(true) },
                onFailure = { Result.success(false) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate login input fields
     */
    private fun validateLoginInput(username: String, password: String): ValidationError? {
        var usernameError: String? = null
        var passwordError: String? = null
        
        when {
            username.isBlank() -> usernameError = "Username is required"
            username.length < 2 -> usernameError = "Username is too short"
        }
        
        when {
            password.isBlank() -> passwordError = "Password is required"
            password.length < 3 -> passwordError = "Password is too short"
        }
        
        return if (usernameError != null || passwordError != null) {
            ValidationError(usernameError, passwordError)
        } else {
            null
        }
    }
    
    /**
     * Clean up resources when ViewModel is destroyed
     */
    fun dispose() {
        scope.cancel()
    }
}

private data class ValidationError(
    val usernameError: String?,
    val passwordError: String?
)