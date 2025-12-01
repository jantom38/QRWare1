package com.qrware.app.data.repository

import com.qrware.app.data.api.OrderApiService
import com.qrware.app.data.api.PagedResponse
import com.qrware.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository(private val apiService: OrderApiService) {

    // === ORDER OPERATIONS ===

    suspend fun getAllOrders(page: Int = 0, size: Int = 20): Result<PagedResponse<OrderDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllOrders(page, size)
                if (response.isSuccessful) {
                    response.body()?.data?.let { orders ->
                        Result.success(orders)
                    } ?: Result.failure(Exception("No data received"))
                } else {
                    Result.failure(Exception("Failed to fetch orders: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getOrderById(id: Long): Result<OrderDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrderById(id)
                if (response.isSuccessful) {
                    response.body()?.data?.let { order ->
                        Result.success(order)
                    } ?: Result.failure(Exception("Order not found"))
                } else {
                    Result.failure(Exception("Failed to fetch order: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getOrderByNumber(orderNumber: String): Result<OrderDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrderByNumber(orderNumber)
                if (response.isSuccessful) {
                    response.body()?.data?.let { order ->
                        Result.success(order)
                    } ?: Result.failure(Exception("Order not found"))
                } else {
                    Result.failure(Exception("Failed to fetch order: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createOrder(request: CreateOrderRequest): Result<OrderDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createOrder(request)
                if (response.isSuccessful) {
                    response.body()?.data?.let { order ->
                        Result.success(order)
                    } ?: Result.failure(Exception("Failed to create order"))
                } else {
                    Result.failure(Exception("Failed to create order: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun startOrder(id: Long): Result<OrderDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.startOrder(id)
                if (response.isSuccessful) {
                    response.body()?.data?.let { order ->
                        Result.success(order)
                    } ?: Result.failure(Exception("Failed to start order"))
                } else {
                    Result.failure(Exception("Failed to start order: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun completeOrder(id: Long): Result<OrderDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.completeOrder(id)
                if (response.isSuccessful) {
                    response.body()?.data?.let { order ->
                        Result.success(order)
                    } ?: Result.failure(Exception("Failed to complete order"))
                } else {
                    Result.failure(Exception("Failed to complete order: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun cancelOrder(id: Long, reason: String): Result<OrderDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelOrder(id, CancelOrderRequest(reason))
                if (response.isSuccessful) {
                    response.body()?.data?.let { order ->
                        Result.success(order)
                    } ?: Result.failure(Exception("Failed to cancel order"))
                } else {
                    Result.failure(Exception("Failed to cancel order: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun assignOrder(id: Long, assignedToId: Long): Result<OrderDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.assignOrder(id, AssignOrderRequest(assignedToId))
                if (response.isSuccessful) {
                    response.body()?.data?.let { order ->
                        Result.success(order)
                    } ?: Result.failure(Exception("Failed to assign order"))
                } else {
                    Result.failure(Exception("Failed to assign order: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMyOrders(): Result<List<OrderDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyOrders()
                if (response.isSuccessful) {
                    response.body()?.data?.let { orders ->
                        Result.success(orders)
                    } ?: Result.failure(Exception("No orders found"))
                } else {
                    Result.failure(Exception("Failed to fetch my orders: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getActiveOrders(): Result<List<OrderDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getActiveOrders()
                if (response.isSuccessful) {
                    response.body()?.data?.let { orders ->
                        Result.success(orders)
                    } ?: Result.failure(Exception("No active orders found"))
                } else {
                    Result.failure(Exception("Failed to fetch active orders: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getOverdueOrders(): Result<List<OrderDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOverdueOrders()
                if (response.isSuccessful) {
                    response.body()?.data?.let { orders ->
                        Result.success(orders)
                    } ?: Result.failure(Exception("No overdue orders found"))
                } else {
                    Result.failure(Exception("Failed to fetch overdue orders: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getHighPriorityOrders(): Result<List<OrderDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getHighPriorityOrders()
                if (response.isSuccessful) {
                    response.body()?.data?.let { orders ->
                        Result.success(orders)
                    } ?: Result.failure(Exception("No high priority orders found"))
                } else {
                    Result.failure(Exception("Failed to fetch high priority orders: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchOrders(query: String, page: Int = 0, size: Int = 20): Result<PagedResponse<OrderDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchOrders(query, page, size)
                if (response.isSuccessful) {
                    response.body()?.data?.let { orders ->
                        Result.success(orders)
                    } ?: Result.failure(Exception("No orders found"))
                } else {
                    Result.failure(Exception("Failed to search orders: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getOrderStatistics(): Result<List<StatusCountDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrderStatistics()
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