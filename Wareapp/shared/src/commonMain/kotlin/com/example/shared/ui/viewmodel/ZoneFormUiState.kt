package com.example.shared.ui.viewmodel

import com.example.shared.data.model.ZoneType

data class ZoneFormUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,

    // Pola formularza
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val type: ZoneType = ZoneType.STORAGE,
    val active: Boolean = true,

    val temperatureControlled: Boolean = false,
    val temperatureMin: String = "",
    val temperatureMax: String = "",

    val humidityControlled: Boolean = false,
    val humidityMin: String = "",
    val humidityMax: String = "",

    val securityLevel: String = "1",
    val hazardousMaterials: Boolean = false,
    val fragileItems: Boolean = false,
    val pickingPriority: String = "5",

    val manager: String = "",
    val contactInfo: String = "",
    val color: String = ""
)