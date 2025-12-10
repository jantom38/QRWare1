package com.qrware.shared.data.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class InventoryItem(
    val id: Long,
    val product: Product,
    val location: Location,
    val quantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int,
    val status: InventoryStatus,
    val qrCode: String,
    val lotNumber: String? = null,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val receivedDate: String, // Using String for dates to simplify serialization
    val expiryDate: String? = null,
    val manufactureDate: String? = null,
    val lastCountedDate: String? = null,
    val lastMovedDate: String? = null,
    val unitCost: Double? = null,
    val totalCost: Double? = null,
    val supplierReference: String? = null,
    val purchaseOrderNumber: String? = null,
    val notes: String? = null,
    val temperature: Int? = null,
    val humidity: Int? = null,
    val conditionRating: Int? = null,
    val quarantine: Boolean,
    val quarantineReason: String? = null,
    val hold: Boolean,
    val holdReason: String? = null
)

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
data class Location(
    val id: Long,
    val code: String,
    val name: String,
    val description: String? = null,
    val zone: Zone
)

@Serializable
data class Zone(
    val id: Long,
    val code: String,
    val name: String,
    val description: String? = null,
    val type: ZoneType
)

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