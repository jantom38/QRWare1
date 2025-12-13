package com.qrware.app.ui.screens.InventoryManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
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
import com.qrware.app.data.model.InventoryStatus
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.ProductsManagement.InventoryDetailsViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailsScreen(
    navController: NavController,
    appContainer: AppContainer,
    itemId: Long
) {
    val viewModel: InventoryDetailsViewModel = viewModel(
        factory = appContainer.createInventoryDetailsViewModelFactory(itemId)
    )
    val uiState by viewModel.uiState.collectAsState()

    var showQuantityDialog by remember { mutableStateOf(false) }
    var isReceiving by remember { mutableStateOf(true) }

    // Auto-clear messages after 3 seconds
    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły pozycji magazynowej") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
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
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Błąd: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshData() }) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
                uiState.inventoryItem != null -> {
                    InventoryDetailsContent(
                        inventoryItem = uiState.inventoryItem!!,
                        isUpdating = uiState.isUpdating,
                        successMessage = uiState.successMessage,
                        onReceiveStock = {
                            isReceiving = true
                            showQuantityDialog = true
                        },
                        onIssueStock = {
                            isReceiving = false
                            showQuantityDialog = true
                        },
                        onGenerateQR = {
                            navController.navigate("generate_qr/INVENTORY_ITEM/${itemId}")
                        },
                        navController = navController
                    )
                }
            }

            // Quantity Dialog
            if (showQuantityDialog) {
                QuantityDialog(
                    isReceiving = isReceiving,
                    onConfirm = { quantity, reason ->
                        if (isReceiving) {
                            viewModel.receiveStock(quantity, reason)
                        } else {
                            viewModel.issueStock(quantity, reason)
                        }
                        showQuantityDialog = false
                    },
                    onDismiss = { showQuantityDialog = false }
                )
            }

            // Loading overlay during updates
            if (uiState.isUpdating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aktualizacja stanu...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryDetailsContent(
    inventoryItem: InventoryItemDTO,
    isUpdating: Boolean,
    successMessage: String?,
    onReceiveStock: () -> Unit,
    onIssueStock: () -> Unit,
    onGenerateQR: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Success message
        if (successMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = successMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main product info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = inventoryItem.product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SKU: ${inventoryItem.product.sku}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (inventoryItem.product.description?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = inventoryItem.product.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quantity and status info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ilość całkowita",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${inventoryItem.quantity}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dostępne",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${inventoryItem.availableQuantity}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Zarezerwowane",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${inventoryItem.reservedQuantity}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodySmall
                    )
                    StatusBadge(status = inventoryItem.status)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location info
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Lokalizacja",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nazwa: ${inventoryItem.location.name}")
                Text("Kod: ${inventoryItem.location.code}")
                if (inventoryItem.location.description?.isNotBlank() == true) {
                    Text("Opis: ${inventoryItem.location.description}")
                }
                Text("Strefa: ${inventoryItem.location.zone.name} (${inventoryItem.location.zone.type.name})")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Additional details
        InventoryAdditionalDetails(inventoryItem = inventoryItem)

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onReceiveStock,
                modifier = Modifier.weight(1f),
                enabled = !isUpdating
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Przyjmij")
            }
            Button(
                onClick = onIssueStock,
                modifier = Modifier.weight(1f),
                enabled = !isUpdating && inventoryItem.availableQuantity > 0
            ) {
                Icon(Icons.Default.Remove, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Wydaj")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGenerateQR,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUpdating
        ) {
            Icon(Icons.Default.QrCode, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generuj kod QR")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("movement_history/item/${inventoryItem.id}")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUpdating
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Historia ruchów")
        }
    }
}

@Composable
fun InventoryAdditionalDetails(inventoryItem: InventoryItemDTO) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Szczegóły",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            inventoryItem.serialNumber?.let {
                DetailRow("Numer seryjny", it)
            }
            inventoryItem.batchNumber?.let {
                DetailRow("Numer partii", it)
            }
            inventoryItem.lotNumber?.let {
                DetailRow("Numer lotu", it)
            }
            inventoryItem.receivedDate?.let {
                DetailRow("Data przyjęcia", it)
            }
            inventoryItem.expiryDate?.let { expiryDate ->
                DetailRow(
                    "Data ważności", 
                    expiryDate,
                    isExpired = expiryDate < LocalDate.now().toString()
                )
            }
            inventoryItem.manufactureDate?.let {
                DetailRow("Data produkcji", it)
            }
            inventoryItem.lastCountedDate?.let {
                DetailRow("Ostatnie liczenie", it)
            }
            inventoryItem.lastMovedDate?.let {
                DetailRow("Ostatni ruch", it)
            }
            inventoryItem.unitCost?.let {
                DetailRow("Koszt jednostkowy", "$it")
            }
            inventoryItem.totalCost?.let {
                DetailRow("Koszt całkowity", "$it")
            }
            inventoryItem.supplierReference?.let {
                DetailRow("Ref. dostawcy", it)
            }
            inventoryItem.purchaseOrderNumber?.let {
                DetailRow("Nr zamówienia", it)
            }
            inventoryItem.conditionRating?.let {
                DetailRow("Ocena stanu", "$it/10")
            }
            if (inventoryItem.temperature != null || inventoryItem.humidity != null) {
                DetailRow(
                    "Warunki", 
                    "${inventoryItem.temperature ?: "?"}°C, ${inventoryItem.humidity ?: "?"}%"
                )
            }
            inventoryItem.notes?.let {
                if (it.isNotBlank()) {
                    DetailRow("Notatki", it)
                }
            }

            // Warnings
            if (inventoryItem.quarantine == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "🚫 KWARANTANNA: ${inventoryItem.quarantineReason ?: "Bez podania powodu"}",
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (inventoryItem.hold == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = "⏸️ BLOKADA: ${inventoryItem.holdReason ?: "Bez podania powodu"}",
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isExpired: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isExpired) MaterialTheme.colorScheme.error else Color.Unspecified
        )
    }
}

@Composable
fun StatusBadge(status: InventoryStatus) {
    val (backgroundColor, textColor) = when (status) {
        InventoryStatus.AVAILABLE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        InventoryStatus.RESERVED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        InventoryStatus.UNAVAILABLE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        InventoryStatus.ON_HOLD -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        InventoryStatus.QUARANTINE -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        InventoryStatus.DAMAGED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        InventoryStatus.EXPIRED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = when (status) {
                InventoryStatus.AVAILABLE -> "Dostępny"
                InventoryStatus.RESERVED -> "Zarezerwowany"
                InventoryStatus.UNAVAILABLE -> "Niedostępny"
                InventoryStatus.ON_HOLD -> "Wstrzymany"
                InventoryStatus.QUARANTINE -> "Kwarantanna"
                InventoryStatus.DAMAGED -> "Uszkodzony"
                InventoryStatus.EXPIRED -> "Przeterminowany"
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}