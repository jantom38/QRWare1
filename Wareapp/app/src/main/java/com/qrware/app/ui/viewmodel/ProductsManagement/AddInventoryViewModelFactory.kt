package com.qrware.app.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.repository.InventoryRepository
import com.qrware.app.data.repository.LocationRepository
import com.qrware.app.data.repository.ProductRepository

class AddInventoryViewModelFactory(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddInventoryViewModel::class.java)) {
            return AddInventoryViewModel(inventoryRepository, productRepository, locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}