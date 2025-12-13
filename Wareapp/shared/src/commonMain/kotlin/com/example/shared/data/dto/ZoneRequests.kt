package com.example.shared.data.dto

import com.example.shared.data.model.ZoneType
import kotlinx.serialization.Serializable

@Serializable
data class CreateZoneRequest(
    val name: String,
    val code: String,
    val description: String?,
    val type: ZoneType,
    val active: Boolean = true,
    val temperatureControlled: Boolean = false,
    val temperatureMin: Int? = null,
    val temperatureMax: Int? = null,
    val humidityControlled: Boolean = false,
    val humidityMin: Int? = null,
    val humidityMax: Int? = null,
    val securityLevel: Int = 1,
    val hazardousMaterials: Boolean = false,
    val fragileItems: Boolean = false,
    val pickingPriority: Int = 5,
    val manager: String? = null,
    val contactInfo: String? = null,
    val color: String? = null
)

@Serializable
data class UpdateZoneRequest(
    val name: String?,
    val code: String?,
    val description: String?,
    val type: ZoneType?,
    val active: Boolean?,
    val temperatureControlled: Boolean?,
    val temperatureMin: Int?,
    val temperatureMax: Int?,
    val humidityControlled: Boolean?,
    val humidityMin: Int?,
    val humidityMax: Int?,
    val securityLevel: Int?,
    val hazardousMaterials: Boolean?,
    val fragileItems: Boolean?,
    val pickingPriority: Int?,
    val manager: String?,
    val contactInfo: String?,
    val color: String?
)