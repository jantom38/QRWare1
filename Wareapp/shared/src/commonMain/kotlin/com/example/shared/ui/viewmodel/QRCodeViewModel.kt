package com.example.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.InventoryItemDTO
import com.example.shared.data.model.*
import com.example.shared.data.repository.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QRCodeViewModel(
    private val qrCodeRepository: QRCodeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRCodeUiState())
    val uiState: StateFlow<QRCodeUiState> = _uiState.asStateFlow()

    private val _scanResult = MutableStateFlow<QRScanResult?>(null)
    val scanResult: StateFlow<QRScanResult?> = _scanResult.asStateFlow()

    private val _stats = MutableStateFlow<QRStatsResponse?>(null)
    val stats: StateFlow<QRStatsResponse?> = _stats.asStateFlow()

    private val _generatedQRCode = MutableStateFlow<QRCodeData?>(null)
    val generatedQRCode: StateFlow<QRCodeData?> = _generatedQRCode.asStateFlow()

    init {
        loadQRCodes()
        loadStats()
    }

    fun loadQRCodes(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = qrCodeRepository.getAllQRCodes(page, size)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    qrCodes = response.content,
                    totalPages = response.totalPages,
                    currentPage = page
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania kodów QR"
                )
            }
        }
    }

    fun loadActiveQRCodes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = qrCodeRepository.getActiveQRCodes()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    qrCodes = response
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania aktywnych kodów QR"
                )
            }
        }
    }

    fun loadQRCodesByType(type: QRCodeType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = qrCodeRepository.getQRCodesByType(type)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    qrCodes = response
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania kodów QR"
                )
            }
        }
    }

    fun scanQRCode(code: String) {
        if (_uiState.value.isScanning) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)

            try {
                val qrData = qrCodeRepository.scanQRCode(code)

                // Weryfikacja stanu magazynowego
                val inventoryVerification = verifyInventoryForQRCode(code, qrData)

                _scanResult.value = QRScanResult(
                    code = qrData.code,
                    data = qrData.data,
                    type = qrData.type,
                    entityType = qrData.entityType,
                    entityId = qrData.entityId,
                    success = true,
                    message = inventoryVerification.message
                )
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    successMessage = inventoryVerification.message
                )

                loadStats()

            } catch (e: Exception) {
                val errorMessage = e.message ?: "Nieznany błąd skanowania"

                _scanResult.value = QRScanResult(
                    code = code,
                    data = "",
                    type = QRCodeType.CUSTOM,
                    entityType = null,
                    entityId = null,
                    success = false,
                    message = errorMessage
                )

                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = errorMessage
                )
            }
        }
    }

    private suspend fun verifyInventoryForQRCode(
        qrCode: String,
        qrData: QRCodeData
    ): QRInventoryVerificationResult {
        return try {
            val inventoryResult = qrCodeRepository.getInventoryByQRCode(qrCode)

            if (inventoryResult.isSuccess) {
                // Pobieramy DTO
                val itemDTO: InventoryItemDTO = inventoryResult.getOrNull()!!

                // Tworzymy wynik.
                // UWAGA: Przekazujemy 'null' do inventoryItem (bo typy się nie zgadzają),
                // ale budujemy pełny komunikat tekstowy z danych DTO.
                QRInventoryVerificationResult(
                    qrCodeExists = true,
                    inventoryExists = true,
                    inventoryItem = null, // ZMIANA: null, aby uniknąć błędu typu. Dane są w 'message'.
                    qrCodeData = qrData,
                    message = "✅ Znaleziono: ${itemDTO.product.name} " +
                            "(${itemDTO.availableQuantity}/${itemDTO.quantity}) " +
                            "w ${itemDTO.location.name}"
                )
            } else {
                QRInventoryVerificationResult(
                    qrCodeExists = true,
                    inventoryExists = false,
                    inventoryItem = null,
                    qrCodeData = qrData,
                    message = "⚠️ QR kod istnieje, ale brak stanu magazynowego."
                )
            }
        } catch (e: Exception) {
            QRInventoryVerificationResult(
                qrCodeExists = true,
                inventoryExists = false,
                inventoryItem = null,
                qrCodeData = qrData,
                message = "❌ Błąd weryfikacji: ${e.message ?: "Nieznany błąd"}"
            )
        }
    }

    fun generateQRCode(request: GenerateQRRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = qrCodeRepository.generateQRCode(request)

                _generatedQRCode.value = response

                loadQRCodes()
                loadStats()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Kod QR został wygenerowany pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas generowania kodu QR"
                )
            }
        }
    }

    fun updateQRCode(qrCodeId: Long, request: UpdateQRRequest) {
        viewModelScope.launch {
            try {
                qrCodeRepository.updateQRCode(qrCodeId, request)
                loadQRCodes()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Kod QR został zaktualizowany pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas aktualizacji kodu QR"
                )
            }
        }
    }

    fun deleteQRCode(qrCodeId: Long) {
        viewModelScope.launch {
            try {
                qrCodeRepository.deleteQRCode(qrCodeId)
                loadQRCodes()
                loadStats()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Kod QR został usunięty pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas usuwania kodu QR"
                )
            }
        }
    }

    fun toggleQRCodeActive(qrCodeId: Long) {
        viewModelScope.launch {
            try {
                qrCodeRepository.toggleQRCodeActive(qrCodeId)
                loadQRCodes()
                loadStats()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Status kodu QR został zmieniony"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas zmiany statusu kodu QR"
                )
            }
        }
    }

    fun clearGeneratedQRCode() {
        _generatedQRCode.value = null
        clearMessages()
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val response = qrCodeRepository.getQRStats()
                _stats.value = response
            } catch (e: Exception) {
            }
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun nextPage() {
        if (_uiState.value.currentPage < _uiState.value.totalPages - 1) {
            loadQRCodes(_uiState.value.currentPage + 1)
        }
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 0) {
            loadQRCodes(_uiState.value.currentPage - 1)
        }
    }
}

data class QRCodeUiState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val qrCodes: List<QRCodeData> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0
)