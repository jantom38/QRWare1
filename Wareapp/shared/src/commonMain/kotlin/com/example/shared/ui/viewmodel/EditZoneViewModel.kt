package com.example.shared.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.UpdateZoneRequest
import com.example.shared.data.repository.ZoneRepository
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditZoneViewModel(
    repository: ZoneRepository,
    private val zoneId: Long
) : ZoneFormViewModel(repository) {

    init {
        loadZoneData()
    }

    private fun loadZoneData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val zone = repository.getZoneById(zoneId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        code = zone.code,
                        name = zone.name,
                        description = zone.description ?: "",
                        type = zone.type,
                        active = zone.active,
                        temperatureControlled = zone.temperatureControlled,
                        temperatureMin = zone.temperatureMin?.toString() ?: "",
                        temperatureMax = zone.temperatureMax?.toString() ?: "",
                        humidityControlled = zone.humidityControlled,
                        humidityMin = zone.humidityMin?.toString() ?: "",
                        humidityMax = zone.humidityMax?.toString() ?: "",
                        securityLevel = zone.securityLevel.toString(),
                        hazardousMaterials = zone.hazardousMaterials,
                        fragileItems = zone.fragileItems,
                        pickingPriority = zone.pickingPriority.toString(),
                        manager = zone.manager ?: "",
                        contactInfo = zone.contactInfo ?: "",
                        color = zone.color ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd ładowania danych: ${e.message}") }
            }
        }
    }

    fun updateZone() {
        val state = _uiState.value

        if (state.code.isBlank() || state.name.isBlank()) {
            _uiState.update { it.copy(error = "Kod i Nazwa są wymagane.") }
            return
        }

        val request = UpdateZoneRequest(
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
            securityLevel = state.securityLevel.toIntOrNull(),
            hazardousMaterials = state.hazardousMaterials,
            fragileItems = state.fragileItems,
            pickingPriority = state.pickingPriority.toIntOrNull(),
            manager = state.manager.takeIf { it.isNotBlank() },
            contactInfo = state.contactInfo.takeIf { it.isNotBlank() },
            color = state.color.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.updateZone(zoneId, request)
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd aktualizacji strefy: ${e.message}") }
            }
        }
    }
}