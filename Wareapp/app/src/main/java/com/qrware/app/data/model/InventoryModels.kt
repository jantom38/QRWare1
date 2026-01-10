package com.qrware.app.data.model

import java.math.BigDecimal
import java.time.LocalDate

data class InventoryItem(
    val id: Long,
    val product: Product,
    val location: Location,
    val quantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int,
    val status: InventoryStatus,
    val qrCode: String,
    val lotNumber: String?,
    val batchNumber: String?,
    val serialNumber: String?,
    val receivedDate: LocalDate,
    val expiryDate: LocalDate?,
    val manufactureDate: LocalDate?,
    val lastCountedDate: LocalDate?,
    val lastMovedDate: LocalDate?,
    val unitCost: BigDecimal?,
    val totalCost: BigDecimal?,
    val supplierReference: String?,
    val purchaseOrderNumber: String?,
    val notes: String?,
    val temperature: Int?,
    val humidity: Int?,
    val conditionRating: Int?,
    val quarantine: Boolean,
    val quarantineReason: String?,
    val hold: Boolean,
    val holdReason: String?
)

enum class InventoryStatus {
    AVAILABLE,
    RESERVED,
    UNAVAILABLE,
    ON_HOLD,
    QUARANTINE,
    DAMAGED,
    EXPIRED
}

data class Location(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val zone: Zone
)

data class Zone(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val type: ZoneType
)

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
    val unitCost: BigDecimal? = null,
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

data class UpdateInventoryRequest(
    val locationId: Long? = null,
    val quantity: Int? = null,
    val reservedQuantity: Int? = null,
    val status: InventoryStatus? = null,
    val lotNumber: String? = null,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val receivedDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    val manufactureDate: LocalDate? = null,
    val unitCost: BigDecimal? = null,
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

data class QuantityUpdateRequest(
    val quantity: Int,
    val reason: String? = null
)

data class QRInventoryVerificationResult(
    val qrCodeExists: Boolean,
    val inventoryExists: Boolean,
    val inventoryItem: InventoryItem?,
    val qrCodeData: QRCodeData?,
    val message: String
)

data class InventoryAlertDTO(
    val type: String,
    val severity: String,
    val sku: String,
    val productName: String,
    val message: String,
    val entityId: Long
)