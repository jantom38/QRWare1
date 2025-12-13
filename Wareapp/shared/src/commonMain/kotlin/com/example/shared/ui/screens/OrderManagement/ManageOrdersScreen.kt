package com.example.shared.ui.screens.OrderManagement

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
import androidx.navigation.NavController
import com.example.shared.data.model.*
import com.example.shared.ui.viewmodel.ManageOrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrdersScreen(
    navController: NavController,
    viewModel: ManageOrdersViewModel // ZMIANA: Przyjmujemy ViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Zarządzanie Zamówieniami") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Filtruj",
                            tint = if (uiState.selectedFilter != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { viewModel.loadOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("create_order")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj zamówienie")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.allOrders.isEmpty() -> {
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
                            Icons.Default.Warning,
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadOrders() }) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
                uiState.visibleOrders.isEmpty() && !uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Brak zamówień",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.selectedFilter != null) "Brak zamówień dla wybranego filtra" else "Brak zamówień",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.selectedFilter == null) {
                            Button(
                                onClick = { navController.navigate("create_order") }
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Utwórz pierwsze zamówienie")
                            }
                        } else {
                            TextButton(onClick = { viewModel.setFilter(null) }) {
                                Text("Wyczyść filtry")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.selectedFilter != null) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Filtr: ${uiState.selectedFilter!!.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    TextButton(
                                        onClick = { viewModel.setFilter(null) }
                                    ) {
                                        Text("Wyczyść")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        item {
                            OrderSummaryCards(orders = uiState.allOrders)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        items(uiState.visibleOrders) { order ->
                            ManageOrderCard(
                                order = order,
                                onOrderClick = {
                                    navController.navigate("order_details/${order.id}")
                                },
                                onEditOrder = {
                                    navController.navigate("edit_order/${order.id}")
                                },
                                onStartOrder = { orderId ->
                                    viewModel.startOrder(orderId)
                                },
                                onCompleteOrder = { orderId ->
                                    viewModel.completeOrder(orderId)
                                },
                                onCancelOrder = { orderId ->
                                    viewModel.cancelOrder(orderId)
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.allOrders.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterOrdersDialog(
            onDismiss = { showFilterDialog = false },
            onFilterSelected = { filter ->
                viewModel.setFilter(filter)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun OrderSummaryCards(orders: List<OrderDTO>) {
    val activeOrders = orders.filter { it.isActive == true }
    val completedOrders = orders.filter { it.status == OrderStatus.COMPLETED }
    val overdueOrders = orders.filter { it.isOverdue == true }
    val highPriorityOrders = orders.filter { it.isHighPriority == true }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = "Aktywne",
            count = activeOrders.size,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Zakończone",
            count = completedOrders.size,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Opóźnione",
            count = overdueOrders.size,
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Priorytetowe",
            count = highPriorityOrders.size,
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrderCard(
    order: OrderDTO,
    onOrderClick: () -> Unit,
    onEditOrder: () -> Unit,
    onStartOrder: (Long) -> Unit,
    onCompleteOrder: (Long) -> Unit,
    onCancelOrder: (Long) -> Unit
) {
    Card(
        onClick = onOrderClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getOrderTypeDisplayName(order.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    StatusChip(status = order.status)
                    Spacer(modifier = Modifier.height(4.dp))
                    PriorityChip(priority = order.priority)
                }
            }

            if (!order.assignedToUsername.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.assignedToFullName ?: order.assignedToUsername,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (order.totalItems != null && order.totalItems > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (order.completedItems ?: 0).toFloat() / order.totalItems.toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${order.completedItems}/${order.totalItems}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${(order.completionPercentage ?: 0.0).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditOrder) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edytuj",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (order.canBeStarted == true) {
                    IconButton(onClick = { onStartOrder(order.id) }) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Rozpocznij",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }

                if (order.canBeCompleted == true) {
                    IconButton(onClick = { onCompleteOrder(order.id) }) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Zakończ",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }

                if (order.canBeCancelled == true) {
                    IconButton(onClick = { onCancelOrder(order.id) }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Anuluj",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterOrdersDialog(
    onDismiss: () -> Unit,
    onFilterSelected: (OrderStatus?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtruj zamówienia") },
        text = {
            LazyColumn {
                item {
                    TextButton(
                        onClick = { onFilterSelected(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Wszystkie", modifier = Modifier.fillMaxWidth())
                    }
                }
                items(OrderStatus.entries) { status ->
                    TextButton(
                        onClick = { onFilterSelected(status) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (status) {
                                OrderStatus.CREATED -> "Utworzone"
                                OrderStatus.ASSIGNED -> "Przypisane"
                                OrderStatus.IN_PROGRESS -> "W trakcie"
                                OrderStatus.ON_HOLD -> "Wstrzymane"
                                OrderStatus.PARTIALLY_COMPLETED -> "Częściowo ukończone"
                                OrderStatus.COMPLETED -> "Zakończone"
                                OrderStatus.CANCELLED -> "Anulowane"
                                OrderStatus.FAILED -> "Nieudane"
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}