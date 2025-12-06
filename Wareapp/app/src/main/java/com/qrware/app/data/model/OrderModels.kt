package com.qrware.app.data.model

import java.math.BigDecimal
import java.time.LocalDateTime

// Order Status Enum
enum class OrderStatus {
    CREATED, ASSIGNED, IN_PROGRESS, ON_HOLD, PARTIALLY_COMPLETED, COMPLETED, CANCELLED, FAILED
}

// Order Type Enum  
enum class OrderType {
    INBOUND, OUTBOUND, TRANSFER, PICK, PUTAWAY, CYCLE_COUNT, REPLENISHMENT, RETURN, ADJUSTMENT, MAINTENANCE, QUALITY_CHECK
}

// Order Priority Enum
enum class OrderPriority {
    LOW, NORMAL, HIGH, URGENT, CRITICAL
}

// Order Item Status Enum
enum class OrderItemStatus {
    PENDING, IN_PROGRESS, PICKED, PARTIALLY_COMPLETED, COMPLETED, CANCELLED, ON_HOLD, BACK_ORDERED, DAMAGED, SHORT_PICKED
}

// Main Order Data Class
data class OrderDTO(
    val id: Long,
    val orderNumber: String,
    val type: OrderType,
    val status: OrderStatus,
    val priority: OrderPriority,
    val description: String? = null,
    
    // User information
    val createdById: Long,
    val createdByUsername: String,
    val createdByFullName: String? = null,
    val assignedToId: Long? = null,
    val assignedToUsername: String? = null,
    val assignedToFullName: String? = null,
    
    // Location information
    val sourceLocationId: Long? = null,
    val sourceLocationName: String? = null,
    val sourceLocationCode: String? = null,
    val destinationLocationId: Long? = null,
    val destinationLocationName: String? = null,
    val destinationLocationCode: String? = null,
    
    // Dates
    val expectedDate: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val cancelledAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    
    // Additional information
    val cancellationReason: String? = null,
    val totalItems: Int? = 0,
    val completedItems: Int? = 0,
    val estimatedValue: BigDecimal? = BigDecimal.ZERO,
    val notes: String? = null,
    val externalReference: String? = null,
    
    // Order items
    val orderItems: List<OrderItemDTO>? = emptyList(),
    
    // Calculated fields
    val completionPercentage: Double? = 0.0,
    val isOverdue: Boolean? = false,
    val isHighPriority: Boolean? = false,
    val canBeStarted: Boolean? = false,
    val canBeCompleted: Boolean? = false,
    val canBeCancelled: Boolean? = false,
    val isActive: Boolean? = false
)

// Order Item Data Class
data class OrderItemDTO(
    val id: Long,
    val lineNumber: Int,
    
    // Order information
    val orderId: Long,
    val orderNumber: String,
    
    // Product information
    val productId: Long,
    val productName: String,
    val productSku: String,
    val productDescription: String? = null,
    
    // Inventory information
    val inventoryItemId: Long? = null,
    val inventoryItemCode: String? = null,
    
    // Location information
    val sourceLocationId: Long? = null,
    val sourceLocationName: String? = null,
    val sourceLocationCode: String? = null,
    val destinationLocationId: Long? = null,
    val destinationLocationName: String? = null,
    val destinationLocationCode: String? = null,
    
    // Quantities
    val requestedQuantity: Int,
    val completedQuantity: Int = 0,
    val remainingQuantity: Int? = null,
    
    // Pricing
    val unitPrice: BigDecimal? = BigDecimal.ZERO,
    val totalValue: BigDecimal? = BigDecimal.ZERO,
    
    // Status and tracking
    val status: OrderItemStatus,
    val notes: String? = null,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val qrCodeData: String? = null,
    
    // Dates
    val expiryDate: String? = null,
    val pickedAt: String? = null,
    val completedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    
    // Additional information
    val completionNotes: String? = null,
    
    // Calculated fields
    val completionPercentage: Double? = 0.0,
    val isCompleted: Boolean? = false,
    val isPartiallyCompleted: Boolean? = false,
    val canBeCompleted: Boolean? = false,
    val requiresQRScan: Boolean? = false,
    val isQRScanned: Boolean? = false,
    
    // Fulfillment fields
    val requiresExactInventory: Boolean? = true,
    val actualSourceQrCode: String? = null,
    val fulfillmentNotes: String? = null
)

// Request Models
data class CreateOrderRequest(
    val orderNumber: String? = null,
    val type: OrderType,
    val description: String? = null,
    val assignedToId: Long? = null,
    val sourceLocationId: Long? = null,
    val destinationLocationId: Long? = null,
    val expectedDate: String? = null,
    val priority: OrderPriority = OrderPriority.NORMAL
)

data class CreateOrderItemRequest(
    val productId: Long,
    val requestedQuantity: Int,
    val sourceLocationId: Long? = null,
    val destinationLocationId: Long? = null,
    val unitPrice: BigDecimal? = null,
    val notes: String? = null,
    val requiresExactInventory: Boolean? = true
)

data class CompleteOrderItemRequest(
    val completedQuantity: Int,
    val completionNotes: String? = null,
    val qrCodeData: String? = null
)

data class ScanQRRequest(
    val qrCodeData: String,
    val orderId: Long? = null
)

data class CancelOrderRequest(
    val reason: String
)

data class AssignOrderRequest(
    val assignedToId: Long
)

data class SetBatchRequest(
    val batchNumber: String
)

data class SetSerialRequest(
    val serialNumber: String
)

// Statistics Models
data class StatusCountDTO(
    val status: OrderStatus,
    val count: Long
)

data class ItemStatusCountDTO(
    val status: OrderItemStatus,
    val count: Long
)