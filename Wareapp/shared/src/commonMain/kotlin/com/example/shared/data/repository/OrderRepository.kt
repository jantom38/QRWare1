package com.example.shared.data.repository

import com.example.shared.data.api.OrderApiService
import com.example.shared.data.api.PagedResponse
import com.example.shared.data.model.OrderDTO
import com.example.shared.data.model.StatusCountDTO
import com.example.shared.data.model.*

class OrderRepository(private val apiService: OrderApiService) {

    // === ORDER OPERATIONS ===

    suspend fun getAllOrders(page: Int = 0, size: Int = 20): Result<PagedResponse<OrderDTO>> {
        return try {
            val response = apiService.getAllOrders(page, size)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderById(id: Long): Result<OrderDTO> {
        return try {
            val response = apiService.getOrderById(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderByNumber(orderNumber: String): Result<OrderDTO> {
        return try {
            val response = apiService.getOrderByNumber(orderNumber)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrder(request: CreateOrderRequest): Result<OrderDTO> {
        return try {
            val response = apiService.createOrder(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startOrder(id: Long): Result<OrderDTO> {
        return try {
            val response = apiService.startOrder(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeOrder(id: Long): Result<OrderDTO> {
        return try {
            val response = apiService.completeOrder(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelOrder(id: Long, reason: String): Result<OrderDTO> {
        return try {
            val response = apiService.cancelOrder(id, CancelOrderRequest(reason))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignOrder(id: Long, assignedToId: Long): Result<OrderDTO> {
        return try {
            val response = apiService.assignOrder(id, AssignOrderRequest(assignedToId))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyOrders(): Result<List<OrderDTO>> {
        return try {
            val response = apiService.getMyOrders()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveOrders(): Result<List<OrderDTO>> {
        return try {
            val response = apiService.getActiveOrders()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOverdueOrders(): Result<List<OrderDTO>> {
        return try {
            val response = apiService.getOverdueOrders()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHighPriorityOrders(): Result<List<OrderDTO>> {
        return try {
            val response = apiService.getHighPriorityOrders()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchOrders(query: String, page: Int = 0, size: Int = 20): Result<PagedResponse<OrderDTO>> {
        return try {
            val response = apiService.searchOrders(query, page, size)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderStatistics(): Result<List<StatusCountDTO>> {
        return try {
            val response = apiService.getOrderStatistics()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}