package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.CreateProductRequest
import com.qrware.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

// Stan UI dla ekranu dodawania produktu
data class AddProductUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AddProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun createProduct(
        sku: String,
        name: String,
        description: String?,
        price: BigDecimal?,
        categoryId: Long?
    ) {
        viewModelScope.launch {
            _uiState.value = AddProductUiState(isLoading = true)
            try {
                // Tworzymy obiekt żądania
                val request = CreateProductRequest(
                    sku = sku,
                    name = name,
                    description = description,
                    price = price,
                    categoryId = categoryId
                    // Możesz dodać więcej pól, jeśli formularz je obsługuje
                )

                // Wywołujemy repozytorium
                val newProduct = productRepository.createProduct(request)

                _uiState.value = AddProductUiState(
                    successMessage = "Produkt ${newProduct.name} został utworzony!"
                )
            } catch (e: Exception) {
                _uiState.value = AddProductUiState(
                    error = e.message ?: "Wystąpił nieznany błąd"
                )
            }
        }
    }

    // Funkcja do czyszczenia komunikatów po ich wyświetleniu
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}