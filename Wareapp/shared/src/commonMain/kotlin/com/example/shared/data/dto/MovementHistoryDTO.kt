package com.example.shared.data.dto

import androidx.compose.ui.graphics.Color
import com.example.shared.data.model.InventoryStatus
import com.example.shared.data.model.MovementType
import kotlinx.serialization.Serializable

@Serializable
data class MovementHistoryDTO(
    val id: Long,
    val inventoryItem: InventoryItemDTO,
    val movementType: MovementType?,
    val movementDate: String, // ISO format
    val quantityBefore: Int?,
    val quantityAfter: Int?,
    val quantityChanged: Int,
    val fromLocation: LocationDTO?,
    val toLocation: LocationDTO?,
    val statusBefore: InventoryStatus?,
    val statusAfter: InventoryStatus?,
    val unitCost: Double?,
    val totalCost: Double?,
    val referenceNumber: String?,
    val referenceType: String?,
    val reason: String?,
    val notes: String?,
    val userId: String?,
    val userName: String?,
    val approved: Boolean,
    val approvedBy: String?,
    val approvedDate: String?,
    val batchId: String?,
    val systemGenerated: Boolean,
    val temperature: Int?,
    val humidity: Int?,
    val weight: Double?,
    val volume: Double?,
    val createdAt: String?,
    val updatedAt: String?
) {
    // UWAGA: Formatowanie dat w KMP najlepiej robić przy użyciu kotlinx-datetime.
    // Tutaj zwracamy surowy string lub prostą wersję.
    fun getFormattedMovementDate(): String {
        return movementDate.replace("T", " ").take(16) // Prosty hack tymczasowy
    }

    fun getFormattedApprovedDate(): String? {
        return approvedDate?.replace("T", " ")?.take(16)
    }

    fun getMovementDescription(): String {
        val description = StringBuilder()
        val type = movementType
        description.append(type?.name ?: "Ruch magazynowy") // Zakładam name, jeśli brak displayName

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

        return description.toString()
    }

    fun isQuantityChange(): Boolean = quantityChanged != 0
    fun isLocationChange(): Boolean = fromLocation != null && toLocation != null && fromLocation.id != toLocation.id
    fun isStatusChange(): Boolean = statusBefore != null && statusAfter != null && statusBefore != statusAfter
    fun isInbound(): Boolean = movementType?.increasesQuantity == true
    fun isOutbound(): Boolean = movementType?.decreasesQuantity == true
    fun requiresApproval(): Boolean = movementType?.requiresApproval == true
    fun isApprovalPending(): Boolean = requiresApproval() && !approved

    fun getQuantityChangeText(): String {
        return when {
            quantityChanged > 0 -> "+$quantityChanged"
            quantityChanged < 0 -> "$quantityChanged"
            else -> "±0"
        }
    }

    // To zadziała w Compose Multiplatform
    fun getQuantityChangeColor(): Color {
        return when {
            quantityChanged > 0 -> Color(0xFF4CAF50) // Green
            quantityChanged < 0 -> Color(0xFFF44336) // Red
            else -> Color(0xFF9E9E9E) // Gray
        }
    }

    fun getMovementIcon(): String {
        val type = movementType ?: return "\u25A0"
        return when (type) {
            MovementType.RECEIPT -> "📥"
            // ... reszta ikon bez zmian (skopiuj ze starego pliku)
            else -> "📦"
        }
    }
}