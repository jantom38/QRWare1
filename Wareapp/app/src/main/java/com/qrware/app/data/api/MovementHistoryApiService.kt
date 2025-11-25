package com.qrware.app.data.api

import com.qrware.app.data.dto.MovementHistoryDTO
import retrofit2.Response
import retrofit2.http.*

interface MovementHistoryApiService {

    @GET("api/movement-history/inventory-item/{itemId}")
    suspend fun getMovementHistoryByItemId(@Path("itemId") itemId: Long): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/product/{productId}")
    suspend fun getMovementHistoryByProductId(@Path("productId") productId: Long): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/location/{locationId}")
    suspend fun getMovementHistoryByLocationId(@Path("locationId") locationId: Long): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/type/{movementType}")
    suspend fun getMovementHistoryByType(@Path("movementType") movementType: String): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/date-range")
    suspend fun getMovementHistoryByDateRange(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/user/{userId}")
    suspend fun getMovementHistoryByUserId(@Path("userId") userId: String): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/recent")
    suspend fun getRecentMovements(@Query("limit") limit: Int = 50): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/pending-approval")
    suspend fun getPendingApprovalMovements(): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/inbound")
    suspend fun getInboundMovements(@Query("limit") limit: Int? = null): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/outbound")
    suspend fun getOutboundMovements(@Query("limit") limit: Int? = null): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/adjustments")
    suspend fun getAdjustmentMovements(@Query("limit") limit: Int? = null): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/stats/by-type")
    suspend fun getMovementStatsByType(): Response<Map<String, Any>>

    @GET("api/movement-history/stats/by-date")
    suspend fun getMovementStatsByDate(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<Map<String, Any>>

    @GET("api/movement-history/search")
    suspend fun searchMovements(
        @Query("keyword") keyword: String,
        @Query("searchIn") searchIn: String = "reason"
    ): Response<List<MovementHistoryDTO>>

    @PUT("api/movement-history/{movementId}/approve")
    suspend fun approveMovement(
        @Path("movementId") movementId: Long,
        @Body approvalData: ApprovalRequest
    ): Response<MovementHistoryDTO>

    @GET("api/movement-history/{movementId}")
    suspend fun getMovementById(@Path("movementId") movementId: Long): Response<MovementHistoryDTO>

    @GET("api/movement-history/requiring-attention")
    suspend fun getMovementsRequiringAttention(): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/audit-trail")
    suspend fun getAuditTrail(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/velocity")
    suspend fun getMovementVelocity(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<Double>

    @GET("api/movement-history/environmental-data")
    suspend fun getMovementsWithEnvironmentalData(): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/batch/{batchId}")
    suspend fun getMovementsByBatchId(@Path("batchId") batchId: String): Response<List<MovementHistoryDTO>>

    @GET("api/movement-history/reference/{referenceNumber}")
    suspend fun getMovementsByReferenceNumber(@Path("referenceNumber") referenceNumber: String): Response<List<MovementHistoryDTO>>
}

data class ApprovalRequest(val approverComment: String? = null)