package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.dto.CreateLocationRequest

import com.qrware.app.data.model.LocationType
import com.qrware.app.data.dto.ZoneDTO

import com.qrware.app.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class LocationFormUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,

    // Lista do dropdownów
    //dodać funkcje do zones
    val zones: List<ZoneDTO> = emptyList(),
    val locationTypes: List<LocationType> = LocationType.values().toList(),

    // Pola formularza
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val zoneId: Long? = null,
    val type: LocationType = LocationType.SHELF,
    val aisle: String = "",
    val rack: String = "",
    val shelf: String = "",
    val bin: String = "",
    val capacityVolume: String = "",
    val capacityWeight: String = "",
    val capacityItems: String = "",
    val temperatureControlled: Boolean = false,
    val temperatureMin: String = "",
    val temperatureMax: String = "",
    val humidityControlled: Boolean = false,
    val humidityMin: String = "",
    val humidityMax: String = "",
    val hazardousMaterials: Boolean = false,
    val fragileItems: Boolean = false,
    val securityLevel: String = "1",
    val active: Boolean = true,
    val pickable: Boolean = true,
    val receivable: Boolean = true,
    val qrCode: String = "",
    val barcode: String = "",
    val xCoordinate: String = "",
    val yCoordinate: String = "",
    val zCoordinate: String = ""
)

open class LocationFormViewModel(
    protected val repository: LocationRepository
) : ViewModel() {

    protected val _uiState = MutableStateFlow(LocationFormUiState())
    val uiState: StateFlow<LocationFormUiState> = _uiState.asStateFlow()

    init {
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // POPRAWKA 1: Przekaż FAKTYCZNE wartości Int, a nie typ Int.Companion
                // Używamy dużej liczby 'size' (zgodnie z ApiService), aby pobrać wszystkie aktywne strefy
                val paginatedResponse = repository.getActiveZones(page = 0, size = 1000)

                // POPRAWKA 2: Wyciągnij listę 'content' z odpowiedzi
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        zones = paginatedResponse.content // Zaktualizuj stan listą stref
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd ładowania stref: ${e.message}") }
            }
        }
    }

    // Metody 'on...Change'
    fun onCodeChange(v: String) = _uiState.update { it.copy(code = v) }
    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onZoneSelected(v: ZoneDTO?) = _uiState.update { it.copy(zoneId = v?.id) }
    fun onTypeSelected(v: LocationType) = _uiState.update { it.copy(type = v) }
    fun onAisleChange(v: String) = _uiState.update { it.copy(aisle = v) }
    fun onRackChange(v: String) = _uiState.update { it.copy(rack = v) }
    fun onShelfChange(v: String) = _uiState.update { it.copy(shelf = v) }
    fun onBinChange(v: String) = _uiState.update { it.copy(bin = v) }
    fun onCapacityVolumeChange(v: String) = _uiState.update { it.copy(capacityVolume = v) }
    fun onCapacityWeightChange(v: String) = _uiState.update { it.copy(capacityWeight = v) }
    fun onCapacityItemsChange(v: String) = _uiState.update { it.copy(capacityItems = v) }
    fun onTempControlledChange(v: Boolean) = _uiState.update { it.copy(temperatureControlled = v) }
    fun onTempMinChange(v: String) = _uiState.update { it.copy(temperatureMin = v) }
    fun onTempMaxChange(v: String) = _uiState.update { it.copy(temperatureMax = v) }
    fun onHumidityControlledChange(v: Boolean) = _uiState.update { it.copy(humidityControlled = v) }
    fun onHumidityMinChange(v: String) = _uiState.update { it.copy(humidityMin = v) }
    fun onHumidityMaxChange(v: String) = _uiState.update { it.copy(humidityMax = v) }
    fun onHazardousChange(v: Boolean) = _uiState.update { it.copy(hazardousMaterials = v) }
    fun onFragileChange(v: Boolean) = _uiState.update { it.copy(fragileItems = v) }
    fun onSecurityLevelChange(v: String) = _uiState.update { it.copy(securityLevel = v) }
    fun onActiveChange(v: Boolean) = _uiState.update { it.copy(active = v) }
    fun onPickableChange(v: Boolean) = _uiState.update { it.copy(pickable = v) }
    fun onReceivableChange(v: Boolean) = _uiState.update { it.copy(receivable = v) }
    fun onQrCodeChange(v: String) = _uiState.update { it.copy(qrCode = v) }
    fun onBarcodeChange(v: String) = _uiState.update { it.copy(barcode = v) }
    fun onXCoordinateChange(v: String) = _uiState.update { it.copy(xCoordinate = v) }
    fun onYCoordinateChange(v: String) = _uiState.update { it.copy(yCoordinate = v) }
    fun onZCoordinateChange(v: String) = _uiState.update { it.copy(zCoordinate = v) }

    // Helpery do konwersji
    protected fun String.toBigDecimalOrNull(): BigDecimal? = try { BigDecimal(this) } catch (e: Exception) { null }
    protected fun String.toIntOrNull(): Int? = try { this.toInt() } catch (e: Exception) { null }
}

class AddLocationViewModel(repository: LocationRepository) : LocationFormViewModel(repository) {
    fun createLocation() {
        val state = _uiState.value
        if (state.code.isBlank() || state.name.isBlank() || state.zoneId == null) {
            _uiState.update { it.copy(error = "Kod, Nazwa i Strefa są wymagane.") }
            return
        }

        val request = CreateLocationRequest(
            code = state.code,
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
            securityLevel = state.securityLevel.toIntOrNull() ?: 1,
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
                repository.createLocation(request)
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd tworzenia: ${e.message}") }
            }
        }
    }
}

// Fabryka
class AddLocationViewModelFactory(
    private val repository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddLocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddLocationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}