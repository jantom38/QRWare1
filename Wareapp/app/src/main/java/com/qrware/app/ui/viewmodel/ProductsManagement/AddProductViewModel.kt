package com.qrware.app.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.CreateProductRequest
import com.qrware.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

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
        cost: BigDecimal?,
        unit: String?,
        weight: BigDecimal?,
        length: BigDecimal?,
        width: BigDecimal?,
        height: BigDecimal?,
        minimumStock: Int?,
        maximumStock: Int?,
        reorderPoint: Int?,
        active: Boolean,
        perishable: Boolean,
        hazardous: Boolean,
        fragile: Boolean,
        manufacturer: String?,
        supplier: String?,
        storageConditions: String?,
        barcode: String?,
        categoryId: Long?
    ) {
        viewModelScope.launch {
            _uiState.value = AddProductUiState(isLoading = true)
            try {
                val request = CreateProductRequest(
                    sku = sku,
                    name = name,
                    description = description,
                    price = price,
                    cost = cost,
                    unit = unit ?: "PIECE",
                    weight = weight,
                    length = length,
                    width = width,
                    height = height,
                    minimumStock = minimumStock,
                    maximumStock = maximumStock,
                    reorderPoint = reorderPoint,
                    active = active,
                    perishable = perishable,
                    hazardous = hazardous,
                    fragile = fragile,
                    manufacturer = manufacturer,
                    supplier = supplier,
                    storageConditions = storageConditions,
                    barcode = barcode,
                    categoryId = categoryId
                )

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

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}