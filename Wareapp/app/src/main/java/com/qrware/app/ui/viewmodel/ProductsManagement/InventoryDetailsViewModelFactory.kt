package com.qrware.app.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.repository.InventoryRepository

class InventoryDetailsViewModelFactory(
    private val inventoryRepository: InventoryRepository,
    private val itemId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryDetailsViewModel(inventoryRepository, itemId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}