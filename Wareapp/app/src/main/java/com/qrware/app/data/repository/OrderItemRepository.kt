package com.qrware.app.data.repository

import com.qrware.app.data.api.OrderApiService
import com.qrware.app.data.api.PagedResponse
import com.qrware.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderItemRepository(private val apiService: OrderApiService) {

    // === ORDER ITEM OPERATIONS ===

    suspend fun getOrderItemById(id: Long): Result<OrderItemDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrderItemById(id)
                if (response.isSuccessful) {
                    response.body()?.data?.let { orderItem ->
                        Result.success(orderItem)
                    } ?: Result.failure(Exception("Order item not found"))
                } else {
                    Result.failure(Exception("Failed to fetch order item: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addOrderItem(orderId: Long, request: CreateOrderItemRequest): Result<OrderItemDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.addOrderItem(orderId, request)
                if (response.isSuccessful) {
                    response.body()?.data?.let { orderItem ->
                        Result.success(orderItem)
                    } ?: Result.failure(Exception("Failed to add order item"))
                } else {
                    Result.failure(Exception("Failed to add order item: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun pickOrderItem(id: Long): Result<OrderItemDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.pickOrderItem(id)
                if (response.isSuccessful) {
                    response.body()?.data?.let { orderItem ->
                        Result.success(orderItem)
                    } ?: Result.failure(Exception("Failed to pick order item"))
                } else {
                    Result.failure(Exception("Failed to pick order item: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun completeOrderItem(id: Long, request: CompleteOrderItemRequest): Result<OrderItemDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.completeOrderItem(id, request)
                if (response.isSuccessful) {
                    response.body()?.data?.let { orderItem ->
                        Result.success(orderItem)
                    } ?: Result.failure(Exception("Failed to complete order item"))
                } else {
                    Result.failure(Exception("Failed to complete order item: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun cancelOrderItem(id: Long, reason: String): Result<OrderItemDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelOrderItem(id, CancelOrderRequest(reason))
                if (response.isSuccessful) {
                    response.body()?.data?.let { orderItem ->
                        Result.success(orderItem)
                    } ?: Result.failure(Exception("Failed to cancel order item"))
                } else {
                    Result.failure(Exception("Failed to cancel order item: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // === QR CODE OPERATIONS ===

    suspend fun scanQRCode(qrCodeData: String): Result<OrderItemDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.scanQRCode(ScanQRRequest(qrCodeData))
                if (response.isSuccessful) {
                    response.body()?.data?.let { orderItem ->
                        Result.success(orderItem)
                    } ?: Result.failure(Exception("Failed to process QR code"))
                } else {
                    val errorMessage = response.body()?.message ?: response.message()
                    Result.failure(Exception("QR scan failed: $errorMessage"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getOrderItemByQR(qrCodeData: String): Result<OrderItemDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrderItemByQR(qrCodeData)
                if (response.isSuccessful) {
                    response.body()?.data?.let { orderItem ->
                        Result.success(orderItem)
                    } ?: Result.failure(Exception("No order item found for QR code"))
                } else {
                    Result.failure(Exception("Failed to find order item: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getItemsRequiringQRScan(): Result<List<OrderItemDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getItemsRequiringQRScan()
                if (response.isSuccessful) {
                    response.body()?.data?.let { items ->
                        Result.success(items)
                    } ?: Result.failure(Exception("No items found"))
                } else {
                    Result.failure(Exception("Failed to fetch items requiring QR scan: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // === BATCH AND SERIAL OPERATIONS ===

    suspend fun setBatchNumber(id: Long, batchNumber: String): Result<OrderItemDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.setBatchNumber(id, SetBatchRequest(batchNumber))
                if (response.isSuccessful) {
                    response.body()?.data?.let { orderItem ->
                        Result.success(orderItem)
                    } ?: Result.failure(Exception("Failed to set batch number"))
                } else {
                    Result.failure(Exception("Failed to set batch number: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun setSerialNumber(id: Long, serialNumber: String): Result<OrderItemDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.setSerialNumber(id, SetSerialRequest(serialNumber))
                if (response.isSuccessful) {
                    response.body()?.data?.let { orderItem ->
                        Result.success(orderItem)
                    } ?: Result.failure(Exception("Failed to set serial number"))
                } else {
                    Result.failure(Exception("Failed to set serial number: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // === LIST AND SEARCH OPERATIONS ===

    suspend fun getActiveOrderItems(): Result<List<OrderItemDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getActiveOrderItems()
                if (response.isSuccessful) {
                    response.body()?.data?.let { items ->
                        Result.success(items)
                    } ?: Result.failure(Exception("No active order items found"))
                } else {
                    Result.failure(Exception("Failed to fetch active order items: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getPartiallyCompletedItems(): Result<List<OrderItemDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPartiallyCompletedItems()
                if (response.isSuccessful) {
                    response.body()?.data?.let { items ->
                        Result.success(items)
                    } ?: Result.failure(Exception("No partially completed items found"))
                } else {
                    Result.failure(Exception("Failed to fetch partially completed items: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchOrderItems(query: String, page: Int = 0, size: Int = 20): Result<PagedResponse<OrderItemDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchOrderItems(query, page, size)
                if (response.isSuccessful) {
                    response.body()?.data?.let { items ->
                        Result.success(items)
                    } ?: Result.failure(Exception("No order items found"))
                } else {
                    Result.failure(Exception("Failed to search order items: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getOrderItemStatistics(): Result<List<ItemStatusCountDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrderItemStatistics()
                if (response.isSuccessful) {
                    response.body()?.data?.let { stats ->
                        Result.success(stats)
                    } ?: Result.failure(Exception("No statistics found"))
                } else {
                    Result.failure(Exception("Failed to fetch statistics: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}