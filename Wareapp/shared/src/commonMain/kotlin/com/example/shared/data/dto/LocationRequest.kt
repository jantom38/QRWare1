package com.example.shared.data.dto

import com.example.shared.data.model.LocationType
import kotlinx.serialization.Serializable

@Serializable
data class CreateLocationRequest(
    val code: String,
    val name: String,
    val description: String?,
    val zoneId: Long,
    val type: LocationType,
    val aisle: String?,
    val rack: String?,
    val shelf: String?,
    val bin: String?,
    val capacityVolume: Double?,
    val capacityWeight: Double?,
    val capacityItems: Int?,
    val temperatureControlled: Boolean?,
    val temperatureMin: Int?,
    val temperatureMax: Int?,
    val humidityControlled: Boolean?,
    val humidityMin: Int?,
    val humidityMax: Int?,
    val hazardousMaterials: Boolean?,
    val fragileItems: Boolean?,
    val securityLevel: Int?,
    val active: Boolean?,
    val pickable: Boolean?,
    val receivable: Boolean?,
    val qrCode: String?,
    val barcode: String?,
    val xCoordinate: Double?,
    val yCoordinate: Double?,
    val zCoordinate: Double?
)

@Serializable
data class UpdateLocationRequest(
    val name: String?,
    val description: String?,
    val zoneId: Long?,
    val type: LocationType?,
    val aisle: String?,
    val rack: String?,
    val shelf: String?,
    val bin: String?,
    val capacityVolume: Double?,
    val capacityWeight: Double?,
    val capacityItems: Int?,
    val temperatureControlled: Boolean?,
    val temperatureMin: Int?,
    val temperatureMax: Int?,
    val humidityControlled: Boolean?,
    val humidityMin: Int?,
    val humidityMax: Int?,
    val hazardousMaterials: Boolean?,
    val fragileItems: Boolean?,
    val securityLevel: Int?,
    val active: Boolean?,
    val pickable: Boolean?,
    val receivable: Boolean?,
    val qrCode: String?,
    val barcode: String?,
    val xCoordinate: Double?,
    val yCoordinate: Double?,
    val zCoordinate: Double?
)