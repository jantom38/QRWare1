package com.qrware.app.data.dto

import com.qrware.app.data.model.ZoneType
import java.time.LocalDateTime

data class ZoneDTO(
    val id: Long?,
    val name: String,
    val code: String,
    val description: String?,
    val type: ZoneType,
    val active: Boolean,
    val temperatureControlled: Boolean,
    val temperatureMin: Int?,
    val temperatureMax: Int?,
    val humidityControlled: Boolean,
    val humidityMin: Int?,
    val humidityMax: Int?,
    val securityLevel: Int,
    val hazardousMaterials: Boolean,
    val fragileItems: Boolean,
    val pickingPriority: Int,
    val manager: String?,
    val contactInfo: String?,
    val color: String?,

    val createdAt: String?,
    val updatedAt: String?,
    val createdBy: String?,

    val locationCount: Int,
    val activeLocationCount: Long,
    val occupiedLocationCount: Long,
    val occupancyRate: Double
)