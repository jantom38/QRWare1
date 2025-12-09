package com.qrware.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class InventoryStatus {
    AVAILABLE, RESERVED, OUT_OF_STOCK, DAMAGED, EXPIRED
}

@Serializable
enum class MovementType {
    IN, OUT, TRANSFER, ADJUSTMENT, RETURN, DAMAGED, EXPIRED
}

@Serializable
data class InventoryItem(
    val id: Long? = null,
    val product: Product,
    val location: Location? = null,
    val quantity: Int,
    val reservedQuantity: Int = 0,
    val status: InventoryStatus = InventoryStatus.AVAILABLE,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val expirationDate: String? = null,
    val lastMovementDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class MovementHistory(
    val id: Long? = null,
    val inventoryItem: InventoryItem,
    val movementType: MovementType,
    val quantity: Int,
    val fromLocation: Location? = null,
    val toLocation: Location? = null,
    val reason: String? = null,
    val performedBy: String? = null,
    val movementDate: String? = null,
    val notes: String? = null
)

@Serializable
data class CreateInventoryRequest(
    val productId: Long,
    val locationId: Long? = null,
    val quantity: Int,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val expirationDate: String? = null
)

@Serializable
data class UpdateInventoryRequest(
    val quantity: Int? = null,
    val status: InventoryStatus? = null,
    val locationId: Long? = null,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val expirationDate: String? = null
)

@Serializable
data class QuantityUpdateRequest(
    val quantity: Int,
    val reason: String? = null
)

@Serializable
data class LocationTransferRequest(
    val fromLocationId: Long? = null,
    val toLocationId: Long,
    val quantity: Int,
    val reason: String? = null
)