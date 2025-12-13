package com.example.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.ZoneDTO
import com.example.shared.data.repository.ZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageZonesUiState(
    val zones: List<ZoneDTO> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ManageZonesViewModel(private val zoneRepository: ZoneRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageZonesUiState())
    val uiState: StateFlow<ManageZonesUiState> = _uiState.asStateFlow()

    init {
        loadZones()
    }

    fun loadZones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Pobieramy wszystkie strefy (strona 0, rozmiar 1000 - uproszczenie)
                val response = zoneRepository.getZones(0, 1000)
                _uiState.update {
                    it.copy(
                        zones = response.content,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Błąd pobierania stref: ${e.message}")
                }
            }
        }
    }

    fun deleteZone(zoneId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                zoneRepository.deleteZone(zoneId)
                _uiState.update { it.copy(successMessage = "Strefa została usunięta") }
                loadZones() // Odśwież listę
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Nie można usunąć strefy: ${e.message}")
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}