package com.example.shared.ui.screens.InventoryManagement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shared.data.dto.InventoryItemDTO
import com.example.shared.data.model.InventoryStatus
import com.example.shared.ui.viewmodel.ProductsManagement.ManageInventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageInventoryScreen(
    navController: NavController,
    viewModel: ManageInventoryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // SZYBKI FIX dla uprawnień w UI:
    val hasFullPermissions = true

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            delay(3000)
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
                    IconButton(onClick = {
                        searchQuery = ""
                        viewModel.loadInventoryItems()
                    }) {
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
                .padding(horizontal = 16.dp)
        ) {
            // Wyszukiwarka
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.searchInventory(query)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                placeholder = { Text("Szukaj...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; viewModel.searchInventory("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (searchQuery.isEmpty()) {
                StatusFilterRow(
                    onStatusSelected = { status ->
                        if (status != null) viewModel.filterByStatus(status) else viewModel.loadInventoryItems()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Komunikaty
            uiState.error?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            // Lista
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                            onReceiveStock = { q, r -> viewModel.receiveStock(item.id, q, r) },
                            onIssueStock = { q, r -> viewModel.issueStock(item.id, q, r) },
                            onDeleteItem = { viewModel.deleteInventoryItem(item.id) },
                            onGenerateQRItem = { navController.navigate("generate_qr/INVENTORY_ITEM/${item.id}") },
                            onViewDetails = { navController.navigate("inventory_details/${item.id}") }
                        )
                    }
                }

                // Paginacja
                if (uiState.totalPages > 1 && searchQuery.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { viewModel.previousPage() }, enabled = uiState.currentPage > 0) { Text("Poprzednia") }
                        Text("${uiState.currentPage + 1} / ${uiState.totalPages}")
                        Button(onClick = { viewModel.nextPage() }, enabled = uiState.currentPage < uiState.totalPages - 1) { Text("Następna") }
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
    onReceiveStock: ((Int, String?) -> Unit)? = null,
    onIssueStock: ((Int, String?) -> Unit)? = null,
    onDeleteItem: (() -> Unit)? = null,
    onGenerateQRItem: (() -> Unit)? = null,
    onViewDetails: () -> Unit
) {
    var showQuantityDialog by remember { mutableStateOf(false) }
    var isReceiving by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
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

            // Dodatkowe informacje
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
                // Pobieramy dzisiejszą datę poprawnie
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toString()

                Text(
                    text = "🗓️ Wygasa: ${item.expiryDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.expiryDate < today)
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onReceiveStock != null && onIssueStock != null) {
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
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wydaj")
                    }
                } else {
                    if (onReceiveStock != null) {
                        Button(
                            onClick = {
                                isReceiving = true
                                showQuantityDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Przyjmij")
                        }
                    }

                    if (onIssueStock != null) {
                        Button(
                            onClick = {
                                isReceiving = false
                                showQuantityDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Wydaj")
                        }
                    }
                }

                onGenerateQRItem?.let { action ->
                    IconButton(onClick = action) {
                        Icon(Icons.Default.Search, contentDescription = "Generuj kod QR")
                    }
                }

                // POPRAWIONE: Usunięto odwołanie do viewModelu i przycisk paginacji z wnętrza kosza
                onDeleteItem?.let { action ->
                    IconButton(onClick = action) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń")
                    }
                }
            }
        }
    }

    if (showQuantityDialog && ((isReceiving && onReceiveStock != null) || (!isReceiving && onIssueStock != null))) {
        QuantityDialog(
            isReceiving = isReceiving,
            onConfirm = { quantity, reason ->
                if (isReceiving) {
                    onReceiveStock?.invoke(quantity, reason)
                } else {
                    onIssueStock?.invoke(quantity, reason)
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
                        // Obsługa błędu
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