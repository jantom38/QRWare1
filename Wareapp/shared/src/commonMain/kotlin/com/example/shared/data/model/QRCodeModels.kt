package com.example.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QRCodeData(
    val id: Long,
    val code: String,
    val type: QRCodeType,
    val entityType: String?,
    val entityId: Long?,
    val data: String,
    val metadata: String?,
    val active: Boolean,
    val expiresAt: String?,
    val lastScanned: String?,
    val scanCount: Long,
    val format: String,
    val size: Int,
    val errorCorrectionLevel: ErrorCorrectionLevel,
    val generatedBy: String?,
    val generationReason: String?,
    val imagePath: String?
)

@Serializable
enum class QRCodeType {
    PRODUCT,
    LOCATION,
    INVENTORY_ITEM,
    SHIPMENT,
    CUSTOM
}

@Serializable
enum class ErrorCorrectionLevel {
    L, M, Q, H
}

@Serializable
data class GenerateQRRequest(
    val code: String,
    val type: QRCodeType,
    val entityType: String? = null,
    val entityId: Long? = null,
    val data: String,
    val metadata: String? = null,
    val expiresAt: String? = null,
    val format: String? = "PNG",
    val size: Int? = 300,
    val errorCorrectionLevel: ErrorCorrectionLevel? = ErrorCorrectionLevel.M,
    val generatedBy: String? = null,
    val generationReason: String? = null
)

@Serializable
data class GenerateQRImageRequest(
    val data: String,
    val type: QRCodeType,
    val entityType: String? = null,
    val entityId: Long? = null,
    val generatedBy: String? = null,
    val generationReason: String? = null
)

@Serializable
data class UpdateQRRequest(
    val data: String? = null,
    val metadata: String? = null,
    val active: Boolean? = null,
    val expiresAt: String? = null
)

@Serializable
data class QRStatsResponse(
    val totalCodes: Long,
    val activeCodes: Long,
    val inactiveCodes: Long,
    val totalScans: Long
)

@Serializable
data class QRScanResult(
    val code: String,
    val data: String,
    val type: QRCodeType,
    val entityType: String?,
    val entityId: Long?,
    val success: Boolean,
    val message: String? = null
)