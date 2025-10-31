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
    val unitCost: BigDecimal?,
    val totalCost: BigDecimal?,
    val notes: String?
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

enum class ZoneType {
    RECEIVING,
    STORAGE,
    PICKING,
    SHIPPING,
    QUARANTINE,
    DAMAGE
}

data class CreateInventoryRequest(
    val productId: Long,
    val locationId: Long,
    val quantity: BigDecimal,
    val status: InventoryStatus = InventoryStatus.AVAILABLE,
    val qrCode: String,
    val serialNumber: String? = null,
    val batchNumber: String? = null,
    val lotNumber: String? = null,
    val expirationDate: LocalDate? = null
)

data class UpdateInventoryRequest(
    val locationId: Long? = null,
    val quantity: BigDecimal? = null,
    val status: InventoryStatus? = null,
    val serialNumber: String? = null,
    val batchNumber: String? = null,
    val lotNumber: String? = null,
    val expirationDate: LocalDate? = null
)

data class QuantityUpdateRequest(
    val quantity: BigDecimal,
    val reason: String? = null
)