package com.qrware.app.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.repository.ProductRepository

class ProductDetailsViewModelFactory(
    private val productRepository: ProductRepository,
    private val productId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailsViewModel::class.java)) {
            return ProductDetailsViewModel(productRepository, productId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}