package com.qrware.app.ui.screens.HistoryManagement

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.dto.MovementHistoryDTO
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.MovementHistoryUiState
import com.qrware.app.ui.viewmodel.MovementHistoryView
import com.qrware.app.ui.viewmodel.MovementHistoryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementHistoryScreen(
    navController: NavController,
    appContainer: AppContainer,
    itemId: Long? = null,
    productId: Long? = null,
    locationId: Long? = null
) {
    val viewModel: MovementHistoryViewModel = viewModel(
        factory = appContainer.movementHistoryViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()

    var showFiltersDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showApprovalDialog by remember { mutableStateOf(false) }
    var selectedMovementForApproval by remember { mutableStateOf<MovementHistoryDTO?>(null) }

    // Auto-clear messages after 3 seconds
    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    // Load specific data based on provided IDs
    LaunchedEffect(itemId, productId, locationId) {
        when {
            itemId != null -> {
                viewModel.setCurrentView(MovementHistoryView.ITEM_SPECIFIC)
                viewModel.loadMovementsByItemId(itemId)
            }
            productId != null -> {
                viewModel.setCurrentView(MovementHistoryView.PRODUCT_SPECIFIC)
                viewModel.loadMovementsByProductId(productId)
            }
            locationId != null -> {
                viewModel.setCurrentView(MovementHistoryView.LOCATION_SPECIFIC)
                viewModel.loadMovementsByLocationId(locationId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (uiState.currentView) {
                            MovementHistoryView.RECENT -> "Historia ruchów"
                            MovementHistoryView.PENDING_APPROVAL -> "Oczekujące zatwierdzenia"
                            MovementHistoryView.INBOUND -> "Ruchy przychodzące"
                            MovementHistoryView.OUTBOUND -> "Ruchy wychodzące"
                            MovementHistoryView.ADJUSTMENTS -> "Korekty"
                            MovementHistoryView.ITEM_SPECIFIC -> "Historia pozycji"
                            MovementHistoryView.LOCATION_SPECIFIC -> "Historia lokalizacji"
                            MovementHistoryView.PRODUCT_SPECIFIC -> "Historia produktu"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Szukaj")
                    }
                    IconButton(onClick = { showFiltersDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtry")
                    }
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
            Column {
                // View selector (only show if not viewing specific item/location/product)
                if (itemId == null && productId == null && locationId == null) {
                    ViewSelector(
                        currentView = uiState.currentView,
                        onViewSelected = { view ->
                            viewModel.setCurrentView(view)
                            when (view) {
                                MovementHistoryView.RECENT -> viewModel.loadRecentMovements()
                                MovementHistoryView.PENDING_APPROVAL -> viewModel.loadPendingApprovalMovements()
                                MovementHistoryView.INBOUND -> viewModel.loadInboundMovements()
                                MovementHistoryView.OUTBOUND -> viewModel.loadOutboundMovements()
                                MovementHistoryView.ADJUSTMENTS -> viewModel.loadAdjustmentMovements()
                                else -> {}
                            }
                        }
                    )
                }

                // Active filters indicator
                if (uiState.selectedMovementType != null || 
                    uiState.selectedStartDate != null || 
                    uiState.selectedEndDate != null ||
                    uiState.showPendingApprovalOnly ||
                    uiState.searchQuery.isNotBlank()) {
                    
                    ActiveFiltersIndicator(
                        uiState = uiState,
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }

                // Success/Error messages
                uiState.successMessage?.let { message ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                uiState.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Content
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Ładowanie historii ruchów...")
                            }
                        }
                    }
                    uiState.filteredMovements.isEmpty() && !uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Brak ruchów do wyświetlenia",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Spróbuj zmienić filtry lub zakres dat",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.filteredMovements) { movement ->
                                MovementHistoryCard(
                                    movement = movement,
                                    onApprove = if (movement.isApprovalPending()) {
                                        {
                                            selectedMovementForApproval = movement
                                            showApprovalDialog = true
                                        }
                                    } else null,
                                    onViewDetails = {
                                        // Navigate to movement details if needed
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Loading overlay for processing
            if (uiState.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Przetwarzanie...")
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showFiltersDialog) {
        FiltersDialog(
            uiState = uiState,
            onDismiss = { showFiltersDialog = false },
            onApplyFilters = { type, startDate, endDate, pendingOnly ->
                viewModel.filterByMovementType(type)
                viewModel.filterByDateRange(startDate, endDate)
                viewModel.filterByApprovalStatus(pendingOnly)
                showFiltersDialog = false
            }
        )
    }

    if (showSearchDialog) {
        SearchDialog(
            currentQuery = uiState.searchQuery,
            onDismiss = { showSearchDialog = false },
            onSearch = { query, searchIn ->
                viewModel.searchMovements(query, searchIn)
                showSearchDialog = false
            }
        )
    }

    if (showApprovalDialog && selectedMovementForApproval != null) {
        ApprovalDialog(
            movement = selectedMovementForApproval!!,
            onDismiss = { 
                showApprovalDialog = false
                selectedMovementForApproval = null
            },
            onApprove = { comment ->
                viewModel.approveMovement(selectedMovementForApproval!!.id, comment)
                showApprovalDialog = false
                selectedMovementForApproval = null
            }
        )
    }
}

@Composable
fun ViewSelector(
    currentView: MovementHistoryView,
    onViewSelected: (MovementHistoryView) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            listOf(
                MovementHistoryView.RECENT to "Ostatnie",
                MovementHistoryView.PENDING_APPROVAL to "Do zatwierdzenia",
                MovementHistoryView.INBOUND to "Przyjęcia",
                MovementHistoryView.OUTBOUND to "Wydania",
                MovementHistoryView.ADJUSTMENTS to "Korekty"
            )
        ) { (view, label) ->
            FilterChip(
                onClick = { onViewSelected(view) },
                label = { Text(label) },
                selected = currentView == view
            )
        }
    }
}

@Composable
fun ActiveFiltersIndicator(
    uiState: MovementHistoryUiState,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Aktywne filtry:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearFilters) {
                    Text("Wyczyść wszystkie")
                }
            }
            
            val filters = mutableListOf<String>()
            
            uiState.selectedMovementType?.let { 
                filters.add("Typ: ${it.displayName}") 
            }
            if (uiState.selectedStartDate != null || uiState.selectedEndDate != null) {
                val dateRange = "${uiState.selectedStartDate ?: "?"} - ${uiState.selectedEndDate ?: "?"}"
                filters.add("Data: $dateRange")
            }
            if (uiState.showPendingApprovalOnly) {
                filters.add("Oczekujące zatwierdzenia")
            }
            if (uiState.searchQuery.isNotBlank()) {
                filters.add("Szukane: \"${uiState.searchQuery}\"")
            }
            
            Text(
                filters.joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MovementHistoryCard(
    movement: MovementHistoryDTO,
    onApprove: (() -> Unit)? = null,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with movement type and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = movement.getMovementIcon(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = movement.movementType?.displayName ?: "Ruch magazynowy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (movement.isApprovalPending()) {
                    AssistChip(
                        onClick = { onApprove?.invoke() },
                        label = { Text("Zatwierdź", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product and quantity information
            Text(
                text = movement.inventoryItem.product.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "SKU: ${movement.inventoryItem.product.sku}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (movement.isQuantityChange()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Zmiana ilości: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = movement.getQuantityChangeText(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = movement.getQuantityChangeColor()
                    )
                    if (movement.quantityBefore != null && movement.quantityAfter != null) {
                        Text(
                            text = " (${movement.quantityBefore} → ${movement.quantityAfter})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Location information
            if (movement.isLocationChange() && movement.fromLocation != null && movement.toLocation != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lokalizacja: ${movement.fromLocation.code} → ${movement.toLocation.code}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (movement.inventoryItem.location.code.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lokalizacja: ${movement.inventoryItem.location.code}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Reason
            movement.reason?.let { reason ->
                if (reason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Powód: $reason",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Footer with date and user
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = movement.getFormattedMovementDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    movement.userName?.let { userName ->
                        Text(
                            text = "Użytkownik: $userName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (movement.systemGenerated) {
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    "System", 
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    
                    if (movement.approved) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Zatwierdzone",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}