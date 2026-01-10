package com.qrware.app.data.dto

import com.qrware.app.data.model.InventoryStatus
import java.math.BigDecimal

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
    val unitCost: BigDecimal?,
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