package com.example.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.LocationDTO
import com.example.shared.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationUiState(
    val isLoading: Boolean = false,
    val locations: List<LocationDTO> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val activeFilter: Boolean? = true,
    val searchQuery: String = ""
)

class ManageLocationsViewModel(private val repository: LocationRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private val pageSize = 20

    init {
        loadLocations()
    }

    fun loadLocations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = if (_uiState.value.searchQuery.isBlank()) {
                    repository.getLocations(
                        page = _uiState.value.currentPage,
                        size = pageSize,
                        active = _uiState.value.activeFilter
                    )
                } else {
                    repository.searchLocations(
                        query = _uiState.value.searchQuery,
                        page = _uiState.value.currentPage,
                        size = pageSize,
                        active = _uiState.value.activeFilter
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        locations = response.content,
                        totalPages = response.totalPages,
                        currentPage = response.number
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd pobierania lokalizacji: ${e.message}") }
            }
        }
    }

    fun deleteLocation(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                repository.deleteLocation(id)
                _uiState.update { it.copy(successMessage = "Lokalizacja usunięta (dezaktywowana)") }
                loadLocations()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd usuwania: ${e.message}") }
            }
        }
    }

    fun filterByActiveStatus(active: Boolean?) {
        _uiState.update { it.copy(activeFilter = active, currentPage = 0) }
        loadLocations()
    }

    fun nextPage() {
        if (_uiState.value.currentPage < _uiState.value.totalPages - 1) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
            loadLocations()
        }
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 0) {
            _uiState.update { it.copy(currentPage = it.currentPage - 1) }
            loadLocations()
        }
    }

    fun searchLocations(query: String) {
        _uiState.update { it.copy(searchQuery = query, currentPage = 0) }
        loadLocations()
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}