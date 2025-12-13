package com.example.shared.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.InventoryItemDTO
import com.example.shared.data.model.QuantityUpdateRequest
import com.example.shared.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryDetailsViewModel(
    private val inventoryRepository: InventoryRepository,
    private val itemId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryDetailsUiState())
    val uiState: StateFlow<InventoryDetailsUiState> = _uiState.asStateFlow()

    init {
        loadInventoryDetails()
    }

    private fun loadInventoryDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val inventoryItem = inventoryRepository.getInventoryItemById(itemId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inventoryItem = inventoryItem
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania szczegółów pozycji"
                )
            }
        }
    }

    fun receiveStock(quantity: Int, reason: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            try {
                val request = QuantityUpdateRequest(quantity, reason)
                val updatedItem = inventoryRepository.receiveStock(itemId, request)
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    inventoryItem = updatedItem,
                    successMessage = "Przyjęto $quantity sztuk pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = e.message ?: "Błąd podczas przyjmowania towaru"
                )
            }
        }
    }

    fun issueStock(quantity: Int, reason: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            try {
                val request = QuantityUpdateRequest(quantity, reason)
                val updatedItem = inventoryRepository.issueStock(itemId, request)
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    inventoryItem = updatedItem,
                    successMessage = "Wydano $quantity sztuk pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = e.message ?: "Błąd podczas wydawania towaru"
                )
            }
        }
    }

    fun refreshData() {
        loadInventoryDetails()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}

data class InventoryDetailsUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val inventoryItem: InventoryItemDTO? = null,
    val error: String? = null,
    val successMessage: String? = null
)