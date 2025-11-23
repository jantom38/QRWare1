package com.qrware.app.ui.viewmodel.ProductsManagement

import android.util.Log // Dodaj ten import
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.dto.LocationDTO
import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.data.model.CreateInventoryRequest
import com.qrware.app.data.model.InventoryStatus
import com.qrware.app.data.repository.InventoryRepository
import com.qrware.app.data.repository.LocationRepository
import com.qrware.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                Log.e("AddInventoryVM", "Error loading product", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Błąd ładowania danych produktu: ${e.message}"
                )
            }
        }
    }

    // --- ZMIANA: Funkcja jest teraz publiczna (usunięto private) ---
    fun loadLocations() {
        viewModelScope.launch {
            Log.d("AddInventoryVM", "Rozpoczynam ładowanie lokalizacji...")
            _uiState.value = _uiState.value.copy(locationsLoading = true)
            try {
                val paginatedResponse = locationRepository.getLocations(
                    page = 0,
                    size = 1000,
                    active = true // Pobieramy tylko aktywne
                )

                Log.d("AddInventoryVM", "Pobrano z API: ${paginatedResponse.content.size} lokalizacji")

                // --- DIAGNOSTYKA: Zakomentuj filtr, jeśli lista jest pusta ---
                // Oryginalny filtr:
                // val locations = paginatedResponse.content.filter { it.receivable }

                // Tymczasowo bierzemy wszystkie aktywne, aby sprawdzić czy API działa:
                val locations = paginatedResponse.content

                Log.d("AddInventoryVM", "Lokalizacje po filtrowaniu: ${locations.size}")

                _uiState.value = _uiState.value.copy(
                    locationsLoading = false,
                    availableLocations = locations
                )
            } catch (e: Exception) {
                Log.e("AddInventoryVM", "Błąd ładowania lokalizacji", e)
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
        unitCost: BigDecimal?,
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
                val receivedLocalDate = receivedDate?.let {
                    try { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) } catch (e: Exception) { null }
                }
                val expiryLocalDate = expiryDate?.let {
                    try { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) } catch (e: Exception) { null }
                }
                val manufactureLocalDate = manufactureDate?.let {
                    try { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) } catch (e: Exception) { null }
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

                    // ZMIANA TUTAJ - przekazujemy Stringi bezpośrednio:
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
                Log.e("AddInventoryVM", "Błąd tworzenia itemu", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Błąd tworzenia pozycji magazynowej: ${e.message}"
                )
            }
        }
    }

    private fun generateQRCode(productId: Long, lotNumber: String?, batchNumber: String?): String {
        val timestamp = System.currentTimeMillis()
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