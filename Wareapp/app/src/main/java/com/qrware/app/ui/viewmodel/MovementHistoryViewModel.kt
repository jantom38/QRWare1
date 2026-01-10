package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.dto.MovementHistoryDTO
import com.qrware.app.data.model.MovementType
import com.qrware.app.data.repository.MovementHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MovementHistoryViewModel(
    private val repository: MovementHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovementHistoryUiState())
    val uiState: StateFlow<MovementHistoryUiState> = _uiState.asStateFlow()

    init {
        loadRecentMovements()
    }

    fun loadMovementsByItemId(itemId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movements = repository.getMovementHistoryByItemId(itemId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania historii ruchów"
                )
            }
        }
    }

    fun loadMovementsByProductId(productId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movements = repository.getMovementHistoryByProductId(productId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania historii ruchów produktu"
                )
            }
        }
    }

    fun loadMovementsByLocationId(locationId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movements = repository.getMovementHistoryByLocationId(locationId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania historii ruchów lokalizacji"
                )
            }
        }
    }

    fun loadRecentMovements(limit: Int = 50) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movements = repository.getRecentMovements(limit)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania ostatnich ruchów"
                )
            }
        }
    }

    fun loadMovementsByType(movementType: MovementType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movements = repository.getMovementHistoryByType(movementType)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania ruchów typu ${movementType.displayName}"
                )
            }
        }
    }

    fun loadMovementsByDateRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val startDateTime = startDate.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val endDateTime = endDate.atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                
                val movements = repository.getMovementHistoryByDateRange(startDateTime, endDateTime)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania ruchów z zakresu dat"
                )
            }
        }
    }

    fun loadPendingApprovalMovements() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movements = repository.getPendingApprovalMovements()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania ruchów oczekujących na zatwierdzenie"
                )
            }
        }
    }

    fun loadInboundMovements() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movements = repository.getInboundMovements()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania ruchów przychodzących"
                )
            }
        }
    }

    fun loadOutboundMovements() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movements = repository.getOutboundMovements()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania ruchów wychodzących"
                )
            }
        }
    }

    fun loadAdjustmentMovements() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movements = repository.getAdjustmentMovements()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    movements = movements,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania korekt"
                )
            }
        }
    }

    fun searchMovements(keyword: String, searchIn: String = "reason") {
        if (keyword.isBlank()) {
            _uiState.value = _uiState.value.copy(
                filteredMovements = _uiState.value.movements,
                searchQuery = ""
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchQuery = keyword)
            try {
                val movements = repository.searchMovements(keyword, searchIn)
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    filteredMovements = movements
                )
            } catch (e: Exception) {
                val filtered = _uiState.value.movements.filter { movement ->
                    when (searchIn) {
                        "reason" -> movement.reason?.contains(keyword, ignoreCase = true) == true
                        "notes" -> movement.notes?.contains(keyword, ignoreCase = true) == true
                        "both" -> (movement.reason?.contains(keyword, ignoreCase = true) == true) ||
                                (movement.notes?.contains(keyword, ignoreCase = true) == true)
                        else -> movement.reason?.contains(keyword, ignoreCase = true) == true
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    filteredMovements = filtered,
                    error = "Wyszukiwanie przez API nie powiodło się, używam filtrowania lokalnego"
                )
            }
        }
    }

    fun filterByMovementType(movementType: MovementType?) {
        _uiState.value = _uiState.value.copy(selectedMovementType = movementType)
        applyFilters()
    }

    fun filterByDateRange(startDate: LocalDate?, endDate: LocalDate?) {
        _uiState.value = _uiState.value.copy(
            selectedStartDate = startDate,
            selectedEndDate = endDate
        )
        applyFilters()
    }

    fun filterByApprovalStatus(showPendingOnly: Boolean) {
        _uiState.value = _uiState.value.copy(showPendingApprovalOnly = showPendingOnly)
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.movements

        currentState.selectedMovementType?.let { type ->
            filtered = filtered.filter { it.movementType == type }
        }

        if (currentState.selectedStartDate != null || currentState.selectedEndDate != null) {
            filtered = filtered.filter { movement ->
                val movementDate = try {
                    LocalDateTime.parse(movement.movementDate).toLocalDate()
                } catch (e: Exception) {
                    return@filter true
                }

                val afterStart = currentState.selectedStartDate?.let { movementDate >= it } ?: true
                val beforeEnd = currentState.selectedEndDate?.let { movementDate <= it } ?: true
                
                afterStart && beforeEnd
            }
        }

        if (currentState.showPendingApprovalOnly) {
            filtered = filtered.filter { it.isApprovalPending() }
        }

        _uiState.value = currentState.copy(filteredMovements = filtered)
    }

    fun approveMovement(movementId: Long, approverComment: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            try {
                val approvedMovement = repository.approveMovement(movementId, approverComment)
                
                val updatedMovements = _uiState.value.movements.map { movement ->
                    if (movement.id == movementId) approvedMovement else movement
                }
                val updatedFiltered = _uiState.value.filteredMovements.map { movement ->
                    if (movement.id == movementId) approvedMovement else movement
                }
                
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    movements = updatedMovements,
                    filteredMovements = updatedFiltered,
                    successMessage = "Ruch został zatwierdzony pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Błąd podczas zatwierdzania ruchu"
                )
            }
        }
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedMovementType = null,
            selectedStartDate = null,
            selectedEndDate = null,
            showPendingApprovalOnly = false,
            searchQuery = "",
            filteredMovements = _uiState.value.movements
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun refreshData() {
        when (_uiState.value.currentView) {
            MovementHistoryView.RECENT -> loadRecentMovements()
            MovementHistoryView.PENDING_APPROVAL -> loadPendingApprovalMovements()
            MovementHistoryView.INBOUND -> loadInboundMovements()
            MovementHistoryView.OUTBOUND -> loadOutboundMovements()
            MovementHistoryView.ADJUSTMENTS -> loadAdjustmentMovements()
            MovementHistoryView.ITEM_SPECIFIC -> {
            }
            MovementHistoryView.LOCATION_SPECIFIC -> {
            }
            MovementHistoryView.PRODUCT_SPECIFIC -> {
            }
        }
    }

    fun setCurrentView(view: MovementHistoryView) {
        _uiState.value = _uiState.value.copy(currentView = view)
    }
}

data class MovementHistoryUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isProcessing: Boolean = false,
    val movements: List<MovementHistoryDTO> = emptyList(),
    val filteredMovements: List<MovementHistoryDTO> = emptyList(),
    val selectedMovementType: MovementType? = null,
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val showPendingApprovalOnly: Boolean = false,
    val searchQuery: String = "",
    val currentView: MovementHistoryView = MovementHistoryView.RECENT,
    val error: String? = null,
    val successMessage: String? = null
)

enum class MovementHistoryView {
    RECENT,
    PENDING_APPROVAL,
    INBOUND,
    OUTBOUND,
    ADJUSTMENTS,
    ITEM_SPECIFIC,
    LOCATION_SPECIFIC,
    PRODUCT_SPECIFIC
}