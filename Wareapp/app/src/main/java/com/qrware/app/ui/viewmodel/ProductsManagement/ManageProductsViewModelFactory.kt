package com.qrware.app.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
// ZMIANA: Importujemy ProductRepository
import com.qrware.app.data.repository.ProductRepository

class ManageProductsViewModelFactory(
    // ZMIANA: Wymagamy ProductRepository
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageProductsViewModel::class.java)) {
            // ZMIANA: Przekazujemy productRepository
            return ManageProductsViewModel(productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}