package com.example.shared.ui.viewmodel.UserManagament

// Removed Android ViewModel dependency for KMP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.shared.data.model.RegisterRequest
import com.example.shared.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Stan UI dla ekranu rejestracji
data class RegisterUiState(
    val isLoading: Boolean = false,
    val registrationSuccess: Boolean = false,
    val error: String? = null
)

class RegisterViewModel(private val authRepository: AuthRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(username: String, email: String, pass: String,firstName: String, lastName: String) {
        scope.launch {
            _uiState.value = RegisterUiState(isLoading = true)

            val request = RegisterRequest(username = username, email = email, password = pass, firstName = firstName, // <-- ADD THIS
                lastName = lastName)
            val result = authRepository.register(request)

            result.onSuccess {
                // Sukces! Ustawiamy flagę, na którą UI będzie mógł zareagować.
                _uiState.value = RegisterUiState(registrationSuccess = true)
            }.onFailure {
                // Błąd. Przekazujemy komunikat do UI.
                _uiState.value = RegisterUiState(error = it.message ?: "Wystąpił nieznany błąd")
            }
        }
    }
}

