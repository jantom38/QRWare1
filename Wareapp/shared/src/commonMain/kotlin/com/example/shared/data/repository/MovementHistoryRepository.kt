package com.example.shared.data.repository

import com.example.shared.data.api.ApprovalRequest
import com.example.shared.data.api.MovementHistoryApiService

import com.example.shared.data.dto.MovementHistoryDTO
import com.example.shared.data.model.MovementType

class MovementHistoryRepository(
    private val apiService: MovementHistoryApiService
) {
    suspend fun getMovementHistoryByItemId(itemId: Long): List<MovementHistoryDTO> {
        return apiService.getMovementHistoryByItemId(itemId)
    }

    suspend fun getMovementHistoryByProductId(productId: Long): List<MovementHistoryDTO> {
        return apiService.getMovementHistoryByProductId(productId)
    }

    suspend fun getMovementHistoryByLocationId(locationId: Long): List<MovementHistoryDTO> {
        return apiService.getMovementHistoryByLocationId(locationId)
    }

    suspend fun getMovementHistoryByType(movementType: MovementType): List<MovementHistoryDTO> {
        // Używamy name enuma, API oczekuje stringa
        return apiService.getMovementHistoryByType(movementType.name)
    }

    suspend fun getMovementHistoryByDateRange(startDate: String, endDate: String): List<MovementHistoryDTO> {
        return apiService.getMovementHistoryByDateRange(startDate, endDate)
    }

    suspend fun getMovementHistoryByUserId(userId: String): List<MovementHistoryDTO> {
        return apiService.getMovementHistoryByUserId(userId)
    }

    suspend fun getRecentMovements(limit: Int = 50): List<MovementHistoryDTO> {
        return apiService.getRecentMovements(limit)
    }

    suspend fun getPendingApprovalMovements(): List<MovementHistoryDTO> {
        return apiService.getPendingApprovalMovements()
    }

    suspend fun getInboundMovements(limit: Int? = null): List<MovementHistoryDTO> {
        return apiService.getInboundMovements(limit)
    }

    suspend fun getOutboundMovements(limit: Int? = null): List<MovementHistoryDTO> {
        return apiService.getOutboundMovements(limit)
    }

    suspend fun getAdjustmentMovements(limit: Int? = null): List<MovementHistoryDTO> {
        return apiService.getAdjustmentMovements(limit)
    }

    suspend fun getMovementStatsByType(): Map<String, Any> {
        return apiService.getMovementStatsByType()
    }

    suspend fun getMovementStatsByDate(startDate: String, endDate: String): Map<String, Any> {
        return apiService.getMovementStatsByDate(startDate, endDate)
    }

    suspend fun searchMovements(keyword: String, searchIn: String = "reason"): List<MovementHistoryDTO> {
        return apiService.searchMovements(keyword, searchIn)
    }

    suspend fun approveMovement(movementId: Long, approverComment: String?): MovementHistoryDTO {
        val request = ApprovalRequest(approverComment)
        return apiService.approveMovement(movementId, request)
    }

    suspend fun getMovementById(movementId: Long): MovementHistoryDTO {
        return apiService.getMovementById(movementId)
    }

    suspend fun getMovementsRequiringAttention(): List<MovementHistoryDTO> {
        return apiService.getMovementsRequiringAttention()
    }

    suspend fun getAuditTrail(startDate: String, endDate: String): List<MovementHistoryDTO> {
        return apiService.getAuditTrail(startDate, endDate)
    }

    suspend fun getMovementVelocity(startDate: String, endDate: String): Double {
        return apiService.getMovementVelocity(startDate, endDate)
    }

    suspend fun getMovementsWithEnvironmentalData(): List<MovementHistoryDTO> {
        return apiService.getMovementsWithEnvironmentalData()
    }

    suspend fun getMovementsByBatchId(batchId: String): List<MovementHistoryDTO> {
        return apiService.getMovementsByBatchId(batchId)
    }

    suspend fun getMovementsByReferenceNumber(referenceNumber: String): List<MovementHistoryDTO> {
        return apiService.getMovementsByReferenceNumber(referenceNumber)
    }
}