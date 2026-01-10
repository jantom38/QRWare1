package com.qrware.app.ui.viewmodel

import androidx.lifecycle.*
import com.qrware.app.data.model.SystemStatus
import com.qrware.app.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HealthUiState {
    object Loading : HealthUiState()
    data class Success(val systemStatus: SystemStatus) : HealthUiState()
    data class Error(val message: String) : HealthUiState()
}

class HealthViewModel(private val healthRepository: HealthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthUiState>(HealthUiState.Loading)
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        fetchSystemStatus()
    }

    fun fetchSystemStatus() {
        _uiState.value = HealthUiState.Loading
        viewModelScope.launch {
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

class HealthViewModelFactory(
    private val healthRepository: HealthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthViewModel(healthRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}