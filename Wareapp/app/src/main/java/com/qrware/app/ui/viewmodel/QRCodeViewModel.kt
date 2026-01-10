package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.*
import com.qrware.app.data.repository.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

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
                val errorMessage = when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            401 -> "Sesja wygasła lub brak autoryzacji. Zaloguj się ponownie."
                            403 -> "Brak uprawnień do skanowania tego kodu."
                            404 -> "Nie znaleziono takiego kodu QR w bazie."
                            500 -> "Wewnętrzny błąd serwera."
                            else -> "Błąd sieci: ${e.code()} ${e.message()}"
                        }
                    }
                    is IOException -> "Brak połączenia z serwerem. Sprawdź Wi-Fi."
                    else -> e.message ?: "Nieznany błąd skanowania"
                }

                _scanResult.value = QRScanResult(
                    code = code,
                    data = "",
                    type = QRCodeType.PRODUCT,
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
                val inventoryItem = inventoryResult.getOrNull()!!
                
                QRInventoryVerificationResult(
                    qrCodeExists = true,
                    inventoryExists = true,
                    inventoryItem = inventoryItem,
                    qrCodeData = qrData,
                    message = "✅ QR kod i stan magazynowy znalezione! " +
                            "Produkt: ${inventoryItem.product.name}, " +
                            "Ilość: ${inventoryItem.availableQuantity}/${inventoryItem.quantity}, " +
                            "Lokalizacja: ${inventoryItem.location.name}"
                )
            } else {
                QRInventoryVerificationResult(
                    qrCodeExists = true,
                    inventoryExists = false,
                    inventoryItem = null,
                    qrCodeData = qrData,
                    message = "⚠️ QR kod znaleziony, ale brak stanu magazynowego. " +
                            "Typ: ${qrData.type}, Dane: ${qrData.data}"
                )
            }
        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    if (e.code() == 404) {
                        QRInventoryVerificationResult(
                            qrCodeExists = true,
                            inventoryExists = false,
                            inventoryItem = null,
                            qrCodeData = qrData,
                            message = "⚠️ QR kod znaleziony, ale nie przypisano stanu magazynowego. " +
                                    "Typ: ${qrData.type}"
                        )
                    } else {
                        QRInventoryVerificationResult(
                            qrCodeExists = true,
                            inventoryExists = false,
                            inventoryItem = null,
                            qrCodeData = qrData,
                            message = "❌ Błąd sprawdzania stanu magazynowego: ${e.message()}"
                        )
                    }
                }
                else -> {
                    QRInventoryVerificationResult(
                        qrCodeExists = true,
                        inventoryExists = false,
                        inventoryItem = null,
                        qrCodeData = qrData,
                        message = "❌ Nie można sprawdzić stanu magazynowego: ${e.message ?: "Nieznany błąd"}"
                    )
                }
            }
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