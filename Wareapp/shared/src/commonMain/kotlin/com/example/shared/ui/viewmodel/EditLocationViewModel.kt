package com.example.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.UpdateLocationRequest
import com.example.shared.data.model.LocationType
import com.example.shared.data.repository.LocationRepository
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditLocationViewModel(
    repository: LocationRepository,
    private val locationId: Long
) : LocationFormViewModel(repository) {

    init {
        loadLocationData()
    }

    private fun loadLocationData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val location = repository.getLocationById(locationId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        code = location.code,
                        name = location.name,
                        description = location.description ?: "",
                        zoneId = location.zone.id,
                        type = location.type ?: LocationType.SHELF,
                        aisle = location.aisle ?: "",
                        rack = location.rack ?: "",
                        shelf = location.shelf ?: "",
                        bin = location.bin ?: "",
                        // ZMIANA: toPlainString() (BigDecimal) -> toString() (Double/String)
                        capacityVolume = location.capacityVolume?.toString() ?: "",
                        capacityWeight = location.capacityWeight?.toString() ?: "",
                        capacityItems = location.capacityItems?.toString() ?: "",
                        temperatureControlled = location.temperatureControlled,
                        temperatureMin = location.temperatureMin?.toString() ?: "",
                        temperatureMax = location.temperatureMax?.toString() ?: "",
                        humidityControlled = location.humidityControlled,
                        humidityMin = location.humidityMin?.toString() ?: "",
                        humidityMax = location.humidityMax?.toString() ?: "",
                        hazardousMaterials = location.hazardousMaterials,
                        fragileItems = location.fragileItems,
                        securityLevel = location.securityLevel.toString(),
                        active = location.active,
                        pickable = location.pickable,
                        receivable = location.receivable,
                        qrCode = location.qrCode ?: "",
                        barcode = location.barcode ?: "",
                        // ZMIANA: toPlainString() -> toString()
                        xCoordinate = location.xCoordinate?.toString() ?: "",
                        yCoordinate = location.yCoordinate?.toString() ?: "",
                        zCoordinate = location.zCoordinate?.toString() ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd ładowania danych: ${e.message}") }
            }
        }
    }

    fun updateLocation() {
        val state = _uiState.value
        // Walidacja
        if (state.name.isBlank() || state.zoneId == null) {
            _uiState.update { it.copy(error = "Nazwa i Strefa są wymagane.") }
            return
        }

        val request = UpdateLocationRequest(
            name = state.name,
            description = state.description.takeIf { it.isNotBlank() },
            zoneId = state.zoneId,
            type = state.type,
            aisle = state.aisle.takeIf { it.isNotBlank() },
            rack = state.rack.takeIf { it.isNotBlank() },
            shelf = state.shelf.takeIf { it.isNotBlank() },
            bin = state.bin.takeIf { it.isNotBlank() },
            // ZMIANA: toBigDecimalOrNull() -> toDoubleOrNull()
            capacityVolume = state.capacityVolume.toDoubleOrNull(),
            capacityWeight = state.capacityWeight.toDoubleOrNull(),
            capacityItems = state.capacityItems.toIntOrNull(),
            temperatureControlled = state.temperatureControlled,
            temperatureMin = state.temperatureMin.toIntOrNull(),
            temperatureMax = state.temperatureMax.toIntOrNull(),
            humidityControlled = state.humidityControlled,
            humidityMin = state.humidityMin.toIntOrNull(),
            humidityMax = state.humidityMax.toIntOrNull(),
            hazardousMaterials = state.hazardousMaterials,
            fragileItems = state.fragileItems,
            securityLevel = state.securityLevel.toIntOrNull(),
            active = state.active,
            pickable = state.pickable,
            receivable = state.receivable,
            qrCode = state.qrCode.takeIf { it.isNotBlank() },
            barcode = state.barcode.takeIf { it.isNotBlank() },
            // ZMIANA: toBigDecimalOrNull() -> toDoubleOrNull()
            xCoordinate = state.xCoordinate.toDoubleOrNull(),
            yCoordinate = state.yCoordinate.toDoubleOrNull(),
            zCoordinate = state.zCoordinate.toDoubleOrNull()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.updateLocation(locationId, request)
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd aktualizacji: ${e.message}") }
            }
        }
    }
}