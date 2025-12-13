package com.example.shared.data.api

import com.example.shared.data.dto.MovementHistoryDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*

class MovementHistoryApiService(private val client: HttpClient) {

    suspend fun getMovementHistoryByItemId(itemId: Long): List<MovementHistoryDTO> {
        return client.get("api/movement-history/inventory-item/$itemId").body()
    }

    suspend fun getMovementHistoryByProductId(productId: Long): List<MovementHistoryDTO> {
        return client.get("api/movement-history/product/$productId").body()
    }

    suspend fun getMovementHistoryByLocationId(locationId: Long): List<MovementHistoryDTO> {
        return client.get("api/movement-history/location/$locationId").body()
    }

    suspend fun getMovementHistoryByType(movementType: String): List<MovementHistoryDTO> {
        return client.get("api/movement-history/type/$movementType").body()
    }

    suspend fun getMovementHistoryByDateRange(startDate: String, endDate: String): List<MovementHistoryDTO> {
        return client.get("api/movement-history/date-range") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()
    }

    suspend fun getMovementHistoryByUserId(userId: String): List<MovementHistoryDTO> {
        return client.get("api/movement-history/user/$userId").body()
    }

    suspend fun getRecentMovements(limit: Int = 50): List<MovementHistoryDTO> {
        return client.get("api/movement-history/recent") {
            parameter("limit", limit)
        }.body()
    }

    suspend fun getPendingApprovalMovements(): List<MovementHistoryDTO> {
        return client.get("api/movement-history/pending-approval").body()
    }

    suspend fun getInboundMovements(limit: Int? = null): List<MovementHistoryDTO> {
        return client.get("api/movement-history/inbound") {
            if (limit != null) parameter("limit", limit)
        }.body()
    }

    suspend fun getOutboundMovements(limit: Int? = null): List<MovementHistoryDTO> {
        return client.get("api/movement-history/outbound") {
            if (limit != null) parameter("limit", limit)
        }.body()
    }

    suspend fun getAdjustmentMovements(limit: Int? = null): List<MovementHistoryDTO> {
        return client.get("api/movement-history/adjustments") {
            if (limit != null) parameter("limit", limit)
        }.body()
    }

    suspend fun getMovementStatsByType(): Map<String, Any> {
        return client.get("api/movement-history/stats/by-type").body()
    }

    suspend fun getMovementStatsByDate(startDate: String, endDate: String): Map<String, Any> {
        return client.get("api/movement-history/stats/by-date") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()
    }

    suspend fun searchMovements(keyword: String, searchIn: String = "reason"): List<MovementHistoryDTO> {
        return client.get("api/movement-history/search") {
            parameter("keyword", keyword)
            parameter("searchIn", searchIn)
        }.body()
    }

    suspend fun approveMovement(movementId: Long, approvalData: ApprovalRequest): MovementHistoryDTO {
        return client.put("api/movement-history/$movementId/approve") {
            setBody(approvalData)
        }.body()
    }

    suspend fun getMovementById(movementId: Long): MovementHistoryDTO {
        return client.get("api/movement-history/$movementId").body()
    }

    suspend fun getMovementsRequiringAttention(): List<MovementHistoryDTO> {
        return client.get("api/movement-history/requiring-attention").body()
    }

    suspend fun getAuditTrail(startDate: String, endDate: String): List<MovementHistoryDTO> {
        return client.get("api/movement-history/audit-trail") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()
    }

    suspend fun getMovementVelocity(startDate: String, endDate: String): Double {
        return client.get("api/movement-history/velocity") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()
    }

    suspend fun getMovementsWithEnvironmentalData(): List<MovementHistoryDTO> {
        return client.get("api/movement-history/environmental-data").body()
    }

    suspend fun getMovementsByBatchId(batchId: String): List<MovementHistoryDTO> {
        return client.get("api/movement-history/batch/$batchId").body()
    }

    suspend fun getMovementsByReferenceNumber(referenceNumber: String): List<MovementHistoryDTO> {
        return client.get("api/movement-history/reference/$referenceNumber").body()
    }
}
data class ApprovalRequest(val approverComment: String? = null)