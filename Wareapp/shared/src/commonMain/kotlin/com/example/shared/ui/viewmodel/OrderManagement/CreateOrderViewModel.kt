package com.example.shared.ui.viewmodel.OrderManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.LocationDTO
import com.example.shared.data.dto.ProductDTO
import com.example.shared.data.model.*
import com.example.shared.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderItemUiModel(
    val productId: Long,
    val productName: String,
    val requestedQuantity: Int,
    val notes: String?,
    val requiresExactInventory: Boolean
)

data class CreateOrderUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,

    val users: List<AdminUserResponse> = emptyList(),
    val locations: List<LocationDTO> = emptyList(),
    val products: List<ProductDTO> = emptyList(),
    val availableInventory: Map<Long, Int> = emptyMap(),

    val orderNumber: String = "",
    val selectedOrderType: OrderType? = null,
    val selectedPriority: OrderPriority = OrderPriority.NORMAL,
    val description: String = "",
    val expectedDate: String = "",
    val notes: String = "",
    val externalReference: String = "",

    val selectedAssignedUser: AdminUserResponse? = null,
    val selectedSourceLocation: LocationDTO? = null,
    val selectedDestinationLocation: LocationDTO? = null,

    val orderItems: List<OrderItemUiModel> = emptyList()
)

class CreateOrderViewModel(
    private val orderRepository: OrderRepository,
    private val userRepository: UserManagementRepository,
    private val locationRepository: LocationRepository,
    private val productRepository: ProductRepository,
    private val orderItemRepository: OrderItemRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateOrderUiState())
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Pobieranie danych. Zakładamy, że userRepository zwraca Result<Page>,
                // a pozostałe repozytoria rzucają wyjątkami lub zwracają obiekt Page bezpośrednio.
                val usersResult = userRepository.getAllUsers(0, 100)
                val locationsResult = locationRepository.getLocations(0, 100, true)
                val productsResult = productRepository.getAllProducts(0, 100)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        // POPRAWKA: Użycie getOrNull()?.content zamiast getOrElse { emptyList() }
                        users = usersResult.getOrNull()?.content ?: emptyList(),
                        locations = locationsResult.content,
                        products = productsResult.content
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd inicjalizacji: ${e.message}") }
            }
        }
    }

    fun onSourceLocationSelected(location: LocationDTO?) {
        _uiState.update { it.copy(selectedSourceLocation = location) }

        if (location != null) {
            viewModelScope.launch {
                try {
                    val inventory = inventoryRepository.getInventoryByLocation(location.id)
                    val inventoryMap = inventory.groupBy { it.product.id }
                        .mapValues { (_, items) -> items.sumOf { it.availableQuantity } }

                    _uiState.update { it.copy(availableInventory = inventoryMap) }
                } catch (e: Exception) {
                    println("[CreateOrderVM] Błąd ładowania inwentarza: ${e.message}")
                }
            }
        } else {
            _uiState.update { it.copy(availableInventory = emptyMap()) }
        }
    }

    fun onOrderNumberChange(v: String) = _uiState.update { it.copy(orderNumber = v) }
    fun onTypeSelected(v: OrderType) = _uiState.update { it.copy(selectedOrderType = v) }
    fun onPriorityChange(v: OrderPriority) = _uiState.update { it.copy(selectedPriority = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onExpectedDateChange(v: String) = _uiState.update { it.copy(expectedDate = v) }
    fun onNotesChange(v: String) = _uiState.update { it.copy(notes = v) }
    fun onExternalRefChange(v: String) = _uiState.update { it.copy(externalReference = v) }
    fun onUserSelected(v: AdminUserResponse?) = _uiState.update { it.copy(selectedAssignedUser = v) }
    fun onDestinationLocationSelected(v: LocationDTO?) = _uiState.update { it.copy(selectedDestinationLocation = v) }

    fun addOrderItem(item: OrderItemUiModel) {
        _uiState.update { it.copy(orderItems = it.orderItems + item) }
    }

    fun removeOrderItem(item: OrderItemUiModel) {
        _uiState.update { it.copy(orderItems = it.orderItems - item) }
    }

    fun submitOrder() {
        val state = _uiState.value
        if (state.selectedOrderType == null) {
            _uiState.update { it.copy(error = "Wybierz typ zamówienia") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val request = CreateOrderRequest(
                    orderNumber = state.orderNumber.takeIf { it.isNotBlank() },
                    type = state.selectedOrderType,
                    description = state.description.takeIf { it.isNotBlank() },
                    assignedToId = state.selectedAssignedUser?.id,
                    sourceLocationId = state.selectedSourceLocation?.id,
                    destinationLocationId = state.selectedDestinationLocation?.id,
                    expectedDate = state.expectedDate.takeIf { it.isNotBlank() },
                    priority = state.selectedPriority
                )

                val createdOrderResult = orderRepository.createOrder(request)

                createdOrderResult.onSuccess { createdOrder ->
                    println("[CreateOrderVM] Zamówienie utworzone: ${createdOrder.id}")

                    if (state.orderItems.isNotEmpty()) {
                        var errorCount = 0
                        state.orderItems.forEach { item ->
                            val itemRequest = CreateOrderItemRequest(
                                productId = item.productId,
                                requestedQuantity = item.requestedQuantity,
                                notes = item.notes,
                                sourceLocationId = state.selectedSourceLocation?.id,
                                destinationLocationId = state.selectedDestinationLocation?.id,
                                requiresExactInventory = item.requiresExactInventory
                            )
                            val itemResult = orderItemRepository.addOrderItem(createdOrder.id, itemRequest)
                            if (itemResult.isFailure) errorCount++
                        }

                        if (errorCount > 0) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    success = true,
                                    error = "Zamówienie utworzone, ale wystąpił błąd przy dodawaniu $errorCount pozycji."
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, success = true) }
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, success = true) }
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Błąd tworzenia zamówienia: ${e.message}") }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Krytyczny błąd: ${e.message}") }
                e.printStackTrace()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}