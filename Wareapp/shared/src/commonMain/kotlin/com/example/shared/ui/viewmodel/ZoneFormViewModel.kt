package com.example.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shared.data.model.ZoneType
import com.example.shared.data.repository.ZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

open class ZoneFormViewModel(
    protected val repository: ZoneRepository
) : ViewModel() {

    protected val _uiState = MutableStateFlow(ZoneFormUiState())
    val uiState: StateFlow<ZoneFormUiState> = _uiState.asStateFlow()

    // --- Handlery zmian pól formularza ---

    fun onCodeChange(v: String) = _uiState.update { it.copy(code = v) }
    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }

    fun onTypeSelected(v: ZoneType) {
        _uiState.update {
            it.copy(
                type = v,
                // Automatyczne ustawienie domyślnych wartości na podstawie typu
                securityLevel = v.defaultSecurityLevel.toString(),
                pickingPriority = v.defaultPickingPriority.toString(),
                temperatureControlled = v.isTemperatureControlled,
                hazardousMaterials = v == ZoneType.HAZMAT,
                fragileItems = v == ZoneType.HIGH_VALUE
            )
        }
    }

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
}