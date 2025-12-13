package com.example.shared.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.ProductDTO
import com.example.shared.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailsUiState(
    val isLoading: Boolean = true,
    val product: ProductDTO? = null,
    val error: String? = null
)

class ProductDetailsViewModel(
    private val productRepository: ProductRepository,
    private val productId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProductDetails()
    }

    fun loadProductDetails() {
        viewModelScope.launch {
            _uiState.value = ProductDetailsUiState(isLoading = true)
            try {
                val product = productRepository.getProductById(productId)
                _uiState.value = ProductDetailsUiState(
                    isLoading = false,
                    product = product
                )
            } catch (e: Exception) {
                _uiState.value = ProductDetailsUiState(
                    isLoading = false,
                    error = "Nie udało się pobrać szczegółów produktu: ${e.message}"
                )
            }
        }
    }
}