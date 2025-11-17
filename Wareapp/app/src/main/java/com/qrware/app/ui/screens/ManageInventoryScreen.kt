package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.dto.InventoryItemDTO // <-- ZMIANA IMPORTU
import com.qrware.app.data.model.InventoryStatus
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.ManageInventoryViewModel
import java.math.BigDecimal
import kotlin.Int

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageInventoryScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val viewModel: ManageInventoryViewModel = viewModel(
        factory = appContainer.InventoryViewModelFactory
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
                title = { Text("Stan magazynowy") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
                    modifier = Modifier.weight(1f) // Dodaj wagę, aby paginacja była na dole
                ) {
                    // uiState.inventoryItems to teraz List<InventoryItemDTO>
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
                            },
                            onGenerateQRItem = {
                                navController.navigate("generate_qr/INVENTORY_ITEM/${item.id}")
                            }
                        )
                    }
                }

                // Paginacja
                if (uiState.totalPages > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp), // Zmiana z padding(16.dp)
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

@Composable
fun StatusFilterRow(onStatusSelected: (InventoryStatus?) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                onClick = { onStatusSelected(null) },
                label = { Text("Wszystkie") },
                selected = false // Możesz dodać logikę do śledzenia wybranego
            )
        }
        items(InventoryStatus.values()) { status ->
            FilterChip(
                onClick = { onStatusSelected(status) },
                label = { Text(status.name) },
                selected = false // Możesz dodać logikę do śledzenia wybranego
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItemDTO,
    onReceiveStock: (Int, String?) -> Unit,
    onIssueStock: (Int, String?) -> Unit,
    onDeleteItem: () -> Unit,
    onGenerateQRItem: () -> Unit // Dodany parametr dla generowania QR
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
                        text = item.product.name, // Dostęp do ProductDTO
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SKU: ${item.product.sku}", // Dostęp do ProductDTO
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Lokalizacja: ${item.location.name}", // Dostęp do LocationDTO
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

            // Dodatkowe informacje w kompaktowym formacie
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

            if (item.lotNumber != null) {
                Text(
                    text = "Lot: ${item.lotNumber}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (item.expiryDate != null) {
                Text(
                    text = "🗓️ Wygasa: ${item.expiryDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.expiryDate < java.time.LocalDate.now().toString()) 
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }

            if (item.quarantine == true) {
                Text(
                    text = "🚫 KWARANTANNA: ${item.quarantineReason ?: "Bez podania powodu"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (item.hold == true) {
                Text(
                    text = "⏸️ BLOKADA: ${item.holdReason ?: "Bez podania powodu"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (item.conditionRating != null && item.conditionRating < 8) {
                Text(
                    text = "⚠️ Stan: ${item.conditionRating}/10",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (item.temperature != null || item.humidity != null) {
                Text(
                    text = "🌡️ ${item.temperature ?: "?"}°C 💧 ${item.humidity ?: "?"}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (item.unitCost != null) {
                    Text(
                        text = "💰 ${item.unitCost}/szt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (item.totalCost != null) {
                    Text(
                        text = "Σ ${item.totalCost}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
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

                IconButton(onClick = onGenerateQRItem) {
                    Icon(Icons.Default.QrCode, contentDescription = "Generuj kod QR")
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
    onConfirm: (Int, String?) -> Unit,
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
                        val qty = quantity.toInt()
                        onConfirm(qty, reason.takeIf { it.isNotBlank() })
                    } catch (e: NumberFormatException) {
                        // Handle error (np. pokaż błąd walidacji)
                    }
                },
                enabled = quantity.isNotBlank() && quantity.toIntOrNull() != null
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