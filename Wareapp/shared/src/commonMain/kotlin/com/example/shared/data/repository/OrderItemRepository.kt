package com.example.shared.data.repository

import com.example.shared.data.api.OrderApiService
import com.example.shared.data.api.PagedResponse
import com.example.shared.data.model.PaginatedResponse
import com.example.shared.data.model.OrderItemDTO
import com.example.shared.data.dto.InventoryItemDTO
import com.example.shared.data.model.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString

class OrderItemRepository(
    private val apiService: OrderApiService
) {
    // Inicjalizacja JSON
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    // === ORDER ITEM OPERATIONS ===

    suspend fun getOrderItemById(id: Long): Result<OrderItemDTO> {
        return try {
            val response = apiService.getOrderItemById(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOrderItem(orderId: Long, request: CreateOrderItemRequest): Result<OrderItemDTO> {
        return try {
            val response = apiService.addOrderItem(orderId, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pickOrderItem(id: Long): Result<OrderItemDTO> {
        return try {
            val response = apiService.pickOrderItem(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeOrderItem(id: Long, request: CompleteOrderItemRequest): Result<OrderItemDTO> {
        return try {
            val response = apiService.completeOrderItem(id, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelOrderItem(id: Long, reason: String): Result<OrderItemDTO> {
        return try {
            val response = apiService.cancelOrderItem(id, CancelOrderRequest(reason))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === QR CODE OPERATIONS ===

    suspend fun scanQRCode(qrCodeData: String, orderId: Long? = null): Result<Any> {
        return try {
            val response = apiService.scanQRCode(ScanQRRequest(qrCodeData, orderId))
            if (response.success && response.data != null) {
                val rawData = response.data

                // Krok 1: Przekonwertuj 'Any' (który jest zazwyczaj Mapą lub JsonObject w Ktor) na JsonElement
                // Ponieważ nie znamy typu 'rawData', spróbujmy go zserializować ponownie do stringa
                // a potem sparsować jako JsonElement. To bezpieczne obejście w KMP.
                val jsonString = try {
                    // Zakładamy, że rawData to coś co Ktor/Serialization potrafi zserializować (np. Mapa)
                    // Jeśli to już JsonElement, to encodeToString zadziała.
                    json.encodeToString(rawData) // To może wymagać castowania rawData na konkretny typ jeśli Ktor zwraca LinkedHashMap
                } catch (e: Exception) {
                    println("Błąd serializacji rawData: $e")
                    return Result.failure(Exception("Błąd przetwarzania danych QR"))
                }

                val jsonElement = json.parseToJsonElement(jsonString)

                if (jsonElement is JsonObject) {
                    println("OrderItemRepository: Analizowanie JSON: $jsonElement")

                    if (jsonElement.containsKey("requestedQuantity")) {
                        val orderItem = json.decodeFromJsonElement<OrderItemDTO>(jsonElement)
                        println("OrderItemRepository: Zdeserializowano jako OrderItemDTO")
                        Result.success(orderItem)
                    } else if (jsonElement.containsKey("availableQuantity")) {
                        val inventoryItem = json.decodeFromJsonElement<InventoryItemDTO>(jsonElement)
                        println("OrderItemRepository: Zdeserializowano jako InventoryItemDTO")
                        Result.success(inventoryItem)
                    } else {
                        println("OrderItemRepository: Nieznany typ danych")
                        Result.failure(Exception("Nieznany typ danych w odpowiedzi"))
                    }
                } else {
                    println("OrderItemRepository: Nieprawidłowy format danych (nie jest obiektem JSON)")
                    Result.failure(Exception("Nieprawidłowy format danych w odpowiedzi"))
                }
            } else {
                val errorMessage = response.message
                println("OrderItemRepository: Skanowanie QR nieudane: $errorMessage")
                Result.failure(Exception("Skanowanie QR nieudane: $errorMessage"))
            }
        } catch (e: Exception) {
            println("OrderItemRepository: Wyjątek w scanQRCode: $e")
            Result.failure(e)
        }
    }

    suspend fun getOrderItemByQR(qrCodeData: String): Result<OrderItemDTO> {
        return try {
            val response = apiService.getOrderItemByQR(qrCodeData)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemsRequiringQRScan(): Result<List<OrderItemDTO>> {
        return try {
            val response = apiService.getItemsRequiringQRScan()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === BATCH AND SERIAL OPERATIONS ===

    suspend fun setBatchNumber(id: Long, batchNumber: String): Result<OrderItemDTO> {
        return try {
            val response = apiService.setBatchNumber(id, SetBatchRequest(batchNumber))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setSerialNumber(id: Long, serialNumber: String): Result<OrderItemDTO> {
        return try {
            val response = apiService.setSerialNumber(id, SetSerialRequest(serialNumber))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === LIST AND SEARCH OPERATIONS ===

    suspend fun getActiveOrderItems(): Result<List<OrderItemDTO>> {
        return try {
            val response = apiService.getActiveOrderItems()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPartiallyCompletedItems(): Result<List<OrderItemDTO>> {
        return try {
            val response = apiService.getPartiallyCompletedItems()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchOrderItems(query: String, page: Int = 0, size: Int = 20): Result<PagedResponse<OrderItemDTO>> {
        return try {
            val response = apiService.searchOrderItems(query, page, size)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderItemStatistics(): Result<List<ItemStatusCountDTO>> {
        return try {
            val response = apiService.getOrderItemStatistics()
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