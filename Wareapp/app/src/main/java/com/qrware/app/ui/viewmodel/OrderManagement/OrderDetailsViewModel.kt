package com.qrware.app.ui.viewmodel.OrderManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.dto.InventoryItemDTO
import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.data.model.CompleteOrderItemRequest
import com.qrware.app.data.model.CreateOrderItemRequest
import com.qrware.app.data.model.OrderDTO
import com.qrware.app.data.model.OrderItemDTO
import com.qrware.app.data.model.OrderPriority
import com.qrware.app.data.model.UpdateOrderRequest
import com.qrware.app.ui.screens.OrderManagement.OrderItemRequest
import com.qrware.app.data.repository.InventoryRepository
import com.qrware.app.data.repository.OrderItemRepository
import com.qrware.app.data.repository.OrderRepository
import com.qrware.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailsUiState(
    val isLoading: Boolean = true,
    val order: OrderDTO? = null,
    val error: String? = null,
    val isOperationProcessing: Boolean = false,
    val availableInventory: List<InventoryItemDTO> = emptyList(),
    val isInventoryLoading: Boolean = false,
    val productSearchResults: List<ProductDTO> = emptyList(),
    val isProductSearchLoading: Boolean = false
)

class OrderDetailsViewModel(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
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

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(productSearchResults = emptyList()) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProductSearchLoading = true) }
            try {
                val products = productRepository.searchProducts(query)
                _uiState.update { 
                    it.copy(
                        isProductSearchLoading = false,
                        productSearchResults = products
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isProductSearchLoading = false,
                        error = "Błąd wyszukiwania: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun loadInventoryForProduct(productId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInventoryLoading = true, availableInventory = emptyList()) }
            
            try {
                val inventoryList = inventoryRepository.getInventoryByProduct(productId)
                val availableItems = inventoryList.filter { it.availableQuantity > 0 }
                
                _uiState.update { 
                    it.copy(
                        isInventoryLoading = false, 
                        availableInventory = availableItems
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isInventoryLoading = false, 
                        error = "Błąd pobierania stanów: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun clearInventoryList() {
        _uiState.update { it.copy(availableInventory = emptyList()) }
    }

    fun clearProductSearchResults() {
        _uiState.update { it.copy(productSearchResults = emptyList()) }
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
                sourceLocationId = orderItemRequest.sourceLocationId,
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

    fun deleteOrderItem(itemId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationProcessing = true) }
            orderItemRepository.deleteOrderItem(itemId)
                .onSuccess {
                    loadOrderDetails()
                    _uiState.update { it.copy(isOperationProcessing = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isOperationProcessing = false, error = "Błąd usuwania pozycji: ${e.message}") }
                }
        }
    }

    fun updateOrder(description: String, priority: OrderPriority, expectedDate: String, notes: String, externalReference: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationProcessing = true) }
            
            val request = UpdateOrderRequest(
                description = description,
                priority = priority,
                expectedDate = expectedDate,
                notes = notes,
                externalReference = externalReference
            )
            
            orderRepository.updateOrder(orderId, request)
                .onSuccess { updatedOrder ->
                    _uiState.update { it.copy(isOperationProcessing = false, order = updatedOrder) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isOperationProcessing = false, error = "Błąd aktualizacji: ${e.message}") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}