package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.dto.InventoryItemDTO
import com.qrware.app.data.model.InventoryStatus
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.InventoryViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val viewModel: InventoryViewModel = viewModel(
        factory = appContainer.inventoryViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stan Magazynowy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadInventoryItems() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        },
        // --- NOWY PRZYCISK ---
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_product")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj nowy produkt")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ... (Reszta pliku bez zmian) ...

            // Filtry statusu
            StatusFilterRow(
                onStatusSelected = { status ->
                    if (status != null) {
                        viewModel.filterByStatus(status)
                    } else {
                        viewModel.loadInventoryItems()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Komunikaty błędów/sukcesu
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista pozycji
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.inventoryItems) { item ->
                        InventoryItemCard(
                            item = item,
                            onReceiveStock = { quantity, reason ->
                                viewModel.receiveStock(item.id, quantity, reason)
                            },
                            onIssueStock = { quantity, reason ->
                                viewModel.issueStock(item.id, quantity, reason)
                            },
                            onDeleteItem = {
                                viewModel.deleteInventoryItem(item.id)
                            }
                        )
                    }
                }

                // Paginacja
                if (uiState.totalPages > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.previousPage() },
                            enabled = uiState.currentPage > 0
                        ) {
                            Text("Poprzednia")
                        }

                        Text("${uiState.currentPage + 1} / ${uiState.totalPages}")

                        Button(
                            onClick = { viewModel.nextPage() },
                            enabled = uiState.currentPage < uiState.totalPages - 1
                        ) {
                            Text("Następna")
                        }
                    }
                }
            }
        }
    }
}

// ... (reszta plików StatusFilterRow, InventoryItemCard, QuantityDialog bez zmian) ...
@Composable
fun StatusFilterRow(onStatusSelected: (InventoryStatus?) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                onClick = { onStatusSelected(null) },
                label = { Text("Wszystkie") },
                selected = false
            )
        }
        items(InventoryStatus.values()) { status ->
            FilterChip(
                onClick = { onStatusSelected(status) },
                label = { Text(status.name) },
                selected = false
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItemDTO,
    onReceiveStock: (BigDecimal, String?) -> Unit,
    onIssueStock: (BigDecimal, String?) -> Unit,
    onDeleteItem: () -> Unit
) {
    var showQuantityDialog by remember { mutableStateOf(false) }
    var isReceiving by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SKU: ${item.product.sku}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Lokalizacja: ${item.location.name}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ilość: ${item.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dostępne: ${item.availableQuantity}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Badge {
                        Text(item.status.name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (item.serialNumber != null) {
                Text(
                    text = "S/N: ${item.serialNumber}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (item.batchNumber != null) {
                Text(
                    text = "Partia: ${item.batchNumber}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isReceiving = true
                        showQuantityDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Przyjmij")
                }

                Button(
                    onClick = {
                        isReceiving = false
                        showQuantityDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Wydaj")
                }

                IconButton(onClick = onDeleteItem) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń")
                }
            }
        }
    }

    if (showQuantityDialog) {
        QuantityDialog(
            isReceiving = isReceiving,
            onConfirm = { quantity, reason ->
                if (isReceiving) {
                    onReceiveStock(quantity, reason)
                } else {
                    onIssueStock(quantity, reason)
                }
                showQuantityDialog = false
            },
            onDismiss = { showQuantityDialog = false }
        )
    }
}

@Composable
fun QuantityDialog(
    isReceiving: Boolean,
    onConfirm: (BigDecimal, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isReceiving) "Przyjmij towar" else "Wydaj towar")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Ilość") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Powód (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val qty = BigDecimal(quantity)
                        onConfirm(qty, reason.takeIf { it.isNotBlank() })
                    } catch (e: NumberFormatException) {
                        // Handle error
                    }
                },
                enabled = quantity.isNotBlank() && quantity.toBigDecimalOrNull() != null
            ) {
                Text("Potwierdź")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}