package com.qrware.app.data.repository

import com.qrware.app.data.api.ApprovalRequest
import com.qrware.app.data.api.MovementHistoryApiService
import com.qrware.app.data.dto.MovementHistoryDTO
import com.qrware.app.security.TokenManager
import com.qrware.app.data.model.MovementType

class MovementHistoryRepository(
    private val apiService: MovementHistoryApiService,
    private val tokenManager: TokenManager
) {

    private fun getAuthToken(): String = "Bearer ${tokenManager.getToken}"

    /**
     * Get movement history by inventory item ID
     */
    suspend fun getMovementHistoryByItemId(itemId: Long): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByItemId(getAuthToken(), itemId)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania historii ruchów: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movement history by product ID
     */
    suspend fun getMovementHistoryByProductId(productId: Long): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByProductId(getAuthToken(), productId)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania historii ruchów produktu: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movement history by location ID
     */
    suspend fun getMovementHistoryByLocationId(locationId: Long): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByLocationId(getAuthToken(), locationId)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania historii ruchów lokalizacji: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movement history by type
     */
    suspend fun getMovementHistoryByType(movementType: MovementType): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByType(getAuthToken(), movementType.name)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania historii ruchów typu: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movement history by date range
     */
    suspend fun getMovementHistoryByDateRange(startDate: String, endDate: String): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByDateRange(getAuthToken(), startDate, endDate)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania historii ruchów z zakresu dat: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movement history by user ID
     */
    suspend fun getMovementHistoryByUserId(userId: String): List<MovementHistoryDTO> {
        val response = apiService.getMovementHistoryByUserId(getAuthToken(), userId)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania historii ruchów użytkownika: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get recent movements
     */
    suspend fun getRecentMovements(limit: Int = 50): List<MovementHistoryDTO> {
        val response = apiService.getRecentMovements(getAuthToken(), limit)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania ostatnich ruchów: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get pending approval movements
     */
    suspend fun getPendingApprovalMovements(): List<MovementHistoryDTO> {
        val response = apiService.getPendingApprovalMovements(getAuthToken())
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania ruchów oczekujących na zatwierdzenie: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get inbound movements
     */
    suspend fun getInboundMovements(limit: Int? = null): List<MovementHistoryDTO> {
        val response = apiService.getInboundMovements(getAuthToken(), limit)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania ruchów przychodzących: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get outbound movements
     */
    suspend fun getOutboundMovements(limit: Int? = null): List<MovementHistoryDTO> {
        val response = apiService.getOutboundMovements(getAuthToken(), limit)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania ruchów wychodzących: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get adjustment movements
     */
    suspend fun getAdjustmentMovements(limit: Int? = null): List<MovementHistoryDTO> {
        val response = apiService.getAdjustmentMovements(getAuthToken(), limit)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania korekt: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movement statistics by type
     */
    suspend fun getMovementStatsByType(): Map<String, Any> {
        val response = apiService.getMovementStatsByType(getAuthToken())
        if (response.isSuccessful) {
            return response.body() ?: emptyMap()
        } else {
            throw Exception("Błąd podczas pobierania statystyk ruchów: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movement statistics by date
     */
    suspend fun getMovementStatsByDate(startDate: String, endDate: String): Map<String, Any> {
        val response = apiService.getMovementStatsByDate(getAuthToken(), startDate, endDate)
        if (response.isSuccessful) {
            return response.body() ?: emptyMap()
        } else {
            throw Exception("Błąd podczas pobierania statystyk ruchów: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Search movements
     */
    suspend fun searchMovements(keyword: String, searchIn: String = "reason"): List<MovementHistoryDTO> {
        val response = apiService.searchMovements(getAuthToken(), keyword, searchIn)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas wyszukiwania ruchów: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Approve movement
     */
    suspend fun approveMovement(movementId: Long, approverComment: String?): MovementHistoryDTO {
        val request = ApprovalRequest(approverComment)
        val response = apiService.approveMovement(getAuthToken(), movementId, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Pusta odpowiedź podczas zatwierdzania ruchu")
        } else {
            throw Exception("Błąd podczas zatwierdzania ruchu: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movement by ID
     */
    suspend fun getMovementById(movementId: Long): MovementHistoryDTO {
        val response = apiService.getMovementById(getAuthToken(), movementId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Nie znaleziono ruchu o ID: $movementId")
        } else {
            throw Exception("Błąd podczas pobierania szczegółów ruchu: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movements requiring attention
     */
    suspend fun getMovementsRequiringAttention(): List<MovementHistoryDTO> {
        val response = apiService.getMovementsRequiringAttention(getAuthToken())
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania ruchów wymagających uwagi: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get audit trail
     */
    suspend fun getAuditTrail(startDate: String, endDate: String): List<MovementHistoryDTO> {
        val response = apiService.getAuditTrail(getAuthToken(), startDate, endDate)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania ścieżki audytowej: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movement velocity
     */
    suspend fun getMovementVelocity(startDate: String, endDate: String): Double {
        val response = apiService.getMovementVelocity(getAuthToken(), startDate, endDate)
        if (response.isSuccessful) {
            return response.body() ?: 0.0
        } else {
            throw Exception("Błąd podczas pobierania prędkości ruchów: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movements with environmental data
     */
    suspend fun getMovementsWithEnvironmentalData(): List<MovementHistoryDTO> {
        val response = apiService.getMovementsWithEnvironmentalData(getAuthToken())
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania ruchów z danymi środowiskowymi: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movements by batch ID
     */
    suspend fun getMovementsByBatchId(batchId: String): List<MovementHistoryDTO> {
        val response = apiService.getMovementsByBatchId(getAuthToken(), batchId)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania ruchów partii: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Get movements by reference number
     */
    suspend fun getMovementsByReferenceNumber(referenceNumber: String): List<MovementHistoryDTO> {
        val response = apiService.getMovementsByReferenceNumber(getAuthToken(), referenceNumber)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Błąd podczas pobierania ruchów o numerze referencyjnym: ${response.errorBody()?.string()}")
        }
    }
}