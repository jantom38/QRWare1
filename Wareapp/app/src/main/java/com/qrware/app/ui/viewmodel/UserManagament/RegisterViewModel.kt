package com.qrware.app.ui.viewmodel.UserManagament

import androidx.lifecycle.*
import com.qrware.app.data.model.RegisterRequest
import com.qrware.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val registrationSuccess: Boolean = false,
    val error: String? = null
)

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(username: String, email: String, pass: String,firstName: String, lastName: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)

            val request = RegisterRequest(username = username, email = email, password = pass, firstName = firstName,
                lastName = lastName)
            val result = authRepository.register(request)

            result.onSuccess {
                _uiState.value = RegisterUiState(registrationSuccess = true)
            }.onFailure {
                _uiState.value = RegisterUiState(error = it.message ?: "Wystąpił nieznany błąd")
            }
        }
    }
}

class RegisterViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}