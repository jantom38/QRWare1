package com.qrware.app.ui.viewmodel.OrderManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.repository.InventoryRepository
import com.qrware.app.data.repository.OrderItemRepository
import com.qrware.app.data.repository.OrderRepository
import com.qrware.app.data.repository.ProductRepository

class OrderDetailsViewModelFactory(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val orderId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderDetailsViewModel::class.java)) {
            return OrderDetailsViewModel(
                orderRepository,
                orderItemRepository,
                inventoryRepository,
                productRepository,
                orderId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}