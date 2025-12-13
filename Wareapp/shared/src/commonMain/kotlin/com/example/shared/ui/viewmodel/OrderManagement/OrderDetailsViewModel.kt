package com.example.shared.ui.viewmodel.OrderManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.model.CompleteOrderItemRequest
import com.example.shared.data.model.CreateOrderItemRequest
import com.example.shared.data.model.OrderDTO
import com.example.shared.data.model.OrderItemDTO
import com.example.shared.data.repository.OrderItemRepository
import com.example.shared.data.repository.OrderRepository
// Zakładamy, że ta klasa jest zdefiniowana w ekranie lub wspólnym modelu.
// Jeśli jest w OrderDetailsScreen.kt, ten import jest poprawny.
import com.example.shared.ui.screens.OrderManagement.OrderItemRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailsUiState(
    val isLoading: Boolean = true,
    val order: OrderDTO? = null,
    val error: String? = null,
    val isOperationProcessing: Boolean = false
)

class OrderDetailsViewModel(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailsUiState())
    val uiState: StateFlow<OrderDetailsUiState> = _uiState.asStateFlow()

    init {
        loadOrderDetails()
    }

    fun loadOrderDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            orderRepository.getOrderById(orderId)
                .onSuccess { order ->
                    _uiState.update { it.copy(isLoading = false, order = order) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Nie znaleziono zamówienia")
                    }
                }
        }
    }

    fun startOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationProcessing = true) }
            orderRepository.startOrder(orderId)
                .onSuccess { updatedOrder ->
                    _uiState.update { it.copy(isOperationProcessing = false, order = updatedOrder) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isOperationProcessing = false, error = e.message) }
                }
        }
    }

    fun completeOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationProcessing = true) }
            orderRepository.completeOrder(orderId)
                .onSuccess { updatedOrder ->
                    _uiState.update { it.copy(isOperationProcessing = false, order = updatedOrder) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isOperationProcessing = false, error = e.message) }
                }
        }
    }

    fun completeOrderItem(item: OrderItemDTO, quantity: Int, notes: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationProcessing = true) }

            val request = CompleteOrderItemRequest(
                completedQuantity = quantity,
                completionNotes = notes,
                qrCodeData = item.qrCodeData
            )

            orderItemRepository.completeOrderItem(item.id, request)
                .onSuccess {
                    loadOrderDetails()
                    _uiState.update { it.copy(isOperationProcessing = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isOperationProcessing = false, error = "Błąd pozycji: ${e.message}") }
                }
        }
    }

    fun addOrderItem(orderItemRequest: OrderItemRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationProcessing = true) }

            val createRequest = CreateOrderItemRequest(
                productId = orderItemRequest.productId,
                requestedQuantity = orderItemRequest.requestedQuantity,
                sourceLocationId = null,
                destinationLocationId = null,
                unitPrice = null,
                notes = orderItemRequest.notes,
                requiresExactInventory = orderItemRequest.requiresExactInventory
            )

            orderItemRepository.addOrderItem(orderId, createRequest)
                .onSuccess {
                    loadOrderDetails()
                    _uiState.update { it.copy(isOperationProcessing = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isOperationProcessing = false, error = "Błąd dodawania pozycji: ${e.message}") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}