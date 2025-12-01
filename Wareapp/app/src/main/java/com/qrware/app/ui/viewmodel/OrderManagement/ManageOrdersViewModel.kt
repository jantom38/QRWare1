package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.OrderDTO
import com.qrware.app.data.model.OrderStatus
import com.qrware.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageOrdersUiState(
    val isLoading: Boolean = false,
    val allOrders: List<OrderDTO> = emptyList(), // Pełna lista pobrana z API
    val visibleOrders: List<OrderDTO> = emptyList(), // Lista po przefiltrowaniu (do wyświetlenia)
    val selectedFilter: OrderStatus? = null,
    val error: String? = null,
    val successMessage: String? = null
)

class ManageOrdersViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageOrdersUiState())
    val uiState: StateFlow<ManageOrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            orderRepository.getAllOrders()
                .onSuccess { pagedResponse ->
                    val orders = pagedResponse.content
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            allOrders = orders,
                            visibleOrders = applyFilter(orders, state.selectedFilter)
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Błąd pobierania zamówień")
                    }
                }
        }
    }

    fun setFilter(status: OrderStatus?) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = status,
                visibleOrders = applyFilter(state.allOrders, status)
            )
        }
    }

    private fun applyFilter(orders: List<OrderDTO>, filter: OrderStatus?): List<OrderDTO> {
        return if (filter == null) {
            orders
        } else {
            orders.filter { it.status == filter }
        }
    }

    // --- AKCJE NA ZAMÓWIENIACH ---

    fun startOrder(orderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            orderRepository.startOrder(orderId)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.update { it.copy(isLoading = false, successMessage = "Rozpoczęto zamówienie") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun completeOrder(orderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            orderRepository.completeOrder(orderId)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.update { it.copy(isLoading = false, successMessage = "Zakończono zamówienie") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Zakładamy, że powód jest wymagany, tutaj wpisujemy domyślny lub można rozbudować UI o dialog
            orderRepository.cancelOrder(orderId, reason = "Anulowane przez administratora")
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.update { it.copy(isLoading = false, successMessage = "Anulowano zamówienie") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    // Pomocnicza funkcja do aktualizacji pojedynczego elementu na liście bez przeładowywania całości
    private fun updateOrderInList(updatedOrder: OrderDTO) {
        _uiState.update { state ->
            val newAllOrders = state.allOrders.map {
                if (it.id == updatedOrder.id) updatedOrder else it
            }
            state.copy(
                allOrders = newAllOrders,
                visibleOrders = applyFilter(newAllOrders, state.selectedFilter)
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}