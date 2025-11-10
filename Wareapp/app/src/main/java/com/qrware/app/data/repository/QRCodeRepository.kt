package com.qrware.app.data.repository

import com.qrware.app.data.model.*
import com.qrware.app.data.remote.ApiService
import retrofit2.Response // Potrzebne dla delete
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRCodeRepository @Inject constructor(
    private val apiService: ApiService
) {
    // ZMIANA: Dodano jawny typ zwracany
    suspend fun getAllQRCodes(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,desc"
    ): PaginatedResponse<QRCodeData> = apiService.getAllQRCodes(page, size, sort)

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun getQRCodeById(qrCodeId: Long): QRCodeData =
        apiService.getQRCodeById(qrCodeId)

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun scanQRCode(code: String): QRCodeData =
        apiService.scanQRCode(code)

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun getQRCodeByEntity(entityType: String, entityId: Long): QRCodeData =
        apiService.getQRCodeByEntity(entityType, entityId)

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun getActiveQRCodes(): List<QRCodeData> =
        apiService.getActiveQRCodes()

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun getQRCodesByType(type: QRCodeType): List<QRCodeData> =
        apiService.getQRCodesByType(type)

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun generateQRCode(request: GenerateQRRequest): QRCodeData =
        apiService.generateQRCode(request)

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun updateQRCode(qrCodeId: Long, request: UpdateQRRequest): QRCodeData =
        apiService.updateQRCode(qrCodeId, request)

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun deleteQRCode(qrCodeId: Long): Response<Unit> =
        apiService.deleteQRCode(qrCodeId)

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun toggleQRCodeActive(qrCodeId: Long): QRCodeData =
        apiService.toggleQRCodeActive(qrCodeId)

    // ZMIANA: Dodano jawny typ zwracany
    suspend fun getQRStats(): QRStatsResponse =
        apiService.getQRStats()
}