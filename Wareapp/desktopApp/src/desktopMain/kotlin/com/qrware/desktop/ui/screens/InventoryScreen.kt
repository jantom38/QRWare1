package com.qrware.desktop.ui.screens

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qrware.shared.data.model.InventoryItem
import com.qrware.shared.data.model.InventoryStatus
import com.qrware.shared.di.NetworkDI
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBack: () -> Unit
) {
    val inventoryRepository = remember { NetworkDI.getInventoryRepository() }
    val inventory by inventoryRepository.inventoryState.collectAsState()
    val locations by inventoryRepository.locationsState.collectAsState()
    val zones by inventoryRepository.zonesState.collectAsState()
    val isLoading by inventoryRepository.isLoading.collectAsState()
    val errorMessage by inventoryRepository.errorMessage.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<InventoryStatus?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedInventoryItem by remember { mutableStateOf<InventoryItem?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Load data on first launch
    LaunchedEffect(Unit) {
        scope.launch {
            inventoryRepository.loadAllInventory()
            // TODO: Check if these endpoints exist in backend
            // inventoryRepository.loadAllLocations()
            // inventoryRepository.loadAllZones()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            scope.launch {
                                inventoryRepository.refreshData()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Inventory")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search and Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        scope.launch {
                            if (it.isBlank()) {
                                inventoryRepository.loadAllInventory()
                            } else {
                                inventoryRepository.searchInventory(it)
                            }
                        }
                    },
                    label = { Text("Search inventory...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { 
                                    searchQuery = ""
                                    scope.launch {
                                        inventoryRepository.loadAllInventory()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // Status Filter
                var statusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedStatus?.name ?: "All Statuses",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .width(180.dp)
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Statuses") },
                            onClick = {
                                selectedStatus = null
                                statusExpanded = false
                                scope.launch {
                                    inventoryRepository.loadAllInventory()
                                }
                            }
                        )
                        InventoryStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name) },
                                onClick = {
                                    selectedStatus = status
                                    statusExpanded = false
                                    scope.launch {
                                        inventoryRepository.getInventoryByStatus(status)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(
                    title = "Total Items",
                    value = inventory.size.toString(),
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f)
                )
                
                StatsCard(
                    title = "Available",
                    value = inventory.count { it.status == InventoryStatus.AVAILABLE }.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color.Green,
                    modifier = Modifier.weight(1f)
                )
                
                StatsCard(
                    title = "Reserved",
                    value = inventory.count { it.status == InventoryStatus.RESERVED }.toString(),
                    icon = Icons.Default.Schedule,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                
                StatsCard(
                    title = "Locations",
                    value = locations.size.toString(),
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Error Message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { inventoryRepository.clearError() }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Inventory List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading inventory...")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(inventory) { inventoryItem ->
                        InventoryCard(
                            inventoryItem = inventoryItem,
                            onClick = { selectedInventoryItem = inventoryItem },
                            onUpdateQuantity = { newQuantity ->
                                scope.launch {
                                    inventoryRepository.updateQuantity(
                                        inventoryId = inventoryItem.id,
                                        newQuantity = newQuantity
                                    )
                                }
                            }
                        )
                    }
                    
                    if (inventory.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Inventory,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No inventory items found",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (searchQuery.isNotEmpty() || selectedStatus != null) {
                                        Text(
                                            "Try adjusting your filters",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add Inventory Dialog
    if (showAddDialog) {
        // TODO: AddInventoryDialog
        showAddDialog = false
    }
    
    // Inventory Details Dialog
    selectedInventoryItem?.let { item ->
        // TODO: InventoryDetailsDialog
        selectedInventoryItem = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryCard(
    inventoryItem: InventoryItem,
    onClick: () -> Unit,
    onUpdateQuantity: (Int) -> Unit
) {
    var showQuantityDialog by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = inventoryItem.product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = inventoryItem.status)
                }
                
                Text(
                    text = "QR: ${inventoryItem.qrCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Location: ${inventoryItem.location.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity: ${inventoryItem.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (inventoryItem.reservedQuantity > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Reserved: ${inventoryItem.reservedQuantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Available: ${inventoryItem.availableQuantity}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Green
                    )
                }
                
                inventoryItem.lotNumber?.let { lot ->
                    Text(
                        text = "Lot: $lot",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = { showQuantityDialog = true }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Quantity")
                }
            }
        }
    }
    
    // Quantity Update Dialog
    if (showQuantityDialog) {
        var newQuantity by remember { mutableStateOf(inventoryItem.quantity.toString()) }
        var quantityError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showQuantityDialog = false },
            title = { Text("Update Quantity") },
            text = {
                Column {
                    Text("Current quantity: ${inventoryItem.quantity}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newQuantity,
                        onValueChange = { 
                            newQuantity = it
                            quantityError = null
                        },
                        label = { Text("New Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = quantityError != null,
                        supportingText = {
                            quantityError?.let { 
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val quantity = newQuantity.toInt()
                            if (quantity < 0) {
                                quantityError = "Quantity cannot be negative"
                            } else {
                                onUpdateQuantity(quantity)
                                showQuantityDialog = false
                            }
                        } catch (e: NumberFormatException) {
                            quantityError = "Invalid quantity"
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuantityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusBadge(status: InventoryStatus) {
    val backgroundColor = when (status) {
        InventoryStatus.AVAILABLE -> Color(0xFF4CAF50)
        InventoryStatus.RESERVED -> Color(0xFFFF9800)
        InventoryStatus.UNAVAILABLE -> Color(0xFF9E9E9E)
        InventoryStatus.ON_HOLD -> Color(0xFF2196F3)
        InventoryStatus.QUARANTINE -> Color(0xFFF44336)
        InventoryStatus.DAMAGED -> Color(0xFFF44336)
        InventoryStatus.EXPIRED -> Color(0xFFF44336)
    }
    val contentColor = Color.White
    
    Badge(
        containerColor = backgroundColor,
        contentColor = contentColor
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}