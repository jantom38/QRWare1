package com.qrware.app.ui.viewmodel.OrderManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.repository.OrderItemRepository
import com.qrware.app.data.repository.OrderRepository

class OrderDetailsViewModelFactory(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderDetailsViewModel::class.java)) {
            return OrderDetailsViewModel(orderRepository, orderItemRepository, orderId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}