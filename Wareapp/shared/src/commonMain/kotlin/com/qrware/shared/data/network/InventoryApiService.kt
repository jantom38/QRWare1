package com.qrware.shared.data.network

import com.qrware.shared.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Inventory API Service - zarządzanie stanami magazynowymi
 * Migracja z Android ApiService na KMP
 */
class InventoryApiService(private val httpClient: HttpClient) {
    
    companion object {
        private const val INVENTORY_BASE = "/api/inventory"
        private const val LOCATIONS_BASE = "/api/locations"
        private const val ZONES_BASE = "/api/zones"
        private const val MOVEMENT_BASE = "/api/movement-history"
    }

    // --- INVENTORY MANAGEMENT ---

    /**
     * Pobierz wszystkie pozycje inwentarza z paginacją
     */
    suspend fun getAllInventory(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,desc"
    ): Result<PaginatedResponse<InventoryItem>> {
        return try {
            val response = httpClient.get(INVENTORY_BASE) {
                parameter("page", page)
                parameter("size", size)
                parameter("sort", sort)
            }
            Result.success(response.body<PaginatedResponse<InventoryItem>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz pozycję inwentarza po ID
     */
    suspend fun getInventoryById(inventoryId: Long): Result<InventoryItem> {
        return try {
            val response = httpClient.get("$INVENTORY_BASE/$inventoryId")
            Result.success(response.body<InventoryItem>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz inwentarz po kodzie QR
     */
    suspend fun getInventoryByQR(qrCode: String): Result<InventoryItem> {
        return try {
            val response = httpClient.get("$INVENTORY_BASE/qr/$qrCode")
            Result.success(response.body<InventoryItem>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz inwentarz dla produktu
     */
    suspend fun getInventoryByProduct(productId: Long): Result<List<InventoryItem>> {
        return try {
            val response = httpClient.get("$INVENTORY_BASE/product/$productId")
            Result.success(response.body<List<InventoryItem>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz inwentarz dla lokalizacji
     */
    suspend fun getInventoryByLocation(locationId: Long): Result<List<InventoryItem>> {
        return try {
            val response = httpClient.get("$INVENTORY_BASE/location/$locationId")
            Result.success(response.body<List<InventoryItem>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz inwentarz według statusu
     */
    suspend fun getInventoryByStatus(status: InventoryStatus): Result<List<InventoryItem>> {
        return try {
            val response = httpClient.get("$INVENTORY_BASE/status/$status")
            Result.success(response.body<List<InventoryItem>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyszukaj inwentarz
     */
    suspend fun searchInventory(query: String): Result<List<InventoryItem>> {
        return try {
            val response = httpClient.get("$INVENTORY_BASE/search") {
                parameter("query", query)
            }
            Result.success(response.body<List<InventoryItem>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stwórz nową pozycję inwentarza
     */
    suspend fun createInventory(request: CreateInventoryRequest): Result<InventoryItem> {
        return try {
            val response = httpClient.post(INVENTORY_BASE) {
                setBody(request)
            }
            Result.success(response.body<InventoryItem>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aktualizuj pozycję inwentarza
     */
    suspend fun updateInventory(inventoryId: Long, request: UpdateInventoryRequest): Result<InventoryItem> {
        return try {
            val response = httpClient.put("$INVENTORY_BASE/$inventoryId") {
                setBody(request)
            }
            Result.success(response.body<InventoryItem>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aktualizuj ilość
     */
    suspend fun updateQuantity(inventoryId: Long, request: QuantityUpdateRequest): Result<InventoryItem> {
        return try {
            val response = httpClient.post("$INVENTORY_BASE/$inventoryId/quantity") {
                setBody(request)
            }
            Result.success(response.body<InventoryItem>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Usuń pozycję inwentarza
     */
    suspend fun deleteInventory(inventoryId: Long): Result<Unit> {
        return try {
            httpClient.delete("$INVENTORY_BASE/$inventoryId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Weryfikacja kodu QR
     */
    suspend fun verifyQR(qrCode: String): Result<QRInventoryVerificationResult> {
        return try {
            val response = httpClient.get("$INVENTORY_BASE/verify/$qrCode")
            Result.success(response.body<QRInventoryVerificationResult>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LOCATIONS MANAGEMENT ---

    /**
     * Pobierz wszystkie lokalizacje
     */
    suspend fun getAllLocations(): Result<List<Location>> {
        return try {
            val response = httpClient.get(LOCATIONS_BASE)
            Result.success(response.body<List<Location>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz lokalizację po ID
     */
    suspend fun getLocationById(locationId: Long): Result<Location> {
        return try {
            val response = httpClient.get("$LOCATIONS_BASE/$locationId")
            Result.success(response.body<Location>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz lokalizacje w strefie
     */
    suspend fun getLocationsByZone(zoneId: Long): Result<List<Location>> {
        return try {
            val response = httpClient.get("$LOCATIONS_BASE/zone/$zoneId")
            Result.success(response.body<List<Location>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyszukaj lokalizacje
     */
    suspend fun searchLocations(query: String): Result<List<Location>> {
        return try {
            val response = httpClient.get("$LOCATIONS_BASE/search") {
                parameter("query", query)
            }
            Result.success(response.body<List<Location>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ZONES MANAGEMENT ---

    /**
     * Pobierz wszystkie strefy
     */
    suspend fun getAllZones(): Result<List<Zone>> {
        return try {
            val response = httpClient.get(ZONES_BASE)
            Result.success(response.body<List<Zone>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz strefę po ID
     */
    suspend fun getZoneById(zoneId: Long): Result<Zone> {
        return try {
            val response = httpClient.get("$ZONES_BASE/$zoneId")
            Result.success(response.body<Zone>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz strefy według typu
     */
    suspend fun getZonesByType(type: ZoneType): Result<List<Zone>> {
        return try {
            val response = httpClient.get("$ZONES_BASE/type/$type")
            Result.success(response.body<List<Zone>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- MOVEMENT HISTORY ---

    /**
     * Pobierz historię ruchów z paginacją
     */
    suspend fun getMovementHistory(
        page: Int = 0,
        size: Int = 20,
        sort: String = "timestamp,desc"
    ): Result<PaginatedResponse<MovementHistoryDTO>> {
        return try {
            val response = httpClient.get(MOVEMENT_BASE) {
                parameter("page", page)
                parameter("size", size)
                parameter("sort", sort)
            }
            Result.success(response.body<PaginatedResponse<MovementHistoryDTO>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz historię ruchów dla produktu
     */
    suspend fun getMovementHistoryByProduct(productId: Long): Result<List<MovementHistoryDTO>> {
        return try {
            val response = httpClient.get("$MOVEMENT_BASE/product/$productId")
            Result.success(response.body<List<MovementHistoryDTO>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz historię ruchów dla pozycji inwentarza
     */
    suspend fun getMovementHistoryByInventory(inventoryId: Long): Result<List<MovementHistoryDTO>> {
        return try {
            val response = httpClient.get("$MOVEMENT_BASE/inventory/$inventoryId")
            Result.success(response.body<List<MovementHistoryDTO>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz historię ruchów według typu
     */
    suspend fun getMovementHistoryByType(type: MovementType): Result<List<MovementHistoryDTO>> {
        return try {
            val response = httpClient.get("$MOVEMENT_BASE/type/$type")
            Result.success(response.body<List<MovementHistoryDTO>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}