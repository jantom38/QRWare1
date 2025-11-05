package com.qrware.app.ui.viewmodel

import android.util.Log // <-- DODAJ TEN IMPORT
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.data.model.UpdateProductRequest
import com.qrware.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class EditProductUiState(
    val isLoading: Boolean = true,
    val product: ProductDTO? = null, // Oryginalny produkt do porównania
    val error: String? = null,
    val updateSuccess: Boolean = false,

    // Pola formularza
    val name: String = "",
    val sku: String = "",
    val description: String = "",
    val price: String = "",
    val categoryId: Long? = null,
    val active: Boolean = false // (Domyślna wartość jest OK)
)

class EditProductViewModel(
    private val productRepository: ProductRepository,
    private val productId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProductUiState())
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    init {
        loadProductDetails()
    }

    fun loadProductDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val product = productRepository.getProductById(productId)

                // --- DODANY LOG DIAGNOSTYCZNY ---
                Log.d("EditProductVM", "Produkt załadowany. ID: ${product.id}, Nazwa: ${product.name}, Otrzymano Active: ${product.active}")
                // --- KONIEC LOGU ---

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        product = product, // Zapisujemy oryginał
                        // Wypełniamy formularz
                        name = product.name,
                        sku = product.sku,
                        description = product.description ?: "",
                        price = product.price?.toPlainString() ?: "",
                        categoryId = product.category?.id,
                        active = product.active // Ustawienie wartości ze 100% pewnością
                    )
                }
            } catch (e: Exception) {
                // --- DODANY LOG BŁĘDU ---
                Log.e("EditProductVM", "Błąd ładowania produktu", e)
                // --- KONIEC LOGU ---
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Błąd ładowania produktu: ${e.message}"
                    )
                }
            }
        }
    }

    // Funkcje do aktualizacji pól formularza
    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }
    fun onSkuChange(newSku: String) {
        _uiState.update { it.copy(sku = newSku) }
    }
    fun onDescriptionChange(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }
    fun onPriceChange(newPrice: String) {
        _uiState.update { it.copy(price = newPrice) }
    }

    // --- NOWA FUNKCJA OBSŁUGUJĄCA ZMIANĘ 'ACTIVE' ---
    fun onActiveChange(newStatus: Boolean) {
        // --- DODANY LOG DIAGNOSTYCZNY ---
        Log.d("EditProductVM", "Switch kliknięty. Nowy status w UI: $newStatus")
        // --- KONIEC LOGU ---
        _uiState.update { it.copy(active = newStatus) }
    }

    fun updateProduct() {
        val currentState = _uiState.value
        val originalProduct = currentState.product

        if (originalProduct == null) {
            _uiState.update { it.copy(error = "Błąd: Brak oryginalnego produktu do porównania.") }
            return
        }

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Nazwa jest wymagana.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val priceDecimal = currentState.price.toBigDecimalOrNull()
            val originalPrice = originalProduct.price
            val originalDesc = originalProduct.description ?: ""
            val currentDesc = currentState.description

            // Budujemy żądanie tylko ze zmienionymi polami
            val request = UpdateProductRequest(
                name = currentState.name.takeIf { it != originalProduct.name },
                description = currentDesc.takeIf { it != originalDesc },
                price = priceDecimal.takeIf {
                    it == null && originalPrice != null ||
                            it != null && originalPrice == null ||
                            (it != null && originalPrice != null && it.compareTo(originalPrice) != 0)
                },
                categoryId = currentState.categoryId.takeIf { it != originalProduct.category?.id },
                active = currentState.active.takeIf { it != originalProduct.active }, // <-- DODANA LINIA

                // Pozostałe pola z UpdateProductRequest ustawione na null
                unit = null,
                weight = null,
                length = null,
                width = null,
                height = null
            )

            // Sprawdź, czy cokolwiek się zmieniło
            if (request.name == null && request.description == null &&
                request.price == null && request.categoryId == null &&
                request.active == null) { // <-- DODANY WARUNEK

                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Nie wprowadzono żadnych zmian.")
                }
                return@launch
            }

            try {
                // Zakładamy, że repozytorium przyjmuje UpdateProductRequest z pakietu .model
                productRepository.updateProduct(productId, request)
                _uiState.update { it.copy(isLoading = false, updateSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Błąd aktualizacji: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}