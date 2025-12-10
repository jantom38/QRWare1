package com.qrware.shared.data.repository

import com.qrware.shared.data.model.*
import com.qrware.shared.data.network.InventoryApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Inventory Repository - zarządzanie stanami magazynowymi
 * Business logic layer dla inwentarza, lokalizacji i historii ruchów
 */
class InventoryRepository(
    private val inventoryApiService: InventoryApiService
) {
    
    // State management
    private val _inventoryState = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryState: StateFlow<List<InventoryItem>> = _inventoryState.asStateFlow()
    
    private val _locationsState = MutableStateFlow<List<Location>>(emptyList())
    val locationsState: StateFlow<List<Location>> = _locationsState.asStateFlow()
    
    private val _zonesState = MutableStateFlow<List<Zone>>(emptyList())
    val zonesState: StateFlow<List<Zone>> = _zonesState.asStateFlow()
    
    private val _movementHistoryState = MutableStateFlow<List<MovementHistoryDTO>>(emptyList())
    val movementHistoryState: StateFlow<List<MovementHistoryDTO>> = _movementHistoryState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- INVENTORY MANAGEMENT ---

    /**
     * Pobierz wszystkie pozycje inwentarza
     */
    suspend fun loadAllInventory(
        page: Int = 0,
        size: Int = 20
    ): Result<PaginatedResponse<InventoryItem>> {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val result = inventoryApiService.getAllInventory(page, size)
            
            result.fold(
                onSuccess = { paginatedResponse ->
                    if (page == 0) {
                        _inventoryState.value = paginatedResponse.content
                    } else {
                        _inventoryState.value = _inventoryState.value + paginatedResponse.content
                    }
                    _isLoading.value = false
                    Result.success(paginatedResponse)
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load inventory: ${error.message}"
                    _isLoading.value = false
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _errorMessage.value = "Error loading inventory: ${e.message}"
            _isLoading.value = false
            Result.failure(e)
        }
    }

    /**
     * Pobierz pozycję inwentarza po ID
     */
    suspend fun getInventoryById(inventoryId: Long): Result<InventoryItem> {
        return try {
            inventoryApiService.getInventoryById(inventoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz inwentarz po kodzie QR
     */
    suspend fun getInventoryByQR(qrCode: String): Result<InventoryItem> {
        if (qrCode.isBlank()) {
            return Result.failure(Exception("QR code cannot be empty"))
        }
        
        return try {
            inventoryApiService.getInventoryByQR(qrCode.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz inwentarz dla produktu
     */
    suspend fun getInventoryByProduct(productId: Long): Result<List<InventoryItem>> {
        return try {
            inventoryApiService.getInventoryByProduct(productId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz inwentarz dla lokalizacji
     */
    suspend fun getInventoryByLocation(locationId: Long): Result<List<InventoryItem>> {
        return try {
            inventoryApiService.getInventoryByLocation(locationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz inwentarz według statusu
     */
    suspend fun getInventoryByStatus(status: InventoryStatus): Result<List<InventoryItem>> {
        return try {
            inventoryApiService.getInventoryByStatus(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyszukaj inwentarz
     */
    suspend fun searchInventory(query: String): Result<List<InventoryItem>> {
        if (query.isBlank()) {
            return Result.success(_inventoryState.value)
        }
        
        return try {
            inventoryApiService.searchInventory(query)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stwórz nową pozycję inwentarza
     */
    suspend fun createInventory(
        productId: Long,
        locationId: Long,
        quantity: Int,
        qrCode: String,
        status: InventoryStatus = InventoryStatus.AVAILABLE,
        lotNumber: String? = null,
        batchNumber: String? = null,
        serialNumber: String? = null,
        notes: String? = null
    ): Result<InventoryItem> {
        // Walidacja
        when {
            quantity < 0 -> return Result.failure(Exception("Quantity cannot be negative"))
            qrCode.isBlank() -> return Result.failure(Exception("QR code is required"))
            qrCode.length < 3 -> return Result.failure(Exception("QR code must be at least 3 characters"))
        }
        
        return try {
            val request = CreateInventoryRequest(
                productId = productId,
                locationId = locationId,
                quantity = quantity,
                qrCode = qrCode.trim(),
                status = status,
                lotNumber = lotNumber?.trim(),
                batchNumber = batchNumber?.trim(),
                serialNumber = serialNumber?.trim(),
                notes = notes?.trim()
            )
            
            val result = inventoryApiService.createInventory(request)
            result.fold(
                onSuccess = { inventoryItem ->
                    // Update cache
                    _inventoryState.value = _inventoryState.value + inventoryItem
                    Result.success(inventoryItem)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aktualizuj ilość
     */
    suspend fun updateQuantity(
        inventoryId: Long,
        newQuantity: Int,
        reason: String? = null
    ): Result<InventoryItem> {
        if (newQuantity < 0) {
            return Result.failure(Exception("Quantity cannot be negative"))
        }
        
        return try {
            val request = QuantityUpdateRequest(
                quantity = newQuantity,
                reason = reason?.trim()
            )
            
            val result = inventoryApiService.updateQuantity(inventoryId, request)
            result.fold(
                onSuccess = { inventoryItem ->
                    // Update cache
                    _inventoryState.value = _inventoryState.value.map {
                        if (it.id == inventoryId) inventoryItem else it
                    }
                    Result.success(inventoryItem)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aktualizuj pozycję inwentarza
     */
    suspend fun updateInventory(
        inventoryId: Long,
        locationId: Long? = null,
        quantity: Int? = null,
        status: InventoryStatus? = null,
        notes: String? = null
    ): Result<InventoryItem> {
        if (quantity != null && quantity < 0) {
            return Result.failure(Exception("Quantity cannot be negative"))
        }
        
        return try {
            val request = UpdateInventoryRequest(
                locationId = locationId,
                quantity = quantity,
                status = status,
                notes = notes?.trim()
            )
            
            val result = inventoryApiService.updateInventory(inventoryId, request)
            result.fold(
                onSuccess = { inventoryItem ->
                    // Update cache
                    _inventoryState.value = _inventoryState.value.map {
                        if (it.id == inventoryId) inventoryItem else it
                    }
                    Result.success(inventoryItem)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Usuń pozycję inwentarza
     */
    suspend fun deleteInventory(inventoryId: Long): Result<Unit> {
        return try {
            val result = inventoryApiService.deleteInventory(inventoryId)
            result.fold(
                onSuccess = {
                    // Remove from cache
                    _inventoryState.value = _inventoryState.value.filter { it.id != inventoryId }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Weryfikuj kod QR
     */
    suspend fun verifyQR(qrCode: String): Result<QRInventoryVerificationResult> {
        if (qrCode.isBlank()) {
            return Result.failure(Exception("QR code cannot be empty"))
        }
        
        return try {
            inventoryApiService.verifyQR(qrCode.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LOCATIONS MANAGEMENT ---

    /**
     * Pobierz wszystkie lokalizacje
     */
    suspend fun loadAllLocations(): Result<List<Location>> {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val result = inventoryApiService.getAllLocations()
            result.fold(
                onSuccess = { locations ->
                    _locationsState.value = locations
                    _isLoading.value = false
                    Result.success(locations)
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load locations: ${error.message}"
                    _isLoading.value = false
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _errorMessage.value = "Error loading locations: ${e.message}"
            _isLoading.value = false
            Result.failure(e)
        }
    }

    /**
     * Pobierz lokalizację po ID
     */
    suspend fun getLocationById(locationId: Long): Result<Location> {
        return try {
            inventoryApiService.getLocationById(locationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz lokalizacje w strefie
     */
    suspend fun getLocationsByZone(zoneId: Long): Result<List<Location>> {
        return try {
            inventoryApiService.getLocationsByZone(zoneId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyszukaj lokalizacje
     */
    suspend fun searchLocations(query: String): Result<List<Location>> {
        if (query.isBlank()) {
            return Result.success(_locationsState.value)
        }
        
        return try {
            inventoryApiService.searchLocations(query)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ZONES MANAGEMENT ---

    /**
     * Pobierz wszystkie strefy
     */
    suspend fun loadAllZones(): Result<List<Zone>> {
        _isLoading.value = true
        
        return try {
            val result = inventoryApiService.getAllZones()
            result.fold(
                onSuccess = { zones ->
                    _zonesState.value = zones
                    _isLoading.value = false
                    Result.success(zones)
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load zones: ${error.message}"
                    _isLoading.value = false
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _errorMessage.value = "Error loading zones: ${e.message}"
            _isLoading.value = false
            Result.failure(e)
        }
    }

    /**
     * Pobierz strefy według typu
     */
    suspend fun getZonesByType(type: ZoneType): Result<List<Zone>> {
        return try {
            inventoryApiService.getZonesByType(type)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- MOVEMENT HISTORY ---

    /**
     * Pobierz historię ruchów
     */
    suspend fun loadMovementHistory(
        page: Int = 0,
        size: Int = 20
    ): Result<PaginatedResponse<MovementHistoryDTO>> {
        _isLoading.value = true
        
        return try {
            val result = inventoryApiService.getMovementHistory(page, size)
            result.fold(
                onSuccess = { paginatedResponse ->
                    if (page == 0) {
                        _movementHistoryState.value = paginatedResponse.content
                    } else {
                        _movementHistoryState.value = _movementHistoryState.value + paginatedResponse.content
                    }
                    _isLoading.value = false
                    Result.success(paginatedResponse)
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load movement history: ${error.message}"
                    _isLoading.value = false
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _errorMessage.value = "Error loading movement history: ${e.message}"
            _isLoading.value = false
            Result.failure(e)
        }
    }

    /**
     * Pobierz historię ruchów dla produktu
     */
    suspend fun getMovementHistoryByProduct(productId: Long): Result<List<MovementHistoryDTO>> {
        return try {
            inventoryApiService.getMovementHistoryByProduct(productId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz historię ruchów według typu
     */
    suspend fun getMovementHistoryByType(type: MovementType): Result<List<MovementHistoryDTO>> {
        return try {
            inventoryApiService.getMovementHistoryByType(type)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyczyść error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Refresh wszystkich danych
     */
    suspend fun refreshData() {
        loadAllInventory(page = 0, size = 50)
        loadAllLocations()
        loadAllZones()
        loadMovementHistory(page = 0, size = 50)
    }
}