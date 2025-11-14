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
    val cost: String = "",
    val weight: String = "",
    val length: String = "",
    val width: String = "",
    val height: String = "",
    val unit: String = "PIECE",
    val minimumStock: String = "",
    val maximumStock: String = "",
    val reorderPoint: String = "",
    val manufacturer: String = "",
    val supplier: String = "",
    val storageConditions: String = "",
    val barcode: String = "",
    val categoryId: Long? = null,
    val active: Boolean = false,
    val perishable: Boolean = false,
    val hazardous: Boolean = false,
    val fragile: Boolean = false
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
                        cost = product.cost?.toPlainString() ?: "",
                        weight = product.weight?.toPlainString() ?: "",
                        length = product.dimensionsLength?.toPlainString() ?: "",
                        width = product.dimensionsWidth?.toPlainString() ?: "",
                        height = product.dimensionsHeight?.toPlainString() ?: "",
                        unit = product.unitOfMeasure ?: "PIECE",
                        minimumStock = product.minimumStock?.toString() ?: "",
                        maximumStock = product.maximumStock?.toString() ?: "",
                        reorderPoint = product.reorderPoint?.toString() ?: "",
                        manufacturer = product.manufacturer ?: "",
                        supplier = product.supplier ?: "",
                        storageConditions = product.storageConditions ?: "",
                        barcode = product.barcode ?: "",
                        categoryId = product.category?.id,
                        active = product.active,
                        perishable = product.perishable ?: false,
                        hazardous = product.hazardous ?: false,
                        fragile = product.fragile ?: false
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
    fun onCostChange(newCost: String) {
        _uiState.update { it.copy(cost = newCost) }
    }
    fun onWeightChange(newWeight: String) {
        _uiState.update { it.copy(weight = newWeight) }
    }
    fun onLengthChange(newLength: String) {
        _uiState.update { it.copy(length = newLength) }
    }
    fun onWidthChange(newWidth: String) {
        _uiState.update { it.copy(width = newWidth) }
    }
    fun onHeightChange(newHeight: String) {
        _uiState.update { it.copy(height = newHeight) }
    }
    fun onUnitChange(newUnit: String) {
        _uiState.update { it.copy(unit = newUnit) }
    }
    fun onMinimumStockChange(newMinStock: String) {
        _uiState.update { it.copy(minimumStock = newMinStock) }
    }
    fun onMaximumStockChange(newMaxStock: String) {
        _uiState.update { it.copy(maximumStock = newMaxStock) }
    }
    fun onReorderPointChange(newReorderPoint: String) {
        _uiState.update { it.copy(reorderPoint = newReorderPoint) }
    }
    fun onManufacturerChange(newManufacturer: String) {
        _uiState.update { it.copy(manufacturer = newManufacturer) }
    }
    fun onSupplierChange(newSupplier: String) {
        _uiState.update { it.copy(supplier = newSupplier) }
    }
    fun onStorageConditionsChange(newStorageConditions: String) {
        _uiState.update { it.copy(storageConditions = newStorageConditions) }
    }
    fun onBarcodeChange(newBarcode: String) {
        _uiState.update { it.copy(barcode = newBarcode) }
    }
    fun onActiveChange(newStatus: Boolean) {
        Log.d("EditProductVM", "Switch kliknięty. Nowy status w UI: $newStatus")
        _uiState.update { it.copy(active = newStatus) }
    }
    fun onPerishableChange(newStatus: Boolean) {
        _uiState.update { it.copy(perishable = newStatus) }
    }
    fun onHazardousChange(newStatus: Boolean) {
        _uiState.update { it.copy(hazardous = newStatus) }
    }
    fun onFragileChange(newStatus: Boolean) {
        _uiState.update { it.copy(fragile = newStatus) }
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

            // Przygotowujemy wartości
            val priceDecimal = currentState.price.toBigDecimalOrNull()
            val costDecimal = currentState.cost.toBigDecimalOrNull()
            val weightDecimal = currentState.weight.toBigDecimalOrNull()
            val lengthDecimal = currentState.length.toBigDecimalOrNull()
            val widthDecimal = currentState.width.toBigDecimalOrNull()
            val heightDecimal = currentState.height.toBigDecimalOrNull()
            val minStockInt = currentState.minimumStock.toIntOrNull()
            val maxStockInt = currentState.maximumStock.toIntOrNull()
            val reorderPointInt = currentState.reorderPoint.toIntOrNull()

            // Budujemy żądanie tylko ze zmienionymi polami
            val request = UpdateProductRequest(
                name = currentState.name.takeIf { it != originalProduct.name },
                description = currentState.description.takeIf { it != (originalProduct.description ?: "") },
                price = priceDecimal.takeIf { it != originalProduct.price },
                cost = costDecimal.takeIf { it != originalProduct.cost },
                unit = currentState.unit.takeIf { it != (originalProduct.unitOfMeasure ?: "PIECE") },
                weight = weightDecimal.takeIf { it != originalProduct.weight },
                length = lengthDecimal.takeIf { it != originalProduct.dimensionsLength },
                width = widthDecimal.takeIf { it != originalProduct.dimensionsWidth },
                height = heightDecimal.takeIf { it != originalProduct.dimensionsHeight },
                minimumStock = minStockInt.takeIf { it != originalProduct.minimumStock },
                maximumStock = maxStockInt.takeIf { it != originalProduct.maximumStock },
                reorderPoint = reorderPointInt.takeIf { it != originalProduct.reorderPoint },
                manufacturer = currentState.manufacturer.takeIf { it != (originalProduct.manufacturer ?: "") },
                supplier = currentState.supplier.takeIf { it != (originalProduct.supplier ?: "") },
                storageConditions = currentState.storageConditions.takeIf { it != (originalProduct.storageConditions ?: "") },
                barcode = currentState.barcode.takeIf { it != (originalProduct.barcode ?: "") },
                categoryId = currentState.categoryId.takeIf { it != originalProduct.category?.id },
                active = currentState.active.takeIf { it != originalProduct.active },
                perishable = currentState.perishable.takeIf { it != (originalProduct.perishable ?: false) },
                hazardous = currentState.hazardous.takeIf { it != (originalProduct.hazardous ?: false) },
                fragile = currentState.fragile.takeIf { it != (originalProduct.fragile ?: false) }
            )

            // Sprawdź, czy cokolwiek się zmieniło
            if (request.name == null && request.description == null && request.price == null && 
                request.cost == null && request.unit == null && request.weight == null &&
                request.length == null && request.width == null && request.height == null &&
                request.minimumStock == null && request.maximumStock == null && 
                request.reorderPoint == null && request.manufacturer == null && 
                request.supplier == null && request.storageConditions == null && 
                request.barcode == null && request.categoryId == null && 
                request.active == null && request.perishable == null && 
                request.hazardous == null && request.fragile == null) {

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