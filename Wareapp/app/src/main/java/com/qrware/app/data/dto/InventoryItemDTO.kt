package com.qrware.app.data.dto

import com.qrware.app.data.model.InventoryStatus
import java.math.BigDecimal
// Usunięto import java.time.LocalDate, ponieważ nie jest już potrzebny

/**
 * Ta klasa DOKŁADNIE pasuje do JSON-a, który wysyła serwer.
 */
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

    // ZMIANA: Odbieramy daty jako zwykły tekst, aby pasowały do JSON-a
    val receivedDate: String?,
    val expiryDate: String?,

    val unitCost: BigDecimal?,
    val totalCost: BigDecimal?,
    val notes: String?
)