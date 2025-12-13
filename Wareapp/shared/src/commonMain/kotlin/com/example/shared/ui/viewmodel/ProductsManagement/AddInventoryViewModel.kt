package com.example.shared.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.LocationDTO
import com.example.shared.data.dto.ProductDTO
import com.example.shared.data.model.CreateInventoryRequest
import com.example.shared.data.model.InventoryStatus
import com.example.shared.data.repository.InventoryRepository
import com.example.shared.data.repository.LocationRepository
import com.example.shared.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

data class AddInventoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val presetProduct: ProductDTO? = null,
    val availableLocations: List<LocationDTO> = emptyList(),
    val locationsLoading: Boolean = false
)

class AddInventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddInventoryUiState())
    val uiState: StateFlow<AddInventoryUiState> = _uiState.asStateFlow()

    init {
        loadLocations()
    }

    fun loadProductData(productId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val product = productRepository.getProductById(productId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    presetProduct = product
                )
            } catch (e: Exception) {
                println("[AddInventoryVM] Error loading product: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Błąd ładowania danych produktu: ${e.message}"
                )
            }
        }
    }

    fun loadLocations() {
        viewModelScope.launch {
            println("[AddInventoryVM] Rozpoczynam ładowanie lokalizacji...")
            _uiState.value = _uiState.value.copy(locationsLoading = true)
            try {
                val paginatedResponse = locationRepository.getLocations(
                    page = 0,
                    size = 1000,
                    active = true
                )

                println("[AddInventoryVM] Pobrano z API: ${paginatedResponse.content.size} lokalizacji")

                val locations = paginatedResponse.content

                _uiState.value = _uiState.value.copy(
                    locationsLoading = false,
                    availableLocations = locations
                )
            } catch (e: Exception) {
                println("[AddInventoryVM] Błąd ładowania lokalizacji: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    locationsLoading = false,
                    error = "Błąd ładowania lokalizacji: ${e.message}"
                )
            }
        }
    }

    fun createInventoryItem(
        productId: Long,
        locationId: Long,
        quantity: Int,
        reservedQuantity: Int,
        status: InventoryStatus,
        lotNumber: String?,
        batchNumber: String?,
        serialNumber: String?,
        receivedDate: String?,
        expiryDate: String?,
        manufactureDate: String?,
        unitCost: Double?, // ZMIANA: BigDecimal -> Double (KMP compatible)
        supplierReference: String?,
        purchaseOrderNumber: String?,
        notes: String?,
        temperature: Int?,
        humidity: Int?,
        conditionRating: Int,
        quarantine: Boolean,
        quarantineReason: String?,
        hold: Boolean,
        holdReason: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Walidacja dat (kotlinx-datetime)
                // Sprawdzamy, czy data parsuje się do formatu ISO (RRRR-MM-DD), jeśli podana
                receivedDate?.let {
                    try {
                        LocalDate.parse(it)
                    } catch (e: IllegalArgumentException) {
                        throw Exception("Nieprawidłowy format daty przyjęcia: $it")
                    }
                }

                val request = CreateInventoryRequest(
                    productId = productId,
                    locationId = locationId,
                    quantity = quantity,
                    reservedQuantity = reservedQuantity,
                    status = status,
                    qrCode = generateQRCode(productId, lotNumber, batchNumber),
                    lotNumber = lotNumber,
                    batchNumber = batchNumber,
                    serialNumber = serialNumber,
                    receivedDate = receivedDate,
                    expiryDate = expiryDate,
                    manufactureDate = manufactureDate,
                    unitCost = unitCost,
                    supplierReference = supplierReference,
                    purchaseOrderNumber = purchaseOrderNumber,
                    notes = notes,
                    temperature = temperature,
                    humidity = humidity,
                    conditionRating = conditionRating,
                    quarantine = quarantine,
                    quarantineReason = quarantineReason,
                    hold = hold,
                    holdReason = holdReason
                )

                val newInventoryItem = inventoryRepository.createInventoryItem(request)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Pozycja magazynowa została utworzona! ID: ${newInventoryItem.id}"
                )
            } catch (e: Exception) {
                println("[AddInventoryVM] Błąd tworzenia itemu: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Błąd tworzenia pozycji magazynowej: ${e.message}"
                )
            }
        }
    }

    private fun generateQRCode(productId: Long, lotNumber: String?, batchNumber: String?): String {
        // ZMIANA: System.currentTimeMillis() -> Clock.System.now().toEpochMilliseconds()
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return buildString {
            append("INV-$productId")
            lotNumber?.let { append("-LOT-$it") }
            batchNumber?.let { append("-BATCH-$it") }
            append("-$timestamp")
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}