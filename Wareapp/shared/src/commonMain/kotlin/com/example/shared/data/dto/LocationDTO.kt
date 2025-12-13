package com.example.shared.data.dto

import com.example.shared.data.model.LocationType
import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val zone: ZoneDTO,
    val type: LocationType?,
    val aisle: String?,
    val rack: String?,
    val shelf: String?,
    val bin: String?,
    val capacityVolume: Double?, // BigDecimal -> Double
    val capacityWeight: Double?,
    val capacityItems: Int?,
    val temperatureControlled: Boolean,
    val temperatureMin: Int?,
    val temperatureMax: Int?,
    val humidityControlled: Boolean,
    val humidityMin: Int?,
    val humidityMax: Int?,
    val hazardousMaterials: Boolean,
    val fragileItems: Boolean,
    val securityLevel: Int,
    val active: Boolean,
    val pickable: Boolean,
    val receivable: Boolean,
    val qrCode: String?,
    val barcode: String?,
    val xCoordinate: Double?,
    val yCoordinate: Double?,
    val zCoordinate: Double?
)