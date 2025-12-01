package com.qrware.app.data.api

import com.qrware.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApiService {

    // === ORDER ENDPOINTS ===

    @GET("api/orders")
    suspend fun getAllOrders(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<ApiResponse<PagedResponse<OrderDTO>>>

    @GET("api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): Response<ApiResponse<OrderDTO>>

    @GET("api/orders/number/{orderNumber}")
    suspend fun getOrderByNumber(@Path("orderNumber") orderNumber: String): Response<ApiResponse<OrderDTO>>

    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<OrderDTO>>

    @PUT("api/orders/{id}/start")
    suspend fun startOrder(@Path("id") id: Long): Response<ApiResponse<OrderDTO>>

    @PUT("api/orders/{id}/complete")
    suspend fun completeOrder(@Path("id") id: Long): Response<ApiResponse<OrderDTO>>

    @PUT("api/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") id: Long, 
        @Body request: CancelOrderRequest
    ): Response<ApiResponse<OrderDTO>>

    @PUT("api/orders/{id}/assign")
    suspend fun assignOrder(
        @Path("id") id: Long, 
        @Body request: AssignOrderRequest
    ): Response<ApiResponse<OrderDTO>>

    @GET("api/orders/active")
    suspend fun getActiveOrders(): Response<ApiResponse<List<OrderDTO>>>

    @GET("api/orders/my-orders")
    suspend fun getMyOrders(): Response<ApiResponse<List<OrderDTO>>>

    @GET("api/orders/overdue")
    suspend fun getOverdueOrders(): Response<ApiResponse<List<OrderDTO>>>

    @GET("api/orders/high-priority")
    suspend fun getHighPriorityOrders(): Response<ApiResponse<List<OrderDTO>>>

    @GET("api/orders/search")
    suspend fun searchOrders(
        @Query("q") searchQuery: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PagedResponse<OrderDTO>>>

    @GET("api/orders/statistics/status")
    suspend fun getOrderStatistics(): Response<ApiResponse<List<StatusCountDTO>>>

    // === ORDER ITEM ENDPOINTS ===

    @GET("api/order-items/{id}")
    suspend fun getOrderItemById(@Path("id") id: Long): Response<ApiResponse<OrderItemDTO>>

    @POST("api/order-items/order/{orderId}")
    suspend fun addOrderItem(
        @Path("orderId") orderId: Long,
        @Body request: CreateOrderItemRequest
    ): Response<ApiResponse<OrderItemDTO>>

    @PUT("api/order-items/{id}/pick")
    suspend fun pickOrderItem(@Path("id") id: Long): Response<ApiResponse<OrderItemDTO>>

    @PUT("api/order-items/{id}/complete")
    suspend fun completeOrderItem(
        @Path("id") id: Long,
        @Body request: CompleteOrderItemRequest
    ): Response<ApiResponse<OrderItemDTO>>

    @PUT("api/order-items/{id}/cancel")
    suspend fun cancelOrderItem(
        @Path("id") id: Long,
        @Body request: CancelOrderRequest
    ): Response<ApiResponse<OrderItemDTO>>

    @POST("api/order-items/scan-qr")
    suspend fun scanQRCode(@Body request: ScanQRRequest): Response<ApiResponse<OrderItemDTO>>

    @GET("api/order-items/qr/{qrCodeData}")
    suspend fun getOrderItemByQR(@Path("qrCodeData") qrCodeData: String): Response<ApiResponse<OrderItemDTO>>

    @PUT("api/order-items/{id}/batch")
    suspend fun setBatchNumber(
        @Path("id") id: Long,
        @Body request: SetBatchRequest
    ): Response<ApiResponse<OrderItemDTO>>

    @PUT("api/order-items/{id}/serial")
    suspend fun setSerialNumber(
        @Path("id") id: Long,
        @Body request: SetSerialRequest
    ): Response<ApiResponse<OrderItemDTO>>

    @GET("api/order-items/active")
    suspend fun getActiveOrderItems(): Response<ApiResponse<List<OrderItemDTO>>>

    @GET("api/order-items/pending-qr")
    suspend fun getItemsRequiringQRScan(): Response<ApiResponse<List<OrderItemDTO>>>

    @GET("api/order-items/partially-completed")
    suspend fun getPartiallyCompletedItems(): Response<ApiResponse<List<OrderItemDTO>>>

    @GET("api/order-items/search")
    suspend fun searchOrderItems(
        @Query("q") searchQuery: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PagedResponse<OrderItemDTO>>>

    @GET("api/order-items/statistics/status")
    suspend fun getOrderItemStatistics(): Response<ApiResponse<List<ItemStatusCountDTO>>>
}

// Helper data class for paged responses
data class PagedResponse<T>(
    val content: List<T>,
    val pageable: Pageable,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val first: Boolean,
    val numberOfElements: Int,
    val size: Int,
    val number: Int,
    val sort: Sort,
    val empty: Boolean
)

data class Pageable(
    val sort: Sort,
    val pageNumber: Int,
    val pageSize: Int,
    val offset: Long,
    val paged: Boolean,
    val unpaged: Boolean
)

data class Sort(
    val sorted: Boolean,
    val unsorted: Boolean,
    val empty: Boolean
)