package com.qrware.shared.data.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class InventoryItemDTO(
    val id: Long,
    val product: ProductDTO,
    val location: LocationDTO,
    val quantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int,
    val status: InventoryStatus,
    val qrCode: String?,
    val lotNumber: String?,
    val batchNumber: String?,
    val serialNumber: String?,
    val receivedDate: String?,
    val expiryDate: String?,
    val manufactureDate: String?,
    val lastCountedDate: String?,
    val lastMovedDate: String?,
    @Serializable(with = BigDecimalSerializer::class)
    val unitCost: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val totalCost: BigDecimal?,
    val supplierReference: String?,
    val purchaseOrderNumber: String?,
    val notes: String?,
    val temperature: Int?,
    val humidity: Int?,
    val conditionRating: Int?,
    val quarantine: Boolean?,
    val quarantineReason: String?,
    val hold: Boolean?,
    val holdReason: String?
)

// Alias for backward compatibility
typealias InventoryItem = InventoryItemDTO

@Serializable
enum class InventoryStatus {
    AVAILABLE,
    RESERVED,
    UNAVAILABLE,
    ON_HOLD,
    QUARANTINE,
    DAMAGED,
    EXPIRED
}

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
    @Serializable(with = BigDecimalSerializer::class)
    val capacityVolume: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val capacityWeight: BigDecimal?,
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
    @Serializable(with = BigDecimalSerializer::class)
    val xCoordinate: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val yCoordinate: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val zCoordinate: BigDecimal?
)

// Alias for backward compatibility
typealias Location = LocationDTO

@Serializable
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

// Alias for backward compatibility  
typealias Zone = ZoneDTO

// LocationType is defined in CommonModels.kt

@Serializable
enum class ZoneType {
    STORAGE,
    RECEIVING, 
    SHIPPING,
    PRODUCTION,
    QUALITY_CONTROL,
    STAGING,
    PICKING,
    PACKING,
    RETURNS,
    QUARANTINE,
    COLD_STORAGE,
    FREEZER,
    HAZMAT,
    HIGH_VALUE,
    BULK,
    OFFICE
}

@Serializable
data class CreateInventoryRequest(
    val productId: Long,
    val locationId: Long,
    val quantity: Int,
    val reservedQuantity: Int = 0,
    val status: InventoryStatus = InventoryStatus.AVAILABLE,
    val qrCode: String,
    val lotNumber: String? = null,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val receivedDate: String? = null,
    val expiryDate: String? = null,
    val manufactureDate: String? = null,
    val unitCost: Double? = null,
    val supplierReference: String? = null,
    val purchaseOrderNumber: String? = null,
    val notes: String? = null,
    val temperature: Int? = null,
    val humidity: Int? = null,
    val conditionRating: Int = 10,
    val quarantine: Boolean = false,
    val quarantineReason: String? = null,
    val hold: Boolean = false,
    val holdReason: String? = null
)

@Serializable
data class UpdateInventoryRequest(
    val locationId: Long? = null,
    val quantity: Int? = null,
    val reservedQuantity: Int? = null,
    val status: InventoryStatus? = null,
    val lotNumber: String? = null,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val receivedDate: String? = null,
    val expiryDate: String? = null,
    val manufactureDate: String? = null,
    val unitCost: Double? = null,
    val supplierReference: String? = null,
    val purchaseOrderNumber: String? = null,
    val notes: String? = null,
    val temperature: Int? = null,
    val humidity: Int? = null,
    val conditionRating: Int? = null,
    val quarantine: Boolean? = null,
    val quarantineReason: String? = null,
    val hold: Boolean? = null,
    val holdReason: String? = null
)

@Serializable
data class QuantityUpdateRequest(
    val quantity: Int,
    val reason: String? = null
)

@Serializable
data class QRInventoryVerificationResult(
    val qrCodeExists: Boolean,
    val inventoryExists: Boolean,
    val inventoryItem: InventoryItem? = null,
    val qrCodeData: QRCodeData? = null,
    val message: String
)