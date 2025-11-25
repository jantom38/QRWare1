package com.qrware.app.data.repository

import com.qrware.app.data.api.ApprovalRequest
import com.qrware.app.data.api.MovementHistoryApiService
import com.qrware.app.data.dto.MovementHistoryDTO
import com.qrware.app.data.model.MovementType

class MovementHistoryRepository(
    private val apiService: MovementHistoryApiService
) {
    suspend fun getMovementHistoryByItemId(itemId: Long): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByItemId(itemId)
        return handleResponse(response)
    }

    suspend fun getMovementHistoryByProductId(productId: Long): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByProductId(productId)
        return handleResponse(response)
    }

    suspend fun getMovementHistoryByLocationId(locationId: Long): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByLocationId(locationId)
        return handleResponse(response)
    }

    suspend fun getMovementHistoryByType(movementType: MovementType): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByType(movementType.name)
        return handleResponse(response)
    }

    suspend fun getMovementHistoryByDateRange(startDate: String, endDate: String): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByDateRange(startDate, endDate)
        return handleResponse(response)
    }

    suspend fun getMovementHistoryByUserId(userId: String): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByUserId(userId)
        return handleResponse(response)
    }

    suspend fun getRecentMovements(limit: Int = 50): List<MovementHistoryDTO> {
        val response = apiService.getRecentMovements(limit)
        return handleResponse(response)
    }

    suspend fun getPendingApprovalMovements(): List<MovementHistoryDTO> {
        val response = apiService.getPendingApprovalMovements()
        return handleResponse(response)
    }

    suspend fun getInboundMovements(limit: Int? = null): List<MovementHistoryDTO> {
        val response = apiService.getInboundMovements(limit)
        return handleResponse(response)
    }

    suspend fun getOutboundMovements(limit: Int? = null): List<MovementHistoryDTO> {
        val response = apiService.getOutboundMovements(limit)
        return handleResponse(response)
    }

    suspend fun getAdjustmentMovements(limit: Int? = null): List<MovementHistoryDTO> {
        val response = apiService.getAdjustmentMovements(limit)
        return handleResponse(response)
    }

    suspend fun getMovementStatsByType(): Map<String, Any> {
        val response = apiService.getMovementStatsByType()
        return response.body() ?: emptyMap()
    }

    suspend fun getMovementStatsByDate(startDate: String, endDate: String): Map<String, Any> {
        val response = apiService.getMovementStatsByDate(startDate, endDate)
        return response.body() ?: emptyMap()
    }

    suspend fun searchMovements(keyword: String, searchIn: String = "reason"): List<MovementHistoryDTO> {
        val response = apiService.searchMovements(keyword, searchIn)
        return handleResponse(response)
    }

    suspend fun approveMovement(movementId: Long, approverComment: String?): MovementHistoryDTO {
        val request = ApprovalRequest(approverComment)
        val response = apiService.approveMovement(movementId, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Pusta odpowiedź")
        } else {
            throw Exception("Błąd: ${response.errorBody()?.string()}")
        }
    }

    suspend fun getMovementById(movementId: Long): MovementHistoryDTO {
        val response = apiService.getMovementById(movementId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Nie znaleziono ruchu")
        } else {
            throw Exception("Błąd: ${response.errorBody()?.string()}")
        }
    }

    suspend fun getMovementsRequiringAttention(): List<MovementHistoryDTO> {
        val response = apiService.getMovementsRequiringAttention()
        return handleResponse(response)
    }

    suspend fun getAuditTrail(startDate: String, endDate: String): List<MovementHistoryDTO> {
        val response = apiService.getAuditTrail(startDate, endDate)
        return handleResponse(response)
    }

    suspend fun getMovementVelocity(startDate: String, endDate: String): Double {
        val response = apiService.getMovementVelocity(startDate, endDate)
        return response.body() ?: 0.0
    }

    suspend fun getMovementsWithEnvironmentalData(): List<MovementHistoryDTO> {
        val response = apiService.getMovementsWithEnvironmentalData()
        return handleResponse(response)
    }

    suspend fun getMovementsByBatchId(batchId: String): List<MovementHistoryDTO> {
        val response = apiService.getMovementsByBatchId(batchId)
        return handleResponse(response)
    }

    suspend fun getMovementsByReferenceNumber(referenceNumber: String): List<MovementHistoryDTO> {
        val response = apiService.getMovementsByReferenceNumber(referenceNumber)
        return handleResponse(response)
    }

    // Helper
    private fun handleResponse(response: retrofit2.Response<List<MovementHistoryDTO>>): List<MovementHistoryDTO> {
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd API: ${response.errorBody()?.string()}")
        }
    }
}