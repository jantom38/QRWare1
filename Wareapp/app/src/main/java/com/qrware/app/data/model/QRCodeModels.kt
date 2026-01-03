package com.qrware.app.data.model

import java.time.LocalDateTime

data class QRCodeData(
    val id: Long,
    val code: String,
    val type: QRCodeType,
    val entityType: String?,
    val entityId: Long?,
    val data: String,
    val metadata: String?,
    val active: Boolean,
    val expiresAt: LocalDateTime?,
    val lastScanned: LocalDateTime?,
    val scanCount: Long,
    val format: String,
    val size: Int,
    val errorCorrectionLevel: ErrorCorrectionLevel,
    val generatedBy: String?,
    val generationReason: String?,
    val imagePath: String?
)

enum class QRCodeType {
    PRODUCT,
    INVENTORY_ITEM
}

enum class ErrorCorrectionLevel {
    L, M, Q, H
}

data class GenerateQRRequest(
    val code: String,
    val type: QRCodeType,
    val entityType: String? = null,
    val entityId: Long? = null,
    val data: String,
    val metadata: String? = null,
    val expiresAt: LocalDateTime? = null,
    val format: String? = "PNG",
    val size: Int? = 300,
    val errorCorrectionLevel: ErrorCorrectionLevel? = ErrorCorrectionLevel.M,
    val generatedBy: String? = null,
    val generationReason: String? = null
)

data class GenerateQRImageRequest(
    val data: String,
    val type: QRCodeType,
    val entityType: String? = null,
    val entityId: Long? = null,
    val generatedBy: String? = null,
    val generationReason: String? = null
)

data class UpdateQRRequest(
    val data: String? = null,
    val metadata: String? = null,
    val active: Boolean? = null,
    val expiresAt: LocalDateTime? = null
)

data class QRStatsResponse(
    val totalCodes: Long,
    val activeCodes: Long,
    val inactiveCodes: Long,
    val totalScans: Long
)

data class QRScanResult(
    val code: String,
    val data: String,
    val type: QRCodeType,
    val entityType: String?,
    val entityId: Long?,
    val success: Boolean,
    val message: String? = null
)