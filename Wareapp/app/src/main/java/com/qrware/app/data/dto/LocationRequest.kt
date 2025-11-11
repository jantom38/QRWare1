package com.qrware.app.data.dto

import com.qrware.app.data.model.LocationType
import java.math.BigDecimal

/**
 * Obiekt żądania do TWORZENIA nowej lokalizacji.
 * Odpowiada CreateLocationRequest z LocationController.
 */
data class CreateLocationRequest(
    val code: String, // @NotBlank
    val name: String, // @NotBlank
    val description: String?,
    val zoneId: Long, // @NotNull
    val type: LocationType, // @NotNull
    val aisle: String?,
    val rack: String?,
    val shelf: String?,
    val bin: String?,
    val capacityVolume: BigDecimal?,
    val capacityWeight: BigDecimal?,
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
    val xCoordinate: BigDecimal?,
    val yCoordinate: BigDecimal?, // Zauważ: gettery w kontrolerze miały Get/Set
    val zCoordinate: BigDecimal?  // Zauważ: gettery w kontrolerze miały Get/Set
)

/**
 * Obiekt żądania do AKTUALIZACJI lokalizacji.
 * Odpowiada UpdateLocationRequest z LocationController.
 * Wszystkie pola są opcjonalne (nullowalne).
 */
data class UpdateLocationRequest(
    val name: String?,
    val description: String?,
    val zoneId: Long?,
    val type: LocationType?,
    val aisle: String?,
    val rack: String?,
    val shelf: String?,
    val bin: String?,
    val capacityVolume: BigDecimal?,
    val capacityWeight: BigDecimal?,
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
    val xCoordinate: BigDecimal?,
    val yCoordinate: BigDecimal?,
    val zCoordinate: BigDecimal?
)