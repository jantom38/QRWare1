package com.qrware.app.ui.viewmodel.OrderManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.CompleteOrderItemRequest
import com.qrware.app.data.model.OrderItemDTO
import com.qrware.app.data.repository.OrderItemRepository
import com.qrware.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QRScanUiState(
    val isScanning: Boolean = true,
    val scannedItem: OrderItemDTO? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val currentOrderItems: List<OrderItemDTO> = emptyList()
)

class QRScanOrderViewModel(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRScanUiState())
    val uiState: StateFlow<QRScanUiState> = _uiState.asStateFlow()

    init {
        loadOrderItems()
    }

    private fun loadOrderItems() {
        viewModelScope.launch {
            orderRepository.getOrderById(orderId)
                .onSuccess { order ->
                    _uiState.update { it.copy(currentOrderItems = order.orderItems ?: emptyList()) }
                }
        }
    }

    fun onQrScanned(rawQrCode: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isScanning = false, error = null) }

            val cleanQrCode = extractDataFromQr(rawQrCode)

            val localMatch = _uiState.value.currentOrderItems.find { item ->
                (item.qrCodeData != null && item.qrCodeData.equals(cleanQrCode, ignoreCase = true)) ||
                        item.productSku.equals(cleanQrCode, ignoreCase = true)
            }

            if (localMatch != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        scannedItem = localMatch,
                        successMessage = "Znaleziono: ${localMatch.productName}"
                    )
                }
            } else {
                orderItemRepository.scanQRCode(cleanQrCode)
                    .onSuccess { item ->
                        _uiState.update {
                            it.copy(isLoading = false, scannedItem = item, successMessage = "Znaleziono: ${item.productName}")
                        }
                    }
                    .onFailure {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Nie znaleziono w zamówieniu.\nSzukano kodu: $cleanQrCode",
                                isScanning = false
                            )
                        }
                    }
            }
        }
    }

    private fun extractDataFromQr(rawCode: String): String {
        return try {
            if (rawCode.contains("###")) {
                rawCode.substringBefore("###").trim()
            } else {
                rawCode.trim()
            }
        } catch (e: Exception) {
            rawCode.trim()
        }
    }

    fun pickItem(item: OrderItemDTO) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            orderItemRepository.pickOrderItem(item.id)
                .onSuccess { updatedItem ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            scannedItem = updatedItem,
                            successMessage = "Pobrano element"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun completeItem(item: OrderItemDTO, quantity: Int, notes: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val request = CompleteOrderItemRequest(
                completedQuantity = quantity,
                completionNotes = notes,
                qrCodeData = item.qrCodeData
            )

            orderItemRepository.completeOrderItem(item.id, request)
                .onSuccess { updatedItem ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            scannedItem = updatedItem,
                            successMessage = "Zrealizowano pomyślnie!"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun resetScanner() {
        _uiState.update {
            QRScanUiState(
                isScanning = true,
                scannedItem = null,
                error = null,
                successMessage = null,
                currentOrderItems = _uiState.value.currentOrderItems
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}