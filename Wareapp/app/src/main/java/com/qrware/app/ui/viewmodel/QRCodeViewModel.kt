package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.*
import com.qrware.app.data.repository.QRCodeRepository
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

    init {
        loadQRCodes()
        loadStats()
    }

    fun loadQRCodes(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // ZMIANA: 'response' to teraz bezpośrednio PaginatedResponse
                val response = qrCodeRepository.getAllQRCodes(page, size)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    qrCodes = response.content, // <-- ZMIANA
                    totalPages = response.totalPages, // <-- ZMIANA
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
                // ZMIANA: 'response' to teraz bezpośrednio Lista
                val response = qrCodeRepository.getActiveQRCodes()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    qrCodes = response // <-- ZMIANA
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
                // ZMIANA: 'response' to teraz bezpośrednio Lista
                val response = qrCodeRepository.getQRCodesByType(type)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    qrCodes = response // <-- ZMIANA
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)
            try {
                // ZMIANA: 'qrData' to teraz bezpośrednio QRCodeData
                // Jeśli kod nie zostanie znaleziony, serwer rzuci wyjątek (np. 404),
                // który zostanie przechwycony przez blok catch.
                val qrData = qrCodeRepository.scanQRCode(code)

                _scanResult.value = QRScanResult(
                    code = qrData.code,
                    data = qrData.data,
                    type = qrData.type,
                    entityType = qrData.entityType,
                    entityId = qrData.entityId,
                    success = true,
                    message = "Kod QR zeskanowany pomyślnie"
                )
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    successMessage = "Kod QR zeskanowany pomyślnie"
                )
                // Odśwież listę i statystyki
                loadQRCodes()
                loadStats()

            } catch (e: Exception) {
                // ZMIANA: Ten blok obsłuży teraz błędy, np. 404 Not Found
                _scanResult.value = QRScanResult(
                    code = code,
                    data = "",
                    type = QRCodeType.CUSTOM,
                    entityType = null,
                    entityId = null,
                    success = false,
                    message = e.message ?: "Błąd podczas skanowania"
                )
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message ?: "Błąd podczas skanowania kodu QR"
                )
            }
        }
    }

    fun generateQRCode(request: GenerateQRRequest) {
        viewModelScope.launch {
            try {
                val response = qrCodeRepository.generateQRCode(request)
                loadQRCodes() // Odśwież listę
                loadStats() // Odśwież statystyki
                _uiState.value = _uiState.value.copy(
                    successMessage = "Kod QR został wygenerowany pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas generowania kodu QR"
                )
            }
        }
    }

    fun updateQRCode(qrCodeId: Long, request: UpdateQRRequest) {
        viewModelScope.launch {
            try {
                qrCodeRepository.updateQRCode(qrCodeId, request)
                loadQRCodes() // Odśwież listę
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
                loadQRCodes() // Odśwież listę
                loadStats() // Odśwież statystyki
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
                loadQRCodes() // Odśwież listę
                loadStats() // Odśwież statystyki
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

    private fun loadStats() {
        viewModelScope.launch {
            try {
                // ZMIANA: 'response' to teraz bezpośrednio QRStatsResponse
                val response = qrCodeRepository.getQRStats()
                _stats.value = response // <-- ZMIANA
            } catch (e: Exception) {
                // Ignoruj błędy statystyk
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