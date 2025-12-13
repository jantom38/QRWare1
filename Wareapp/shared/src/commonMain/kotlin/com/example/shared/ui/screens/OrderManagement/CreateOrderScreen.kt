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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shared.data.model.*
import com.example.shared.ui.viewmodel.OrderManagement.CreateOrderViewModel
import com.example.shared.ui.viewmodel.OrderManagement.OrderItemUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    navController: NavController,
    viewModel: CreateOrderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var showUserDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationDialogType by remember { mutableStateOf<LocationDialogType?>(null) }
    var showOrderTypeDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.popBackStack()
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
                        onClick = { viewModel.submitOrder() },
                        enabled = uiState.selectedOrderType != null && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
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
            if (uiState.error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.error!!,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, contentDescription = "Zamknij")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Podstawowe informacje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.orderNumber,
                    onValueChange = viewModel::onOrderNumberChange,
                    label = { Text("Numer zamówienia (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Zostanie wygenerowany automatycznie jeśli pusty") }
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.selectedOrderType?.let { getOrderTypeDisplayName(it) } ?: "",
                    onValueChange = { },
                    label = { Text("Typ zamówienia *") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showOrderTypeDialog = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Wybierz")
                        }
                    },
                    isError = uiState.selectedOrderType == null
                )
            }

            item {
                OutlinedTextField(
                    value = getPriorityDisplayName(uiState.selectedPriority),
                    onValueChange = { },
                    label = { Text("Priorytet") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val nextPriority = when (uiState.selectedPriority) {
                                OrderPriority.LOW -> OrderPriority.NORMAL
                                OrderPriority.NORMAL -> OrderPriority.HIGH
                                OrderPriority.HIGH -> OrderPriority.URGENT
                                OrderPriority.URGENT -> OrderPriority.CRITICAL
                                OrderPriority.CRITICAL -> OrderPriority.LOW
                            }
                            viewModel.onPriorityChange(nextPriority)
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Zmień")
                        }
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            item {
                Text(
                    text = "Przypisanie i lokalizacje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.selectedAssignedUser?.let { "${it.firstName} ${it.lastName}".trim().ifEmpty { it.username } } ?: "",
                    onValueChange = { },
                    label = { Text("Przypisz do użytkownika") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (uiState.selectedAssignedUser != null) {
                                IconButton(onClick = { viewModel.onUserSelected(null) }) {
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
                    value = uiState.selectedSourceLocation?.name ?: "",
                    onValueChange = { },
                    label = { Text("Lokalizacja źródłowa") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (uiState.selectedSourceLocation != null) {
                                IconButton(onClick = { viewModel.onSourceLocationSelected(null) }) {
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
                    value = uiState.selectedDestinationLocation?.name ?: "",
                    onValueChange = { },
                    label = { Text("Lokalizacja docelowa") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (uiState.selectedDestinationLocation != null) {
                                IconButton(onClick = { viewModel.onDestinationLocationSelected(null) }) {
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

            item {
                Text(
                    text = "Dodatkowe informacje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.expectedDate,
                    onValueChange = viewModel::onExpectedDateChange,
                    label = { Text("Data oczekiwana (YYYY-MM-DD HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("2025-12-01 10:00") }
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.externalReference,
                    onValueChange = viewModel::onExternalRefChange,
                    label = { Text("Referencja zewnętrzna") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Notatki") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pozycje zamówienia (${uiState.orderItems.size})",
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

            items(uiState.orderItems) { item ->
                OrderItemCardUi(
                    item = item,
                    onRemove = { viewModel.removeOrderItem(item) }
                )
            }

            if (uiState.orderItems.isEmpty()) {
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

    if (showOrderTypeDialog) {
        OrderTypeDialog(
            onDismiss = { showOrderTypeDialog = false },
            onTypeSelected = { type ->
                viewModel.onTypeSelected(type)
                showOrderTypeDialog = false
            }
        )
    }

    if (showUserDialog) {
        UserSelectionDialog(
            users = uiState.users,
            onDismiss = { showUserDialog = false },
            onUserSelected = { user ->
                viewModel.onUserSelected(user)
                showUserDialog = false
            }
        )
    }

    if (showLocationDialog && locationDialogType != null) {
        LocationSelectionDialog(
            locations = uiState.locations,
            onDismiss = { showLocationDialog = false },
            onLocationSelected = { location ->
                when (locationDialogType) {
                    LocationDialogType.SOURCE -> viewModel.onSourceLocationSelected(location)
                    LocationDialogType.DESTINATION -> viewModel.onDestinationLocationSelected(location)
                    null -> { }
                }
                showLocationDialog = false
                locationDialogType = null
            }
        )
    }

    if (showAddItemDialog) {
        AddOrderItemDialog(
            products = uiState.products,
            availableInventory = uiState.availableInventory,
            sourceLocation = uiState.selectedSourceLocation,
            onDismiss = { showAddItemDialog = false },
            onItemAdded = { newItemUi ->
                viewModel.addOrderItem(
                    OrderItemUiModel(
                        productId = newItemUi.productId,
                        productName = newItemUi.productName,
                        requestedQuantity = newItemUi.requestedQuantity,
                        notes = newItemUi.notes,
                        requiresExactInventory = newItemUi.requiresExactInventory
                    )
                )
                showAddItemDialog = false
            },
            onRefreshInventory = { }
        )
    }
}

enum class LocationDialogType {
    SOURCE, DESTINATION
}

fun getPriorityDisplayName(priority: OrderPriority): String {
    return when (priority) {
        OrderPriority.LOW -> "Niski"
        OrderPriority.NORMAL -> "Normalny"
        OrderPriority.HIGH -> "Wysoki"
        OrderPriority.URGENT -> "Pilny"
        OrderPriority.CRITICAL -> "Krytyczny"
    }
}

@Composable
fun OrderItemCardUi(
    item: OrderItemUiModel,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.productName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = "Ilość: ${item.requestedQuantity}", style = MaterialTheme.typography.bodyMedium)
                if (!item.notes.isNullOrBlank()) {
                    Text(text = "Notatki: ${item.notes}", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}