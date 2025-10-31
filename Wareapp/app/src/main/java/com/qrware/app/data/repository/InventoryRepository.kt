package com.qrware.app.data.repository

import com.qrware.app.data.model.*
import com.qrware.app.data.dto.InventoryItemDTO
import com.qrware.app.data.remote.ApiService
import retrofit2.Response // Potrzebne dla delete
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * ZMIANA: Zwracamy PaginatedResponse<...> bezpośrednio
     */
    suspend fun getAllInventoryItems(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc"
    ): PaginatedResponse<InventoryItemDTO> { // <-- ZMIANA
        return apiService.getAllInventoryItems(page, size, sort)
    }

    suspend fun getInventoryItemById(itemId: Long): InventoryItemDTO { // <-- ZMIANA
        return apiService.getInventoryItemById(itemId)
    }

    suspend fun getInventoryByProduct(productId: Long): List<InventoryItemDTO> { // <-- ZMIANA
        return apiService.getInventoryByProduct(productId)
    }

    suspend fun getInventoryByLocation(locationId: Long): List<InventoryItemDTO> { // <-- ZMIANA
        return apiService.getInventoryByLocation(locationId)
    }

    suspend fun getInventoryByStatus(status: InventoryStatus): List<InventoryItemDTO> { // <-- ZMIANA
        return apiService.getInventoryByStatus(status)
    }

    suspend fun createInventoryItem(request: CreateInventoryRequest): InventoryItemDTO { // <-- ZMIANA
        return apiService.createInventoryItem(request)
    }

    suspend fun updateInventoryItem(itemId: Long, request: UpdateInventoryRequest): InventoryItemDTO { // <-- ZMIANA
        return apiService.updateInventoryItem(itemId, request)
    }

    suspend fun deleteInventoryItem(itemId: Long): Response<Unit> { // <-- ZMIANA
        return apiService.deleteInventoryItem(itemId)
    }

    suspend fun receiveStock(itemId: Long, request: QuantityUpdateRequest): InventoryItemDTO { // <-- ZMIANA
        return apiService.receiveStock(itemId, request)
    }

    suspend fun issueStock(itemId: Long, request: QuantityUpdateRequest): InventoryItemDTO { // <-- ZMIANA
        return apiService.issueStock(itemId, request)
    }
}