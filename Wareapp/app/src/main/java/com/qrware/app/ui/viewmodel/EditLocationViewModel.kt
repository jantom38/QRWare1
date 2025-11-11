package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.dto.UpdateLocationRequest
import com.qrware.app.data.model.LocationType
import com.qrware.app.data.repository.LocationRepository
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
                        capacityVolume = location.capacityVolume?.toPlainString() ?: "",
                        capacityWeight = location.capacityWeight?.toPlainString() ?: "",
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
                        xCoordinate = location.xCoordinate?.toPlainString() ?: "",
                        yCoordinate = location.yCoordinate?.toPlainString() ?: "",
                        zCoordinate = location.zCoordinate?.toPlainString() ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd ładowania danych: ${e.message}") }
            }
        }
    }

    fun updateLocation() {
        val state = _uiState.value
        // Walidacja (minimalna, można rozbudować)
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
            capacityVolume = state.capacityVolume.toBigDecimalOrNull(),
            capacityWeight = state.capacityWeight.toBigDecimalOrNull(),
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
            xCoordinate = state.xCoordinate.toBigDecimalOrNull(),
            yCoordinate = state.yCoordinate.toBigDecimalOrNull(),
            zCoordinate = state.zCoordinate.toBigDecimalOrNull()
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

// Fabryka
class EditLocationViewModelFactory(
    private val repository: LocationRepository,
    private val locationId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditLocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditLocationViewModel(repository, locationId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}