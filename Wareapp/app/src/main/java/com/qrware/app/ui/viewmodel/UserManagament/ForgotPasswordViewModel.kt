package com.qrware.app.ui.viewmodel.UserManagament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class ForgotPasswordViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false) }
            val result = authRepository.requestPasswordReset(email)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, success = true) }
            } else {
                // Even if it fails (e.g. email not found), for security reasons we might want to show success or generic message.
                // But here we follow repository result.
                // The backend usually returns 200 OK even if email not found to prevent enumeration.
                // If repository throws exception (network error), we show error.
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Błąd wysyłania żądania") }
            }
        }
    }
}

class ForgotPasswordViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForgotPasswordViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}