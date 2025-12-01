package com.qrware.app.ui.viewmodel.OrderManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.OrderDTO
import com.qrware.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyOrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<OrderDTO> = emptyList(),
    val error: String? = null
)

class MyOrdersViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyOrdersUiState())
    val uiState: StateFlow<MyOrdersUiState> = _uiState.asStateFlow()

    init {
        loadMyOrders()
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            orderRepository.getMyOrders()
                .onSuccess { orders ->
                    _uiState.update {
                        it.copy(isLoading = false, orders = orders)
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Błąd pobierania zamówień")
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}