package com.example.shared.ui.viewmodel.UserManagament

// import android.util.Log // removed for KMP
// Removed Android ViewModel dependency for KMP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.shared.data.model.LoginRequest
import com.example.shared.data.repository.AuthRepository
import com.example.shared.security.TokenManager
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
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, pass: String) {
        scope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            val result = authRepository.login(LoginRequest(username, pass))
            result.onSuccess { authResponse ->
                println("LoginViewModel: login success, token=${authResponse.token}")
                tokenManager.saveToken(authResponse.token)
                _uiState.value = LoginUiState(loginSuccess = true)
            }.onFailure {
                _uiState.value = LoginUiState(error = it.message ?: "Unknown error")
            }
        }
    }
}

