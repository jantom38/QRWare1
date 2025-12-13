package com.example.shared.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.ProductDTO
import com.example.shared.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageProductsViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState(activeFilter = true))
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        loadProducts(active = _uiState.value.activeFilter)
    }

    fun loadProducts(
        page: Int = 0,
        size: Int = 20,
        active: Boolean? = _uiState.value.activeFilter
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, activeFilter = active)
            try {
                val response = productRepository.getAllProducts(page, size, "id,asc", active)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = response.content,
                    totalPages = response.totalPages,
                    currentPage = page
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania"
                )
            }
        }
    }

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            loadProducts(page = 0, active = _uiState.value.activeFilter)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val results = productRepository.searchProducts(query)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = results,
                    totalPages = 1,
                    currentPage = 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas wyszukiwania"
                )
            }
        }
    }

    fun filterByActiveStatus(active: Boolean?) {
        if (active != _uiState.value.activeFilter) {
            loadProducts(page = 0, active = active)
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(productId)
                loadProducts(
                    page = _uiState.value.currentPage,
                    active = _uiState.value.activeFilter
                )
                _uiState.value = _uiState.value.copy(
                    successMessage = "Produkt usunięty pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas usuwania produktu"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun nextPage() {
        if (_uiState.value.currentPage < _uiState.value.totalPages - 1) {
            loadProducts(
                page = _uiState.value.currentPage + 1,
                active = _uiState.value.activeFilter
            )
        }
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 0) {
            loadProducts(
                page = _uiState.value.currentPage - 1,
                active = _uiState.value.activeFilter
            )
        }
    }
}

data class ProductListUiState(
    val isLoading: Boolean = false,
    val products: List<ProductDTO> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val activeFilter: Boolean? = true
)