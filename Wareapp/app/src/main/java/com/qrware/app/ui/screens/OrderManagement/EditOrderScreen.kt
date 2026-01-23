package com.qrware.app.ui.screens.OrderManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.dto.InventoryItemDTO
import com.qrware.app.data.dto.LocationDTO
import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.data.model.*
import com.qrware.app.data.repository.InventoryRepository
import com.qrware.app.data.repository.LocationRepository
import com.qrware.app.data.repository.OrderItemRepository
import com.qrware.app.data.repository.OrderRepository
import com.qrware.app.data.repository.ProductRepository
import com.qrware.app.data.repository.UserManagementRepository
import com.qrware.app.ui.viewmodel.OrderManagement.OrderDetailsViewModel
import com.qrware.app.ui.viewmodel.OrderManagement.OrderDetailsViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderScreen(
    orderId: Long,
    navController: NavController,
    orderRepository: OrderRepository,
    orderItemRepository: OrderItemRepository,
    inventoryRepository: InventoryRepository,
    productRepository: ProductRepository,
    userRepository: UserManagementRepository,
    locationRepository: LocationRepository
) {
    val viewModel: OrderDetailsViewModel = viewModel(
        factory = OrderDetailsViewModelFactory(
            orderRepository, 
            orderItemRepository, 
            inventoryRepository, 
            productRepository,
            orderId
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationDialogType by remember { mutableStateOf<LocationDialogType?>(null) }
    
    // Local state for editing order details
    var description by remember { mutableStateOf("") }
    var expectedDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var externalReference by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(OrderPriority.NORMAL) }
    
    var users by remember { mutableStateOf<List<AdminUserResponse>>(emptyList()) }
    var locations by remember { mutableStateOf<List<LocationDTO>>(emptyList()) }
    var products by remember { mutableStateOf<List<ProductDTO>>(emptyList()) }
    
    var dialogInventory by remember { mutableStateOf<List<InventoryItemDTO>>(emptyList()) }
    var isDialogInventoryLoading by remember { mutableStateOf(false) }

    // Initialize local state when order is loaded
    LaunchedEffect(uiState.order) {
        uiState.order?.let { order ->
            description = order.description ?: ""
            expectedDate = order.expectedDate ?: ""
            notes = order.notes ?: ""
            externalReference = order.externalReference ?: ""
            selectedPriority = order.priority
        }
    }

    // Load auxiliary data
    LaunchedEffect(Unit) {
        userRepository.getAllUsers(page = 0, size = 100)
            .onSuccess { pagedResponse -> users = pagedResponse.content }
        
        try {
            val locationsResponse = locationRepository.getLocations(page = 0, size = 100, active = true)
            locations = locationsResponse.content
        } catch (e: Exception) {
            // Handle error
        }
        
        try {
            val productsResponse = productRepository.getAllProducts(page = 0, size = 100)
            products = productsResponse.content
        } catch (e: Exception) {
            // Handle error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj Zlecenie #${uiState.order?.orderNumber ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.updateOrder(
                            description = description,
                            priority = selectedPriority,
                            expectedDate = expectedDate,
                            notes = notes,
                            externalReference = externalReference
                        )
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Zapisz")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj pozycję")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = "Błąd: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Text(
                            text = "Szczegóły zlecenia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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

                    item {
                        OutlinedTextField(
                            value = getPriorityDisplayName(selectedPriority),
                            onValueChange = { },
                            label = { Text("Priorytet") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { 
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

                    item {
                        Text(
                            text = "Pozycje zlecenia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    items(uiState.order?.orderItems ?: emptyList()) { item ->
                        EditOrderItemCard(
                            item = item,
                            onDelete = { 
                                viewModel.deleteOrderItem(item.id)
                            }
                        )
                    }
                }
            }
            
            if (uiState.isOperationProcessing) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        // Find location objects if IDs are present in the order
        val sourceLoc = uiState.order?.sourceLocationId?.let { id -> locations.find { it.id == id } }
        val destLoc = uiState.order?.destinationLocationId?.let { id -> locations.find { it.id == id } }

        AddOrderItemDialog(
            products = products,
            locations = locations,
            inventory = dialogInventory,
            isLoadingInventory = isDialogInventoryLoading,
            defaultSourceLocation = sourceLoc,
            defaultDestinationLocation = destLoc,
            onDismiss = { 
                showAddItemDialog = false 
                dialogInventory = emptyList()
            },
            onItemAdded = { request ->
                viewModel.addOrderItem(request)
                showAddItemDialog = false
                dialogInventory = emptyList()
            },
            onFetchInventoryForProduct = { productId ->
                isDialogInventoryLoading = true
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    try {
                        dialogInventory = inventoryRepository.getInventoryByProduct(productId)
                    } catch (e: Exception) {
                        dialogInventory = emptyList()
                    } finally {
                        isDialogInventoryLoading = false
                    }
                }
            },
            onFetchInventoryForLocation = { locationId ->
                isDialogInventoryLoading = true
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    try {
                        dialogInventory = inventoryRepository.getInventoryByLocation(locationId)
                    } catch (e: Exception) {
                        dialogInventory = emptyList()
                    } finally {
                        isDialogInventoryLoading = false
                    }
                }
            }
        )
    }
}

@Composable
fun EditOrderItemCard(
    item: OrderItemDTO,
    onDelete: () -> Unit
) {
    val isCompleted = item.status == OrderItemStatus.COMPLETED
    val isInactive = isCompleted

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isInactive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) 
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isInactive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "SKU: ${item.productSku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ilość: ${item.requestedQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isInactive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
                if (!item.sourceLocationName.isNullOrBlank()) {
                    Text(
                        text = "Lokalizacja: ${item.sourceLocationName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (isCompleted) {
                    Text(
                        text = "ZAKOŃCZONE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            if (!isInactive) {
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Usuń",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}