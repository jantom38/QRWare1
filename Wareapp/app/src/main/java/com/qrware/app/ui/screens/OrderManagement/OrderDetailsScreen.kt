package com.qrware.app.ui.screens.OrderManagement

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.dto.InventoryItemDTO
import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.data.model.*
import com.qrware.app.data.repository.OrderItemRepository
import com.qrware.app.data.repository.OrderRepository
import com.qrware.app.ui.viewmodel.OrderManagement.OrderDetailsViewModel
import com.qrware.app.ui.viewmodel.OrderManagement.OrderDetailsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: Long,
    navController: NavController,
    orderRepository: OrderRepository,
    orderItemRepository: OrderItemRepository,
    inventoryRepository: com.qrware.app.data.repository.InventoryRepository,
    productRepository: com.qrware.app.data.repository.ProductRepository
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

    var showCompleteDialog by remember { mutableStateOf(false) }
    var selectedOrderItem by remember { mutableStateOf<OrderItemDTO?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.order?.orderNumber ?: "Zamówienie #$orderId")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    val order = uiState.order
                    if (order?.canBeStarted == true) {
                        IconButton(
                            onClick = { viewModel.startOrder() },
                            enabled = !uiState.isOperationProcessing
                        ) {
                            if (uiState.isOperationProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Rozpocznij")
                            }
                        }
                    }
                    if (order?.canBeCompleted == true) {
                        IconButton(
                            onClick = { viewModel.completeOrder() },
                            enabled = !uiState.isOperationProcessing
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Zakończ")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.order?.status == OrderStatus.IN_PROGRESS) {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("qr_scan_order/${orderId}")
                        }
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Skanuj QR")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Błąd",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadOrderDetails() }) {
                            Text("Odśwież")
                        }
                    }
                }
                uiState.order != null -> {
                    val order = uiState.order!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { OrderHeaderCard(order) }
                        item { OrderProgressCard(order) }

                        item {
                            Text(
                                text = "Pozycje zamówienia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(order.orderItems ?: emptyList()) { orderItem ->
                            OrderItemCard(
                                orderItem = orderItem,
                                inventoryRepository = inventoryRepository,
                                onCompleteItem = { item ->
                                    selectedOrderItem = item
                                    showCompleteDialog = true
                                },
                                onScanQR = { item ->
                                    navController.navigate("qr_scan_order/${order.id}")
                                }
                            )
                        }
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

    if (showCompleteDialog && selectedOrderItem != null) {
        CompleteOrderItemDialog(
            orderItem = selectedOrderItem!!,
            onDismiss = {
                showCompleteDialog = false
                selectedOrderItem = null
            },
            onComplete = { quantity, notes ->
                viewModel.completeOrderItem(selectedOrderItem!!, quantity, notes)
                showCompleteDialog = false
                selectedOrderItem = null
            }
        )
    }
}

@Composable
fun OrderHeaderCard(order: OrderDTO) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = order.orderNumber, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = getOrderTypeDisplayName(order.type), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(status = order.status)
                    Spacer(modifier = Modifier.height(4.dp))
                    PriorityChip(priority = order.priority)
                }
            }
            if (!order.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = order.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun OrderProgressCard(order: OrderDTO) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Postęp realizacji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            val progress = if (order.totalItems != null && order.totalItems > 0) {
                (order.completedItems ?: 0).toFloat() / order.totalItems.toFloat()
            } else 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${order.completedItems ?: 0} z ${order.totalItems ?: 0} pozycji", style = MaterialTheme.typography.bodyMedium)
                Text(text = "${(order.completionPercentage ?: 0.0).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItemCard(
    orderItem: OrderItemDTO,
    inventoryRepository: com.qrware.app.data.repository.InventoryRepository,
    onCompleteItem: (OrderItemDTO) -> Unit,
    onScanQR: (OrderItemDTO) -> Unit
) {
    var inventoryItem by remember(orderItem.inventoryItemId) { mutableStateOf<com.qrware.app.data.dto.InventoryItemDTO?>(null) }
    var isLoadingLocation by remember(orderItem.inventoryItemId) { mutableStateOf(false) }
    var locationError by remember(orderItem.inventoryItemId) { mutableStateOf<String?>(null) }

    LaunchedEffect(orderItem.inventoryItemId) {
        val inventoryItemId = orderItem.inventoryItemId
        if (inventoryItemId == null) return@LaunchedEffect
        isLoadingLocation = true
        locationError = null
        try {
            inventoryItem = inventoryRepository.getInventoryItemById(inventoryItemId)
        } catch (e: Exception) {
            locationError = e.message
        } finally {
            isLoadingLocation = false
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = orderItem.productName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = "SKU: ${orderItem.productSku}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = if(orderItem.status == OrderItemStatus.COMPLETED) Color(0xFF4CAF50).copy(0.1f) else Color.Gray.copy(0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(text = orderItem.status.name, modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ilość: ${orderItem.requestedQuantity}")
                Text("Zrealizowano: ${orderItem.completedQuantity}", fontWeight = FontWeight.Bold)
            }

            val resolvedLocation = inventoryItem?.location
            if (resolvedLocation != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val parts = listOfNotNull(
                    resolvedLocation.aisle?.let { "Alejka: $it" },
                    resolvedLocation.rack?.let { "Regał: $it" },
                    resolvedLocation.shelf?.let { "Półka: $it" },
                    resolvedLocation.bin?.let { "Bin: $it" },
                    resolvedLocation.zone.name.takeIf { it.isNotBlank() }?.let { "Strefa: $it" }
                )
                val details = parts.joinToString(" | ")
                Text(
                    text = "Lokalizacja: ${resolvedLocation.code} (${resolvedLocation.name})" + if (details.isNotBlank()) "\n$details" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (!orderItem.sourceLocationCode.isNullOrBlank() || !orderItem.sourceLocationName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lokalizacja: ${orderItem.sourceLocationCode ?: ""} ${orderItem.sourceLocationName ?: ""}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isLoadingLocation) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (locationError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nie udało się pobrać lokalizacji",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (orderItem.canBeCompleted == true) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (orderItem.requiresQRScan == true && orderItem.isQRScanned != true) {
                        OutlinedButton(onClick = { onScanQR(orderItem) }) {
                            Icon(Icons.Default.QrCodeScanner, null, Modifier.size(16.dp))
                            Text(" Skanuj")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(
                        onClick = { onCompleteItem(orderItem) },
                        enabled = orderItem.requiresQRScan != true || orderItem.isQRScanned == true
                    ) {
                        Icon(Icons.Default.Done, null, Modifier.size(16.dp))
                        Text(" Realizuj")
                    }
                }
            }
        }
    }
}

@Composable
fun CompleteOrderItemDialog(
    orderItem: OrderItemDTO,
    onDismiss: () -> Unit,
    onComplete: (quantity: Int, notes: String) -> Unit
) {
    var quantity by remember { mutableStateOf(orderItem.requestedQuantity.toString()) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Realizuj pozycję") },
        text = {
            Column {
                Text(orderItem.productName, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Ilość") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notatki") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val qty = quantity.toIntOrNull() ?: 0
                if (qty > 0) onComplete(qty, notes)
            }) { Text("Zatwierdź") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}