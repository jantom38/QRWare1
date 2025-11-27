package com.qrware.app.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.dto.InventoryItemDTO
import com.qrware.app.data.model.*
import com.qrware.app.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageInventoryViewModel(
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

    fun searchInventory(query: String) {
        if (query.isBlank()) {
            loadInventoryItems(page = 0)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = inventoryRepository.searchInventory(query)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inventoryItems = response,
                    totalPages = 1,
                    currentPage = 0
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
                val response = inventoryRepository.getInventoryByStatus(status)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inventoryItems = response,
                    totalPages = 1,
                    currentPage = 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas filtrowania"
                )
            }
        }
    }

    fun receiveStock(itemId: Long, quantity: Int, reason: String?) {
        viewModelScope.launch {
            try {
                val request = QuantityUpdateRequest(quantity, reason)
                inventoryRepository.receiveStock(itemId, request)
                loadInventoryItems()
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

    fun issueStock(itemId: Long, quantity: Int, reason: String?) {
        viewModelScope.launch {
            try {
                val request = QuantityUpdateRequest(quantity, reason)
                inventoryRepository.issueStock(itemId, request)
                loadInventoryItems()
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
                loadInventoryItems()
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

data class InventoryUiState(
    val isLoading: Boolean = false,
    val inventoryItems: List<InventoryItemDTO> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0
)