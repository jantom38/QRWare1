package com.qrware.shared.data.model

import kotlinx.serialization.Serializable

// Location Type Enum
@Serializable
enum class LocationType {
    STORAGE,
    RECEIVING,
    SHIPPING,
    STAGING,
    PICKING,
    PACKING,
    RETURNS,
    QUARANTINE,
    PRODUCTION,
    OFFICE
}

// Health Models
@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val database: String? = null,
    val version: String? = null,
    val uptime: String? = null
)

// Admin User Response
@Serializable
data class AdminUserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val fullName: String? = null,
    val active: Boolean,
    val emailVerified: Boolean,
    val createdAt: String? = null,
    val lastLogin: String? = null,
    val roles: List<String>,
    val permissions: List<String>
)

// Movement History Models
@Serializable
data class MovementHistoryDTO(
    val id: Long,
    val inventoryItemId: Long,
    val movementType: MovementType,
    val fromLocationId: Long? = null,
    val fromLocationCode: String? = null,
    val fromLocationName: String? = null,
    val toLocationId: Long? = null,
    val toLocationCode: String? = null,
    val toLocationName: String? = null,
    val quantityBefore: Int,
    val quantityChanged: Int,
    val quantityAfter: Int,
    val unitCost: Double? = null,
    val totalCost: Double? = null,
    val reason: String? = null,
    val notes: String? = null,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val referenceType: String? = null,
    val referenceId: Long? = null,
    val referenceNumber: String? = null,
    val performedById: Long,
    val performedByUsername: String,
    val performedByFullName: String? = null,
    val timestamp: String,
    val approved: Boolean? = null,
    val approvedById: Long? = null,
    val approvedByUsername: String? = null,
    val approvedAt: String? = null
)

// System Status Models
@Serializable
data class HealthStatus(
    val status: String,
    val message: String,
    val application: String,
    val version: String
)

@Serializable
data class SystemStatus(
    val application: String,
    val version: String,
    val uptime: String,
    val system: Map<String, String>,
    val memory: Map<String, String>,
    val database: Map<String, String>, // Changed from Map<String, Any> for serialization
    val status: String
)

// Pagination Models
@Serializable
data class PaginatedResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val number: Int,
    val size: Int,
    val first: Boolean,
    val last: Boolean
)

// User Management Models
@Serializable
data class AdminCreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val roles: Set<String>,
    val active: Boolean,
    val emailVerified: Boolean
)

@Serializable
data class UpdateUserRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val active: Boolean,
    val emailVerified: Boolean,
    val roles: Set<String>
)