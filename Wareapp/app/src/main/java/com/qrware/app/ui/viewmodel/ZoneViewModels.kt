package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.dto.CreateZoneRequest
import com.qrware.app.data.dto.UpdateZoneRequest
import com.qrware.app.data.model.ZoneType
import com.qrware.app.data.repository.ZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- STAN UI FORMULARZA ---

data class ZoneFormUiState(
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val type: ZoneType = ZoneType.STORAGE,
    val active: Boolean = true,

    // Warunki środowiskowe
    val temperatureControlled: Boolean = false,
    val temperatureMin: String = "",
    val temperatureMax: String = "",
    val humidityControlled: Boolean = false,
    val humidityMin: String = "",
    val humidityMax: String = "",

    // Właściwości
    val securityLevel: String = "1",
    val hazardousMaterials: Boolean = false,
    val fragileItems: Boolean = false,
    val pickingPriority: String = "5",

    // Info zarządcze
    val manager: String = "",
    val contactInfo: String = "",
    val color: String = "#FFFFFF",

    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

// --- VIEWMODEL: ADD ZONE ---

class AddZoneViewModel(private val zoneRepository: ZoneRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ZoneFormUiState())
    val uiState: StateFlow<ZoneFormUiState> = _uiState.asStateFlow()

    // Handlery zmian pól
    fun onCodeChange(v: String) = _uiState.update { it.copy(code = v) }
    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onTypeSelected(v: ZoneType) = _uiState.update { it.copy(type = v) }
    fun onActiveChange(v: Boolean) = _uiState.update { it.copy(active = v) }

    fun onTempControlledChange(v: Boolean) = _uiState.update { it.copy(temperatureControlled = v) }
    fun onTempMinChange(v: String) = _uiState.update { it.copy(temperatureMin = v) }
    fun onTempMaxChange(v: String) = _uiState.update { it.copy(temperatureMax = v) }

    fun onHumidityControlledChange(v: Boolean) = _uiState.update { it.copy(humidityControlled = v) }
    fun onHumidityMinChange(v: String) = _uiState.update { it.copy(humidityMin = v) }
    fun onHumidityMaxChange(v: String) = _uiState.update { it.copy(humidityMax = v) }

    fun onSecurityLevelChange(v: String) = _uiState.update { it.copy(securityLevel = v) }
    fun onHazardousChange(v: Boolean) = _uiState.update { it.copy(hazardousMaterials = v) }
    fun onFragileChange(v: Boolean) = _uiState.update { it.copy(fragileItems = v) }
    fun onPickingPriorityChange(v: String) = _uiState.update { it.copy(pickingPriority = v) }

    fun onManagerChange(v: String) = _uiState.update { it.copy(manager = v) }
    fun onContactInfoChange(v: String) = _uiState.update { it.copy(contactInfo = v) }
    fun onColorChange(v: String) = _uiState.update { it.copy(color = v) }

    fun createZone() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val request = CreateZoneRequest(
                    name = _uiState.value.name,
                    code = _uiState.value.code,
                    description = _uiState.value.description.ifBlank { null },
                    type = _uiState.value.type,
                    active = _uiState.value.active,
                    temperatureControlled = _uiState.value.temperatureControlled,
                    temperatureMin = _uiState.value.temperatureMin.toIntOrNull(),
                    temperatureMax = _uiState.value.temperatureMax.toIntOrNull(),
                    humidityControlled = _uiState.value.humidityControlled,
                    humidityMin = _uiState.value.humidityMin.toIntOrNull(),
                    humidityMax = _uiState.value.humidityMax.toIntOrNull(),
                    securityLevel = _uiState.value.securityLevel.toIntOrNull() ?: 1,
                    hazardousMaterials = _uiState.value.hazardousMaterials,
                    fragileItems = _uiState.value.fragileItems,
                    pickingPriority = _uiState.value.pickingPriority.toIntOrNull() ?: 5,
                    manager = _uiState.value.manager.ifBlank { null },
                    contactInfo = _uiState.value.contactInfo.ifBlank { null },
                    color = _uiState.value.color.ifBlank { null }
                )
                zoneRepository.createZone(request)
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Błąd tworzenia strefy") }
            }
        }
    }

    private fun validateInput(): Boolean {
        if (_uiState.value.code.isBlank() || _uiState.value.name.isBlank()) {
            _uiState.update { it.copy(error = "Kod i Nazwa są wymagane") }
            return false
        }
        return true
    }
}

// --- VIEWMODEL: EDIT ZONE ---

class EditZoneViewModel(
    private val zoneRepository: ZoneRepository,
    private val zoneId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(ZoneFormUiState(isLoading = true))
    val uiState: StateFlow<ZoneFormUiState> = _uiState.asStateFlow()

    init {
        loadZone()
    }

    // Te same handlery co w AddZoneViewModel (można by wydzielić bazowy ViewModel, ale duplikacja jest bezpieczniejsza dla Compose)
    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    // Code zazwyczaj jest zablokowany przy edycji, ale handler może zostać
    fun onCodeChange(v: String) = _uiState.update { it.copy(code = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onTypeSelected(v: ZoneType) = _uiState.update { it.copy(type = v) }
    fun onActiveChange(v: Boolean) = _uiState.update { it.copy(active = v) }
    fun onTempControlledChange(v: Boolean) = _uiState.update { it.copy(temperatureControlled = v) }
    fun onTempMinChange(v: String) = _uiState.update { it.copy(temperatureMin = v) }
    fun onTempMaxChange(v: String) = _uiState.update { it.copy(temperatureMax = v) }
    fun onHumidityControlledChange(v: Boolean) = _uiState.update { it.copy(humidityControlled = v) }
    fun onHumidityMinChange(v: String) = _uiState.update { it.copy(humidityMin = v) }
    fun onHumidityMaxChange(v: String) = _uiState.update { it.copy(humidityMax = v) }
    fun onSecurityLevelChange(v: String) = _uiState.update { it.copy(securityLevel = v) }
    fun onHazardousChange(v: Boolean) = _uiState.update { it.copy(hazardousMaterials = v) }
    fun onFragileChange(v: Boolean) = _uiState.update { it.copy(fragileItems = v) }
    fun onPickingPriorityChange(v: String) = _uiState.update { it.copy(pickingPriority = v) }
    fun onManagerChange(v: String) = _uiState.update { it.copy(manager = v) }
    fun onContactInfoChange(v: String) = _uiState.update { it.copy(contactInfo = v) }
    fun onColorChange(v: String) = _uiState.update { it.copy(color = v) }

    private fun loadZone() {
        viewModelScope.launch {
            try {
                val zone = zoneRepository.getZoneById(zoneId)
                _uiState.update {
                    it.copy(
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
                        color = zone.color ?: "",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Nie udało się załadować strefy: ${e.message}") }
            }
        }
    }

    fun updateZone() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val request = UpdateZoneRequest(
                    name = _uiState.value.name,
                    code = _uiState.value.code, // Backend decyduje czy pozwala zmienić kod
                    description = _uiState.value.description.ifBlank { null },
                    type = _uiState.value.type,
                    active = _uiState.value.active,
                    temperatureControlled = _uiState.value.temperatureControlled,
                    temperatureMin = _uiState.value.temperatureMin.toIntOrNull(),
                    temperatureMax = _uiState.value.temperatureMax.toIntOrNull(),
                    humidityControlled = _uiState.value.humidityControlled,
                    humidityMin = _uiState.value.humidityMin.toIntOrNull(),
                    humidityMax = _uiState.value.humidityMax.toIntOrNull(),
                    securityLevel = _uiState.value.securityLevel.toIntOrNull(),
                    hazardousMaterials = _uiState.value.hazardousMaterials,
                    fragileItems = _uiState.value.fragileItems,
                    pickingPriority = _uiState.value.pickingPriority.toIntOrNull(),
                    manager = _uiState.value.manager.ifBlank { null },
                    contactInfo = _uiState.value.contactInfo.ifBlank { null },
                    color = _uiState.value.color.ifBlank { null }
                )
                zoneRepository.updateZone(zoneId, request)
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Błąd aktualizacji") }
            }
        }
    }
}