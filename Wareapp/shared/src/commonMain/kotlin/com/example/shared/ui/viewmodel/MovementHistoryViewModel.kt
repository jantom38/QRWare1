package com.example.shared.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.shared.data.dto.MovementHistoryDTO
import com.example.shared.data.model.MovementType
import com.example.shared.data.repository.MovementHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class MovementHistoryViewModel(
    private val repository: MovementHistoryRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(MovementHistoryUiState())
    val uiState: StateFlow<MovementHistoryUiState> = _uiState.asStateFlow()

    init {
        loadRecentMovements()
    }

    fun loadMovementsByItemId(itemId: Long) {
        scope.launch {
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
        scope.launch {
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
        scope.launch {
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
        scope.launch {
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
        scope.launch {
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
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val startDateTime =
                    formatDateTime(LocalDateTime(startDate.year, startDate.monthNumber, startDate.dayOfMonth, 0, 0, 0))
                val endDateTime =
                    formatDateTime(LocalDateTime(endDate.year, endDate.monthNumber, endDate.dayOfMonth, 23, 59, 59))

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
        scope.launch {
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
        scope.launch {
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
        scope.launch {
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
        scope.launch {
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

        scope.launch {
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
                    run {
                        val dt = try {
                            LocalDateTime.parse(movement.movementDate)
                        } catch (e: Exception) {
                            return@filter true
                        }
                        dt.date
                    }
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
        scope.launch {
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
            MovementHistoryView.ITEM_SPECIFIC -> { /* refresh via stored ID if needed */
            }

            MovementHistoryView.LOCATION_SPECIFIC -> { /* refresh via stored ID if needed */
            }

            MovementHistoryView.PRODUCT_SPECIFIC -> { /* refresh via stored ID if needed */
            }
        }
    }

    fun setCurrentView(view: MovementHistoryView) {
        _uiState.value = _uiState.value.copy(currentView = view)
    }

    private fun formatDateTime(dt: LocalDateTime): String {
        // Format as yyyy-MM-ddTHH:mm:ss for backend API
        fun two(i: Int) = i.toString().padStart(2, '0')
        return "${dt.year}-${two(dt.monthNumber)}-${two(dt.dayOfMonth)}T${two(dt.hour)}:${two(dt.minute)}:${two(dt.second)}"
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
        RECENT, PENDING_APPROVAL, INBOUND, OUTBOUND, ADJUSTMENTS,
        ITEM_SPECIFIC, LOCATION_SPECIFIC, PRODUCT_SPECIFIC
    }
}