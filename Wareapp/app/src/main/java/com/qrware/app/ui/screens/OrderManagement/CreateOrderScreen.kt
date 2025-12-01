package com.qrware.app.ui.screens.OrderManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrware.app.data.dto.LocationDTO
import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.data.model.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    navController: NavController,
    orderRepository: com.qrware.app.data.repository.OrderRepository,
    userRepository: com.qrware.app.data.repository.UserManagementRepository,
    locationRepository: com.qrware.app.data.repository.LocationRepository,
    productRepository: com.qrware.app.data.repository.ProductRepository,
    orderItemRepository: com.qrware.app.data.repository.OrderItemRepository
) {
    var orderNumber by remember { mutableStateOf("") }
    var selectedOrderType by remember { mutableStateOf<OrderType?>(null) }
    var selectedPriority by remember { mutableStateOf(OrderPriority.NORMAL) }
    var description by remember { mutableStateOf("") }
    var expectedDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var externalReference by remember { mutableStateOf("") }

    // User and location selection
    var selectedAssignedUser by remember { mutableStateOf<AdminUserResponse?>(null) }
    var selectedSourceLocation by remember { mutableStateOf<LocationDTO?>(null) }
    var selectedDestinationLocation by remember { mutableStateOf<LocationDTO?>(null) }

    // Order items
    var orderItems by remember { mutableStateOf<List<OrderItemRequest>>(emptyList()) }
    
    // UI State
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationDialogType by remember { mutableStateOf<LocationDialogType?>(null) }
    var showOrderTypeDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    // Data lists
    var users by remember { mutableStateOf<List<AdminUserResponse>>(emptyList()) }
    var locations by remember { mutableStateOf<List<LocationDTO>>(emptyList()) }
    var products by remember { mutableStateOf<List<ProductDTO>>(emptyList()) }

    // Load initial data
    LaunchedEffect(Unit) {
        // Load users, locations, and products
        userRepository.getAllUsers(page = 0, size = 100)
            .onSuccess { pagedResponse -> users = pagedResponse.content }
            .onFailure { errorMessage = "Błąd ładowania użytkowników: ${it.message}" }
        
        try {
            val locationsResponse = locationRepository.getLocations(page = 0, size = 100, active = true)
            locations = locationsResponse.content
        } catch (e: Exception) {
            errorMessage = "Błąd ładowania lokalizacji: ${e.message}"
        }
        
        try {
            val productsResponse = productRepository.getAllProducts(page = 0, size = 100)
            products = productsResponse.content
        } catch (e: Exception) {
            errorMessage = "Błąd ładowania produktów: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Utwórz Zamówienie") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Create order
                            if (selectedOrderType != null) {
                                isLoading = true
                                errorMessage = null
                                
                                val request = CreateOrderRequest(
                                    orderNumber = orderNumber.takeIf { it.isNotBlank() },
                                    type = selectedOrderType!!,
                                    description = description.takeIf { it.isNotBlank() },
                                    assignedToId = selectedAssignedUser?.id,
                                    sourceLocationId = selectedSourceLocation?.id,
                                    destinationLocationId = selectedDestinationLocation?.id,
                                    expectedDate = expectedDate.takeIf { it.isNotBlank() },
                                    priority = selectedPriority
                                )
                                
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    orderRepository.createOrder(request)
                                        .onSuccess { createdOrder ->
                                            // Add order items if any
                                            if (orderItems.isNotEmpty()) {
                                                addOrderItems(createdOrder.id, orderItems, orderItemRepository) {
                                                    navController.popBackStack()
                                                }
                                            } else {
                                                navController.popBackStack()
                                            }
                                        }
                                        .onFailure { 
                                            errorMessage = it.message
                                            isLoading = false
                                        }
                                }
                            }
                        },
                        enabled = selectedOrderType != null && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("UTWÓRZ")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Basic Information Section
            item {
                Text(
                    text = "Podstawowe informacje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = orderNumber,
                    onValueChange = { orderNumber = it },
                    label = { Text("Numer zamówienia (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Zostanie wygenerowany automatycznie jeśli pusty") }
                )
            }

            item {
                OutlinedTextField(
                    value = selectedOrderType?.let { getOrderTypeDisplayName(it) } ?: "",
                    onValueChange = { },
                    label = { Text("Typ zamówienia *") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showOrderTypeDialog = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Wybierz")
                        }
                    },
                    isError = selectedOrderType == null
                )
            }

            item {
                OutlinedTextField(
                    value = selectedPriority.let { getPriorityDisplayName(it) },
                    onValueChange = { },
                    label = { Text("Priorytet") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { 
                            // Cycle through priorities
                            selectedPriority = when (selectedPriority) {
                                OrderPriority.LOW -> OrderPriority.NORMAL
                                OrderPriority.NORMAL -> OrderPriority.HIGH
                                OrderPriority.HIGH -> OrderPriority.URGENT
                                OrderPriority.URGENT -> OrderPriority.CRITICAL
                                OrderPriority.CRITICAL -> OrderPriority.LOW
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Zmień")
                        }
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            // Assignment Section
            item {
                Text(
                    text = "Przypisanie i lokalizacje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = selectedAssignedUser?.let { "${it.firstName} ${it.lastName}".takeIf { name -> name.trim().isNotEmpty() } ?: it.username } ?: "",
                    onValueChange = { },
                    label = { Text("Przypisz do użytkownika") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (selectedAssignedUser != null) {
                                IconButton(onClick = { selectedAssignedUser = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                                }
                            }
                            IconButton(onClick = { showUserDialog = true }) {
                                Icon(Icons.Default.Person, contentDescription = "Wybierz")
                            }
                        }
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = selectedSourceLocation?.name ?: "",
                    onValueChange = { },
                    label = { Text("Lokalizacja źródłowa") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (selectedSourceLocation != null) {
                                IconButton(onClick = { selectedSourceLocation = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                                }
                            }
                            IconButton(onClick = { 
                                locationDialogType = LocationDialogType.SOURCE
                                showLocationDialog = true 
                            }) {
                                Icon(Icons.Default.Place, contentDescription = "Wybierz")
                            }
                        }
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = selectedDestinationLocation?.name ?: "",
                    onValueChange = { },
                    label = { Text("Lokalizacja docelowa") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (selectedDestinationLocation != null) {
                                IconButton(onClick = { selectedDestinationLocation = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                                }
                            }
                            IconButton(onClick = { 
                                locationDialogType = LocationDialogType.DESTINATION
                                showLocationDialog = true 
                            }) {
                                Icon(Icons.Default.Place, contentDescription = "Wybierz")
                            }
                        }
                    }
                )
            }

            // Additional Information Section
            item {
                Text(
                    text = "Dodatkowe informacje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = expectedDate,
                    onValueChange = { expectedDate = it },
                    label = { Text("Data oczekiwana (YYYY-MM-DD HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("2025-12-01 10:00") }
                )
            }

            item {
                OutlinedTextField(
                    value = externalReference,
                    onValueChange = { externalReference = it },
                    label = { Text("Referencja zewnętrzna") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notatki") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            // Order Items Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pozycje zamówienia (${orderItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedButton(
                        onClick = { showAddItemDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dodaj pozycję")
                    }
                }
            }

            items(orderItems) { item ->
                OrderItemCard(
                    item = item,
                    onRemove = { 
                        orderItems = orderItems - item
                    }
                )
            }

            if (orderItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "Brak pozycji zamówienia\nMożesz dodać je teraz lub później",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showOrderTypeDialog) {
        OrderTypeDialog(
            onDismiss = { showOrderTypeDialog = false },
            onTypeSelected = { type ->
                selectedOrderType = type
                showOrderTypeDialog = false
            }
        )
    }

    if (showUserDialog) {
        UserSelectionDialog(
            users = users,
            onDismiss = { showUserDialog = false },
            onUserSelected = { user ->
                selectedAssignedUser = user
                showUserDialog = false
            }
        )
    }

    if (showLocationDialog && locationDialogType != null) {
        LocationSelectionDialog(
            locations = locations,
            onDismiss = { showLocationDialog = false },
            onLocationSelected = { location ->
                when (locationDialogType) {
                    LocationDialogType.SOURCE -> selectedSourceLocation = location
                    LocationDialogType.DESTINATION -> selectedDestinationLocation = location
                    null -> { }
                }
                showLocationDialog = false
                locationDialogType = null
            }
        )
    }

    if (showAddItemDialog) {
        AddOrderItemDialog(
            products = products,
            onDismiss = { showAddItemDialog = false },
            onItemAdded = { newItem ->
                orderItems = orderItems + newItem
                showAddItemDialog = false
            }
        )
    }
}

// Helper data classes and enums
data class OrderItemRequest(
    val productId: Long,
    val productName: String,
    val productSku: String,
    val requestedQuantity: Int,
    val notes: String? = null
)

enum class LocationDialogType {
    SOURCE, DESTINATION
}

// Helper functions
fun getPriorityDisplayName(priority: OrderPriority): String {
    return when (priority) {
        OrderPriority.LOW -> "Niski"
        OrderPriority.NORMAL -> "Normalny"
        OrderPriority.HIGH -> "Wysoki"
        OrderPriority.URGENT -> "Pilny"
        OrderPriority.CRITICAL -> "Krytyczny"
    }
}

suspend fun addOrderItems(
    orderId: Long,
    items: List<OrderItemRequest>,
    orderItemRepository: com.qrware.app.data.repository.OrderItemRepository,
    onComplete: () -> Unit
) {
    var completedCount = 0
    items.forEach { item ->
        val request = CreateOrderItemRequest(
            productId = item.productId,
            requestedQuantity = item.requestedQuantity,
            notes = item.notes
        )
        
        orderItemRepository.addOrderItem(orderId, request)
            .onSuccess {
                completedCount++
                if (completedCount == items.size) {
                    onComplete()
                }
            }
            .onFailure {
                // Handle error - for now just continue
                completedCount++
                if (completedCount == items.size) {
                    onComplete()
                }
            }
    }
}