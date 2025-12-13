package com.example.shared.ui.viewmodel

// Removed Android ViewModel dependency for KMP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.shared.data.model.SystemStatus
import com.example.shared.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HealthUiState {
    object Loading : HealthUiState()
    data class Success(val systemStatus: SystemStatus) : HealthUiState()
    data class Error(val message: String) : HealthUiState()
}

class HealthViewModel(private val healthRepository: HealthRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<HealthUiState>(HealthUiState.Loading)
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        fetchSystemStatus()
    }

    fun fetchSystemStatus() {
        _uiState.value = HealthUiState.Loading
        scope.launch {
            healthRepository.getSystemStatus()
                .onSuccess { status ->
                    _uiState.value = HealthUiState.Success(status)
                }
                .onFailure { error ->
                    _uiState.value = HealthUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

