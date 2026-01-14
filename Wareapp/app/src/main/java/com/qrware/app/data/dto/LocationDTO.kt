package com.qrware.app.data.dto

import com.qrware.app.data.model.LocationType
import java.math.BigDecimal

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
    val capacityVolume: BigDecimal?,
    val capacityWeight: BigDecimal?,
    val capacityItems: Int?,
    val currentItems: Int?,
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
    val xCoordinate: BigDecimal?,
    val yCoordinate: BigDecimal?,
    val zCoordinate: BigDecimal?
)