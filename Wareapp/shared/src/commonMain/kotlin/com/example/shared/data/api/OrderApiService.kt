package com.example.shared.data.api

import com.example.shared.data.dto.*
import com.example.shared.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*

class OrderApiService(private val client: HttpClient) {

    // === ORDER ENDPOINTS ===

    suspend fun getAllOrders(page: Int = 0, size: Int = 20, sort: String = "createdAt,desc"): ApiResponse<PagedResponse<OrderDTO>> {
        return client.get("api/orders") {
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
        }.body()
    }

    suspend fun getOrderById(id: Long): ApiResponse<OrderDTO> {
        return client.get("api/orders/$id").body()
    }

    suspend fun getOrderByNumber(orderNumber: String): ApiResponse<OrderDTO> {
        return client.get("api/orders/number/$orderNumber").body()
    }

    suspend fun createOrder(request: CreateOrderRequest): ApiResponse<OrderDTO> {
        return client.post("api/orders") {
            setBody(request)
        }.body()
    }

    suspend fun startOrder(id: Long): ApiResponse<OrderDTO> {
        return client.put("api/orders/$id/start").body()
    }

    suspend fun completeOrder(id: Long): ApiResponse<OrderDTO> {
        return client.put("api/orders/$id/complete").body()
    }

    suspend fun cancelOrder(id: Long, request: CancelOrderRequest): ApiResponse<OrderDTO> {
        return client.put("api/orders/$id/cancel") {
            setBody(request)
        }.body()
    }

    suspend fun assignOrder(id: Long, request: AssignOrderRequest): ApiResponse<OrderDTO> {
        return client.put("api/orders/$id/assign") {
            setBody(request)
        }.body()
    }

    suspend fun getActiveOrders(): ApiResponse<List<OrderDTO>> {
        return client.get("api/orders/active").body()
    }

    suspend fun getMyOrders(): ApiResponse<List<OrderDTO>> {
        return client.get("api/orders/my-orders").body()
    }

    suspend fun getOverdueOrders(): ApiResponse<List<OrderDTO>> {
        return client.get("api/orders/overdue").body()
    }

    suspend fun getHighPriorityOrders(): ApiResponse<List<OrderDTO>> {
        return client.get("api/orders/high-priority").body()
    }

    suspend fun searchOrders(searchQuery: String, page: Int = 0, size: Int = 20): ApiResponse<PagedResponse<OrderDTO>> {
        return client.get("api/orders/search") {
            parameter("q", searchQuery)
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getOrderStatistics(): ApiResponse<List<StatusCountDTO>> {
        return client.get("api/orders/statistics/status").body()
    }

    // === ORDER ITEM ENDPOINTS ===

    suspend fun getOrderItemById(id: Long): ApiResponse<OrderItemDTO> {
        return client.get("api/order-items/$id").body()
    }

    suspend fun addOrderItem(orderId: Long, request: CreateOrderItemRequest): ApiResponse<OrderItemDTO> {
        return client.post("api/order-items/order/$orderId") {
            setBody(request)
        }.body()
    }

    suspend fun pickOrderItem(id: Long): ApiResponse<OrderItemDTO> {
        return client.put("api/order-items/$id/pick").body()
    }

    suspend fun completeOrderItem(id: Long, request: CompleteOrderItemRequest): ApiResponse<OrderItemDTO> {
        return client.put("api/order-items/$id/complete") {
            setBody(request)
        }.body()
    }

    suspend fun cancelOrderItem(id: Long, request: CancelOrderRequest): ApiResponse<OrderItemDTO> {
        return client.put("api/order-items/$id/cancel") {
            setBody(request)
        }.body()
    }

    suspend fun scanQRCode(request: ScanQRRequest): ApiResponse<Any> {
        return client.post("api/order-items/scan-qr") {
            setBody(request)
        }.body()
    }

    suspend fun getOrderItemByQR(qrCodeData: String): ApiResponse<OrderItemDTO> {
        return client.get("api/order-items/qr/$qrCodeData").body()
    }

    suspend fun setBatchNumber(id: Long, request: SetBatchRequest): ApiResponse<OrderItemDTO> {
        return client.put("api/order-items/$id/batch") {
            setBody(request)
        }.body()
    }

    suspend fun setSerialNumber(id: Long, request: SetSerialRequest): ApiResponse<OrderItemDTO> {
        return client.put("api/order-items/$id/serial") {
            setBody(request)
        }.body()
    }

    suspend fun getActiveOrderItems(): ApiResponse<List<OrderItemDTO>> {
        return client.get("api/order-items/active").body()
    }

    suspend fun getItemsRequiringQRScan(): ApiResponse<List<OrderItemDTO>> {
        return client.get("api/order-items/pending-qr").body()
    }

    suspend fun getPartiallyCompletedItems(): ApiResponse<List<OrderItemDTO>> {
        return client.get("api/order-items/partially-completed").body()
    }

    suspend fun searchOrderItems(searchQuery: String, page: Int = 0, size: Int = 20): ApiResponse<PagedResponse<OrderItemDTO>> {
        return client.get("api/order-items/search") {
            parameter("q", searchQuery)
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getOrderItemStatistics(): ApiResponse<List<ItemStatusCountDTO>> {
        return client.get("api/order-items/statistics/status").body()
    }
}data class PagedResponse<T>(
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