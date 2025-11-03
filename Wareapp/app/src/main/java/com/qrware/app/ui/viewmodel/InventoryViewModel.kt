package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.*
import com.qrware.app.data.dto.InventoryItemDTO
import com.qrware.app.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadInventoryItems()
    }

    fun loadInventoryItems(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = inventoryRepository.getAllInventoryItems(page, size)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inventoryItems = response.content, 
                    totalPages = response.totalPages,
                    currentPage = page
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania"
                )
            }
        }
    }

    fun searchByProduct(productId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // ZMIANA: response to teraz List<InventoryItemDTO>
                val response = inventoryRepository.getInventoryByProduct(productId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inventoryItems = response // <-- ZMIANA (bez .data)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas wyszukiwania"
                )
            }
        }
    }

    fun searchByLocation(locationId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // ZMIANA: response to teraz List<InventoryItemDTO>
                val response = inventoryRepository.getInventoryByLocation(locationId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inventoryItems = response // <-- ZMIANA (bez .data)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas wyszukiwania"
                )
            }
        }
    }

    fun filterByStatus(status: InventoryStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // ZMIANA: response to teraz List<InventoryItemDTO>
                val response = inventoryRepository.getInventoryByStatus(status)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inventoryItems = response // <-- ZMIANA (bez .data)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas filtrowania"
                )
            }
        }
    }

    // Funkcje receive, issue, delete nie wymagają zmian w logice,
    // ponieważ po prostu wywołują loadInventoryItems(), która jest już poprawiona.

    fun receiveStock(itemId: Long, quantity: java.math.BigDecimal, reason: String?) {
        viewModelScope.launch {
            try {
                val request = QuantityUpdateRequest(quantity, reason)
                inventoryRepository.receiveStock(itemId, request) // Ta funkcja już nie rzuca błędu
                loadInventoryItems() // Odśwież listę
                _uiState.value = _uiState.value.copy(
                    successMessage = "Przyjęto towar pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas przyjmowania towaru"
                )
            }
        }
    }

    fun issueStock(itemId: Long, quantity: java.math.BigDecimal, reason: String?) {
        viewModelScope.launch {
            try {
                val request = QuantityUpdateRequest(quantity, reason)
                inventoryRepository.issueStock(itemId, request)
                loadInventoryItems() // Odśwież listę
                _uiState.value = _uiState.value.copy(
                    successMessage = "Wydano towar pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas wydawania towaru"
                )
            }
        }
    }

    fun deleteInventoryItem(itemId: Long) {
        viewModelScope.launch {
            try {
                inventoryRepository.deleteInventoryItem(itemId)
                loadInventoryItems() // Odśwież listę
                _uiState.value = _uiState.value.copy(
                    successMessage = "Pozycja usunięta pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas usuwania"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun nextPage() {
        if (_uiState.value.currentPage < _uiState.value.totalPages - 1) {
            loadInventoryItems(_uiState.value.currentPage + 1)
        }
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 0) {
            loadInventoryItems(_uiState.value.currentPage - 1)
        }
    }
}

// Ten plik (InventoryUiState) pozostaje bez zmian,
// ponieważ już używa InventoryItemDTO
data class InventoryUiState(
    val isLoading: Boolean = false,
    val inventoryItems: List<InventoryItemDTO> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0
)