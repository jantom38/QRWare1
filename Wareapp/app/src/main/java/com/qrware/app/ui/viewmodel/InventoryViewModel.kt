package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    // ZMIANA: Domyślnie filtrujemy po 'Aktywne' (true)
    private val _uiState = MutableStateFlow(ProductListUiState(activeFilter = true))
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        // Ładujemy produkty z domyślnym filtrem
        loadProducts(active = _uiState.value.activeFilter)
    }

    // ZMIANA: Funkcja ładująca produkty przyjmuje i przekazuje filtr
    fun loadProducts(
        page: Int = 0,
        size: Int = 20,
        active: Boolean? = _uiState.value.activeFilter // Pobierz bieżący filtr
    ) {
        viewModelScope.launch {
            // ZMIANA: Zapisz stan filtra przy ładowaniu
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, activeFilter = active)
            try {
                // ZMIANA: Przekazujemy filtr 'active' do repozytorium
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

    // --- NOWA FUNKCJA DO FILTROWANIA ---
    /**
     * Wywoływana po kliknięciu kafelka filtra.
     * Ładuje pierwszą stronę przefiltrowanych danych.
     * 'null' oznacza "Wszystkie"
     */
    fun filterByActiveStatus(active: Boolean?) {
        // Załaduj od nowa tylko jeśli filtr się zmienił
        if (active != _uiState.value.activeFilter) {
            loadProducts(page = 0, active = active)
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(productId)
                // ZMIANA: Odśwież listę z bieżącym filtrem
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
            // ZMIANA: Przekaż filtr
            loadProducts(
                page = _uiState.value.currentPage + 1,
                active = _uiState.value.activeFilter
            )
        }
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 0) {
            // ZMIANA: Przekaż filtr
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
    val activeFilter: Boolean? = true // <-- DODANE POLE STANU (domyślnie true)
)