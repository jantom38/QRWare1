package com.qrware.app.data.repository

import com.qrware.app.data.dto.InventoryItemDTO
import com.qrware.app.data.model.*
import com.qrware.app.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllInventoryItems(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc"
    ): PaginatedResponse<InventoryItemDTO> {
        return apiService.getAllInventoryItems(page, size, sort)
    }


    suspend fun searchInventory(query: String): List<InventoryItemDTO> {
        return apiService.searchInventory(query)
    }

    suspend fun getInventoryItemById(itemId: Long): InventoryItemDTO {
        return apiService.getInventoryItemById(itemId)
    }

    suspend fun getInventoryByProduct(productId: Long): List<InventoryItemDTO> {
        return apiService.getInventoryByProduct(productId)
    }

    suspend fun getInventoryByLocation(locationId: Long): List<InventoryItemDTO> {
        return apiService.getInventoryByLocation(locationId)
    }

    suspend fun getInventoryByStatus(status: InventoryStatus): List<InventoryItemDTO> {
        return apiService.getInventoryByStatus(status)
    }

    suspend fun createInventoryItem(request: CreateInventoryRequest): InventoryItemDTO {
        return apiService.createInventoryItem(request)
    }

    suspend fun updateInventoryItem(itemId: Long, request: UpdateInventoryRequest): InventoryItemDTO {
        return apiService.updateInventoryItem(itemId, request)
    }

    suspend fun deleteInventoryItem(itemId: Long): Response<Unit> {
        return apiService.deleteInventoryItem(itemId)
    }

    suspend fun receiveStock(itemId: Long, request: QuantityUpdateRequest): InventoryItemDTO {
        return apiService.receiveStock(itemId, request)
    }

    suspend fun issueStock(itemId: Long, request: QuantityUpdateRequest): InventoryItemDTO {
        return apiService.issueStock(itemId, request)
    }

    suspend fun getInventoryAlerts(): List<InventoryAlertDTO> {
        return apiService.getInventoryAlerts()
    }
}