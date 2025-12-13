package com.example.shared.data.repository

import com.example.shared.data.dto.*
import com.example.shared.data.model.*
import com.example.shared.data.remote.ApiService

class QRCodeRepository(
    private val apiService: ApiService
) {
    suspend fun getAllQRCodes(page: Int = 0, size: Int = 20, sort: String = "id,desc"): PaginatedResponse<QRCodeData> =
        apiService.getAllQRCodes(page, size, sort)

    suspend fun getQRCodeById(qrCodeId: Long): QRCodeData =
        apiService.getQRCodeById(qrCodeId)

    suspend fun scanQRCode(code: String): QRCodeData =
        apiService.scanQRCode(code)

    suspend fun getQRCodeByEntity(entityType: String, entityId: Long): QRCodeData =
        apiService.getQRCodeByEntity(entityType, entityId)

    suspend fun getActiveQRCodes(): List<QRCodeData> =
        apiService.getActiveQRCodes()

    suspend fun getQRCodesByType(type: QRCodeType): List<QRCodeData> =
        apiService.getQRCodesByType(type)

    suspend fun generateQRCode(request: GenerateQRRequest): QRCodeData =
        apiService.generateQRCode(request)

    suspend fun updateQRCode(qrCodeId: Long, request: UpdateQRRequest): QRCodeData =
        apiService.updateQRCode(qrCodeId, request)

    suspend fun deleteQRCode(qrCodeId: Long) =
        apiService.deleteQRCode(qrCodeId)

    suspend fun toggleQRCodeActive(qrCodeId: Long): QRCodeData =
        apiService.toggleQRCodeActive(qrCodeId)

    suspend fun getQRStats(): QRStatsResponse =
        apiService.getQRStats()

    // Zwracamy DTO, bo konwersja na modele domenowe z java.time w KMP jest problematyczna
    // bez dodatkowych bibliotek. Logikę UI najlepiej oprzeć na DTO w tym etapie.
    suspend fun getInventoryByQRCode(qrCode: String): Result<InventoryItemDTO> {
        return try {
            val dto = apiService.getInventoryByQRCode(qrCode)
            Result.success(dto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}