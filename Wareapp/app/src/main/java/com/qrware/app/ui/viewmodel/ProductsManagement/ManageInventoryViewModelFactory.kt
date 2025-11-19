package com.qrware.app.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.repository.InventoryRepository

class ManageInventoryViewModelFactory(
    private val inventoryRepository: InventoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageInventoryViewModel::class.java)) {
            return ManageInventoryViewModel(inventoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}