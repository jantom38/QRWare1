package com.qrware.app.data.api

import com.qrware.app.data.dto.MovementHistoryDTO
import com.qrware.app.data.model.MovementType
import retrofit2.Response
import retrofit2.http.*

interface MovementHistoryApiService {

    /**
     * Get movement history by inventory item ID
     */
    @GET("api/movement-history/inventory-item/{itemId}")
    suspend fun getMovementHistoryByItemId(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: Long
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get movement history by product ID
     */
    @GET("api/movement-history/product/{productId}")
    suspend fun getMovementHistoryByProductId(
        @Header("Authorization") token: String,
        @Path("productId") productId: Long
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get movement history by location ID
     */
    @GET("api/movement-history/location/{locationId}")
    suspend fun getMovementHistoryByLocationId(
        @Header("Authorization") token: String,
        @Path("locationId") locationId: Long
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get movement history by movement type
     */
    @GET("api/movement-history/type/{movementType}")
    suspend fun getMovementHistoryByType(
        @Header("Authorization") token: String,
        @Path("movementType") movementType: String
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get movement history by date range
     */
    @GET("api/movement-history/date-range")
    suspend fun getMovementHistoryByDateRange(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String, // ISO format: yyyy-MM-ddTHH:mm:ss
        @Query("endDate") endDate: String
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get movement history by user
     */
    @GET("api/movement-history/user/{userId}")
    suspend fun getMovementHistoryByUserId(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get recent movements for dashboard
     */
    @GET("api/movement-history/recent")
    suspend fun getRecentMovements(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get pending approval movements
     */
    @GET("api/movement-history/pending-approval")
    suspend fun getPendingApprovalMovements(
        @Header("Authorization") token: String
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get inbound movements
     */
    @GET("api/movement-history/inbound")
    suspend fun getInboundMovements(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int? = null
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get outbound movements
     */
    @GET("api/movement-history/outbound")
    suspend fun getOutboundMovements(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int? = null
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get adjustment movements
     */
    @GET("api/movement-history/adjustments")
    suspend fun getAdjustmentMovements(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int? = null
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get movement statistics by type
     */
    @GET("api/movement-history/stats/by-type")
    suspend fun getMovementStatsByType(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    /**
     * Get movement statistics by date
     */
    @GET("api/movement-history/stats/by-date")
    suspend fun getMovementStatsByDate(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<Map<String, Any>>

    /**
     * Search movements by reason or notes
     */
    @GET("api/movement-history/search")
    suspend fun searchMovements(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String,
        @Query("searchIn") searchIn: String = "reason" // "reason", "notes", "both"
    ): Response<List<MovementHistoryDTO>>

    /**
     * Approve a movement
     */
    @PUT("api/movement-history/{movementId}/approve")
    suspend fun approveMovement(
        @Header("Authorization") token: String,
        @Path("movementId") movementId: Long,
        @Body approvalData: ApprovalRequest
    ): Response<MovementHistoryDTO>

    /**
     * Get movement details by ID
     */
    @GET("api/movement-history/{movementId}")
    suspend fun getMovementById(
        @Header("Authorization") token: String,
        @Path("movementId") movementId: Long
    ): Response<MovementHistoryDTO>

    /**
     * Get movements requiring attention
     */
    @GET("api/movement-history/requiring-attention")
    suspend fun getMovementsRequiringAttention(
        @Header("Authorization") token: String
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get audit trail for compliance
     */
    @GET("api/movement-history/audit-trail")
    suspend fun getAuditTrail(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get movement velocity (movements per hour)
     */
    @GET("api/movement-history/velocity")
    suspend fun getMovementVelocity(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<Double>

    /**
     * Get movements with environmental data
     */
    @GET("api/movement-history/environmental-data")
    suspend fun getMovementsWithEnvironmentalData(
        @Header("Authorization") token: String
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get movements by batch ID
     */
    @GET("api/movement-history/batch/{batchId}")
    suspend fun getMovementsByBatchId(
        @Header("Authorization") token: String,
        @Path("batchId") batchId: String
    ): Response<List<MovementHistoryDTO>>

    /**
     * Get movements by reference number
     */
    @GET("api/movement-history/reference/{referenceNumber}")
    suspend fun getMovementsByReferenceNumber(
        @Header("Authorization") token: String,
        @Path("referenceNumber") referenceNumber: String
    ): Response<List<MovementHistoryDTO>>
}

data class ApprovalRequest(
    val approverComment: String? = null
)