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
import com.qrware.app.data.model.*
import com.qrware.app.data.repository.OrderRepository
import com.qrware.app.ui.viewmodel.OrderManagement.MyOrdersViewModel
import com.qrware.app.ui.viewmodel.OrderManagement.MyOrdersViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    navController: NavController,
    orderRepository: OrderRepository
) {
    // Tworzymy ViewModel przy użyciu fabryki
    val viewModel: MyOrdersViewModel = viewModel(
        factory = MyOrdersViewModelFactory(orderRepository)
    )

    // Obserwujemy stan UI z ViewModelu
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moje Zamówienia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadMyOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadMyOrders() }) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
                uiState.orders.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = "Brak zamówień",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nie masz przypisanych zamówień",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.orders) { order ->
                            OrderCard(
                                order = order,
                                onOrderClick = {
                                    navController.navigate("order_details/${order.id}")
                                },
                                // Te akcje nie są zaimplementowane w VM dla listy,
                                // ale można je łatwo dodać w przyszłości
                                onStartOrder = { /* Opcjonalnie dodać w VM */ },
                                onCompleteOrder = { /* Opcjonalnie dodać w VM */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(
    order: OrderDTO,
    onOrderClick: () -> Unit,
    onStartOrder: (Long) -> Unit,
    onCompleteOrder: (Long) -> Unit
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
                Text(
                    text = order.orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                PriorityChip(priority = order.priority)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = getOrderTypeDisplayName(order.type),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusChip(status = order.status)
            }

            if (!order.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = order.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (order.totalItems != null && order.totalItems > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = (order.completedItems ?: 0).toFloat() / order.totalItems.toFloat()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Postęp: ${order.completedItems}/${order.totalItems}",
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
        }
    }
}

// Funkcje pomocnicze PriorityChip i StatusChip (takie same jak były)
@Composable
fun PriorityChip(priority: OrderPriority) {
    val (color, text) = when (priority) {
        OrderPriority.LOW -> Color(0xFF4CAF50) to "Niski"
        OrderPriority.NORMAL -> Color(0xFF9E9E9E) to "Normalny"
        OrderPriority.HIGH -> Color(0xFFFF9800) to "Wysoki"
        OrderPriority.URGENT -> Color(0xFFFF5722) to "Pilny"
        OrderPriority.CRITICAL -> Color(0xFFF44336) to "Krytyczny"
    }
    Surface(color = color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
        Text(text = text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val (color, text) = when (status) {
        OrderStatus.CREATED -> Color(0xFF2196F3) to "Utworzone"
        OrderStatus.ASSIGNED -> Color(0xFF9C27B0) to "Przypisane"
        OrderStatus.IN_PROGRESS -> Color(0xFFFF9800) to "W trakcie"
        OrderStatus.ON_HOLD -> Color(0xFF607D8B) to "Wstrzymane"
        OrderStatus.PARTIALLY_COMPLETED -> Color(0xFFFF5722) to "Częściowo"
        OrderStatus.COMPLETED -> Color(0xFF4CAF50) to "Zakończone"
        OrderStatus.CANCELLED -> Color(0xFFF44336) to "Anulowane"
        OrderStatus.FAILED -> Color(0xFF795548) to "Nieudane"
    }
    Surface(color = color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
        Text(text = text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}