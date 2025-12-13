package com.example.shared.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.CreateZoneRequest
import com.example.shared.data.repository.ZoneRepository
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddZoneViewModel(repository: ZoneRepository) : ZoneFormViewModel(repository) {

    fun createZone() {
        val state = _uiState.value

        if (state.code.isBlank() || state.name.isBlank()) {
            _uiState.update { it.copy(error = "Kod i Nazwa są wymagane.") }
            return
        }

        val request = CreateZoneRequest(
            code = state.code,
            name = state.name,
            description = state.description.takeIf { it.isNotBlank() },
            type = state.type,
            active = state.active,
            temperatureControlled = state.temperatureControlled,
            temperatureMin = state.temperatureMin.toIntOrNull(),
            temperatureMax = state.temperatureMax.toIntOrNull(),
            humidityControlled = state.humidityControlled,
            humidityMin = state.humidityMin.toIntOrNull(),
            humidityMax = state.humidityMax.toIntOrNull(),
            securityLevel = state.securityLevel.toIntOrNull() ?: 1,
            hazardousMaterials = state.hazardousMaterials,
            fragileItems = state.fragileItems,
            pickingPriority = state.pickingPriority.toIntOrNull() ?: 5,
            manager = state.manager.takeIf { it.isNotBlank() },
            contactInfo = state.contactInfo.takeIf { it.isNotBlank() },
            color = state.color.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.createZone(request)
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd tworzenia strefy: ${e.message}") }
            }
        }
    }
}