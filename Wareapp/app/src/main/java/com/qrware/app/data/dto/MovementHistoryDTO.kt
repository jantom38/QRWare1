package com.qrware.app.data.dto

import com.qrware.app.data.model.InventoryStatus
import com.qrware.app.data.model.MovementType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MovementHistoryDTO(
    val id: Long,
    val inventoryItem: InventoryItemDTO,
    val movementType: MovementType,
    val movementDate: String, // ISO format: yyyy-MM-ddTHH:mm:ss
    val quantityBefore: Int?,
    val quantityAfter: Int?,
    val quantityChanged: Int,
    val fromLocation: LocationDTO?,
    val toLocation: LocationDTO?,
    val statusBefore: InventoryStatus?,
    val statusAfter: InventoryStatus?,
    val unitCost: BigDecimal?,
    val totalCost: BigDecimal?,
    val referenceNumber: String?,
    val referenceType: String?,
    val reason: String?,
    val notes: String?,
    val userId: String?,
    val userName: String?,
    val approved: Boolean,
    val approvedBy: String?,
    val approvedDate: String?, // ISO format
    val batchId: String?,
    val systemGenerated: Boolean,
    val temperature: Int?,
    val humidity: Int?,
    val weight: BigDecimal?,
    val volume: BigDecimal?,
    val createdAt: String?,
    val updatedAt: String?
) {
    fun getFormattedMovementDate(): String {
        return try {
            val localDateTime = LocalDateTime.parse(movementDate)
            localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } catch (e: Exception) {
            movementDate
        }
    }

    fun getFormattedApprovedDate(): String? {
        return approvedDate?.let {
            try {
                val localDateTime = LocalDateTime.parse(it)
                localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            } catch (e: Exception) {
                it
            }
        }
    }

    fun getMovementDescription(): String {
        val description = StringBuilder()
        // MovementType posiada displayName (z pliku MovementType.kt), więc to jest OK
        description.append(movementType.displayName)

        if (quantityChanged != 0) {
            description.append(" - Ilość: ")
            quantityBefore?.let { description.append(it) } ?: description.append("0")
            description.append(" → ")
            quantityAfter?.let { description.append(it) } ?: description.append("0")
            description.append(" (")
            if (quantityChanged > 0) description.append("+")
            description.append(quantityChanged).append(")")
        }

        if (fromLocation != null && toLocation != null && fromLocation.id != toLocation.id) {
            description.append(" - Lokalizacja: ")
            description.append(fromLocation.code).append(" → ").append(toLocation.code)
        }

        if (statusBefore != null && statusAfter != null && statusBefore != statusAfter) {
            description.append(" - Status: ")
            // Używamy extension property zdefiniowanego na dole pliku
            description.append(statusBefore.displayName).append(" → ").append(statusAfter.displayName)
        }

        return description.toString()
    }

    fun isQuantityChange(): Boolean = quantityChanged != 0

    fun isLocationChange(): Boolean =
        fromLocation != null && toLocation != null && fromLocation.id != toLocation.id

    fun isStatusChange(): Boolean =
        statusBefore != null && statusAfter != null && statusBefore != statusAfter

    fun isInbound(): Boolean = movementType.increasesQuantity

    fun isOutbound(): Boolean = movementType.decreasesQuantity

    fun requiresApproval(): Boolean = movementType.requiresApproval

    fun isApprovalPending(): Boolean = requiresApproval() && !approved

    fun getQuantityChangeText(): String {
        return when {
            quantityChanged > 0 -> "+$quantityChanged"
            quantityChanged < 0 -> "$quantityChanged"
            else -> "±0"
        }
    }

    // Zakładam, że masz zależność do Compose UI, jeśli nie - usuń ten import i metodę
    fun getQuantityChangeColor(): androidx.compose.ui.graphics.Color {
        return when {
            quantityChanged > 0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            quantityChanged < 0 -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            else -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Gray
        }
    }

    fun getMovementIcon(): String {
        return when (movementType) {
            MovementType.RECEIPT -> "📥"
            MovementType.ISSUE -> "📤"
            MovementType.TRANSFER, MovementType.MOVE -> "🔄"
            MovementType.ADJUSTMENT, MovementType.CYCLE_COUNT, MovementType.PHYSICAL_COUNT -> "⚖️"
            MovementType.RESERVE, MovementType.UNRESERVE -> "🔒"
            MovementType.PICK -> "📋"
            MovementType.PACK -> "📦"
            MovementType.SHIP -> "🚚"
            MovementType.RETURN -> "↩️"
            MovementType.PUTAWAY -> "📍"
            MovementType.REPLENISHMENT -> "🔄"
            MovementType.ALLOCATION, MovementType.DEALLOCATION -> "🎯"
            MovementType.QUARANTINE -> "⚠️"
            MovementType.RELEASE -> "✅"
            MovementType.HOLD, MovementType.UNHOLD -> "⏸️"
            MovementType.DAMAGE -> "💥"
            MovementType.DISPOSAL -> "🗑️"
            MovementType.LOSS -> "❌"
            MovementType.FOUND -> "🔍"
            MovementType.EXPIRY -> "⏰"
            MovementType.RECALL -> "🚨"
            MovementType.STAGING -> "📍"
            MovementType.CROSSDOCK -> "🔀"
            MovementType.CONSOLIDATION -> "📊"
            MovementType.SPLIT -> "✂️"
            MovementType.MERGE -> "🔗"
            MovementType.CONVERSION -> "🔄"
            MovementType.PRODUCTION -> "🏭"
            MovementType.CONSUMPTION -> "⚡"
            MovementType.SCRAP -> "♻️"
            MovementType.REWORK -> "🔧"
            MovementType.SAMPLE -> "🧪"
            MovementType.LOAN -> "📋"
            MovementType.LOAN_RETURN -> "↩️"
        }
    }
}

// Dodano rozszerzenie, aby obsłużyć brak pola displayName w InventoryModels.kt
private val InventoryStatus.displayName: String
    get() = when (this) {
        InventoryStatus.AVAILABLE -> "Dostępne"
        InventoryStatus.RESERVED -> "Zarezerwowane"
        InventoryStatus.UNAVAILABLE -> "Niedostępne"
        InventoryStatus.ON_HOLD -> "Wstrzymane"
        InventoryStatus.QUARANTINE -> "Kwarantanna"
        InventoryStatus.DAMAGED -> "Uszkodzone"
        InventoryStatus.EXPIRED -> "Przeterminowane"
    }