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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.qrware.app.data.model.*
import com.qrware.app.data.dto.LocationDTO
import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.data.dto.InventoryItemDTO
import androidx.compose.ui.graphics.Color

data class OrderItemRequest(
    val productId: Long,
    val productName: String = "",
    val productSku: String = "",
    val requestedQuantity: Int,
    val notes: String? = null,
    val requiresExactInventory: Boolean = true,
    val sourceLocationId: Long? = null,
    val destinationLocationId: Long? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTypeDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (OrderType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wybierz typ zlecenia") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                LazyColumn {
                    items(OrderType.entries) { type ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onTypeSelected(type) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = getOrderTypeDisplayName(type),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = getOrderTypeDescription(type),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun UserSelectionDialog(
    users: List<AdminUserResponse>,
    onDismiss: () -> Unit,
    onUserSelected: (AdminUserResponse) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Wybierz użytkownika",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserSelected(user) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                val displayName = if (user.firstName?.isNotBlank() == true || user.lastName?.isNotBlank() == true) {
                                    "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                                } else {
                                    user.username
                                }

                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (user.roles.isNotEmpty()) {
                                    Text(
                                        text = "Role: ${user.roles.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj")
                    }
                }
            }
        }
    }
}

@Composable
fun LocationSelectionDialog(
    locations: List<LocationDTO>,
    onDismiss: () -> Unit,
    onLocationSelected: (LocationDTO) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Wybierz lokalizację",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(locations) { location ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocationSelected(location) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = location.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (location.code.isNotEmpty()) {
                                    Text(
                                        text = "Kod: ${location.code}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (location.description?.isNotEmpty() == true) {
                                    Text(
                                        text = location.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Strefa: ${location.zone.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj")
                    }
                }
            }
        }
    }
}

@Composable
fun AddOrderItemDialog(
    products: List<ProductDTO>,
    locations: List<LocationDTO>,
    inventory: List<InventoryItemDTO>,
    isLoadingInventory: Boolean,
    defaultSourceLocation: LocationDTO? = null,
    defaultDestinationLocation: LocationDTO? = null,
    onDismiss: () -> Unit,
    onItemAdded: (OrderItemRequest) -> Unit,
    onFetchInventoryForProduct: (Long) -> Unit,
    onFetchInventoryForLocation: (Long) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<ProductDTO?>(null) }
    var selectedInventoryItem by remember { mutableStateOf<InventoryItemDTO?>(null) }
    var requiresExactInventory by remember { mutableStateOf(false) }
    
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var showProductDialog by remember { mutableStateOf(true) } // Start with product selection

    // If product is not selected, show product selection dialog
    if (selectedProduct == null) {
        if (showProductDialog) {
            ProductSelectionDialog(
                products = products,
                onDismiss = onDismiss, // Dismissing product selection closes the whole dialog
                onProductSelected = { product ->
                    selectedProduct = product
                    showProductDialog = false
                    onFetchInventoryForProduct(product.id)
                    selectedInventoryItem = null
                    requiresExactInventory = false // Reset to default
                }
            )
        }
        return
    }

    // Main dialog content once product is selected
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .imePadding()
            ) {
                Text(
                    text = "Dodaj pozycję zlecenia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selected Product Info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Wybrany produkt:",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = selectedProduct!!.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SKU: ${selectedProduct!!.sku}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            onClick = { 
                                selectedProduct = null
                                showProductDialog = true 
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Zmień produkt")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Exact Inventory Toggle
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { requiresExactInventory = !requiresExactInventory },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = requiresExactInventory,
                                onCheckedChange = { requiresExactInventory = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Wymagana dokładna lokalizacja",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (requiresExactInventory) 
                                        "Musisz wybrać konkretną partię/lokalizację z listy poniżej" 
                                    else 
                                        "System automatycznie dobierze lokalizację",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Inventory Selection (only if exact inventory required)
                    if (requiresExactInventory) {
                        if (isLoadingInventory) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else if (inventory.isEmpty()) {
                            item {
                                Text(
                                    text = "Brak dostępnych stanów magazynowych dla tego produktu.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            item {
                                Text("Wybierz lokalizację źródłową:", style = MaterialTheme.typography.titleSmall)
                            }
                            items(inventory) { item ->
                                InventoryItemSelectionCard(
                                    item = item,
                                    isSelected = selectedInventoryItem?.id == item.id,
                                    onClick = { selectedInventoryItem = item }
                                )
                            }
                        }
                    } else {
                        // If exact inventory is NOT required, we can still show default locations if they are set
                        if (defaultSourceLocation != null) {
                            item {
                                Text(
                                    text = "Domyślna lokalizacja źródłowa: ${defaultSourceLocation.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (defaultDestinationLocation != null) {
                            item {
                                Text(
                                    text = "Domyślna lokalizacja docelowa: ${defaultDestinationLocation.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                    }

                    // Quantity Input
                    item {
                        val maxQuantity = if (requiresExactInventory && selectedInventoryItem != null) {
                            selectedInventoryItem!!.availableQuantity
                        } else {
                            Int.MAX_VALUE // Or total available across all locations if we had that info easily
                        }

                        OutlinedTextField(
                            value = quantity,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    quantity = it
                                }
                            },
                            label = { Text("Ilość *") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            isError = quantity.toIntOrNull() == null || 
                                     (quantity.toIntOrNull() ?: 0) <= 0 || 
                                     (requiresExactInventory && selectedInventoryItem != null && (quantity.toIntOrNull() ?: 0) > maxQuantity),
                            supportingText = if (requiresExactInventory && selectedInventoryItem != null) {
                                { Text("Dostępne: $maxQuantity") }
                            } else null
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notatki (opcjonalnie)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anuluj")
                    }

                    Button(
                        onClick = {
                            val qty = quantity.toIntOrNull()
                            
                            if (qty != null && qty > 0) {
                                // Logic for source location:
                                // 1. If exact inventory is required, use the selected inventory item's location.
                                // 2. If exact inventory is NOT required, use the default source location (if provided).
                                val sourceLocId = if (requiresExactInventory) {
                                    selectedInventoryItem?.location?.id
                                } else {
                                    defaultSourceLocation?.id
                                }

                                // Logic for destination location:
                                // Use default destination location if provided (regardless of exact inventory setting, 
                                // unless we want to allow overriding it per item, but for now let's stick to defaults).
                                val destLocId = defaultDestinationLocation?.id

                                val orderItem = OrderItemRequest(
                                    productId = selectedProduct!!.id,
                                    productName = selectedProduct!!.name,
                                    productSku = selectedProduct!!.sku,
                                    requestedQuantity = qty,
                                    notes = notes.takeIf { it.isNotBlank() },
                                    requiresExactInventory = requiresExactInventory,
                                    sourceLocationId = sourceLocId,
                                    destinationLocationId = destLocId
                                )
                                onItemAdded(orderItem)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = (quantity.toIntOrNull() ?: 0) > 0 && 
                                 (!requiresExactInventory || selectedInventoryItem != null) &&
                                 (!requiresExactInventory || (selectedInventoryItem != null && (quantity.toIntOrNull() ?: 0) <= selectedInventoryItem!!.availableQuantity))
                    ) {
                        Text("Dodaj")
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemSelectionCard(
    item: InventoryItemDTO,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.location.name,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.availableQuantity} szt.",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Kod: ${item.location.code}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun InventoryProductSelectionCard(
    item: InventoryItemDTO,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.availableQuantity} szt.",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "SKU: ${item.product.sku}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ProductSelectionDialog(
    products: List<ProductDTO>,
    onDismiss: () -> Unit,
    onProductSelected: (ProductDTO) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter { product ->
                product.name.contains(searchQuery, ignoreCase = true) ||
                        product.sku.contains(searchQuery, ignoreCase = true) ||
                        product.description?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Wybierz produkt",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Szukaj produktu...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Szukaj")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductSelected(product) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SKU: ${product.sku}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (product.description?.isNotEmpty() == true) {
                                    Text(
                                        text = product.description!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (product.category?.name?.isNotEmpty() == true) {
                                    Text(
                                        text = "Kategoria: ${product.category.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Brak produktów" else "Nie znaleziono produktów",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj")
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(
    item: OrderItemRequest,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SKU: ${item.productSku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ilość: ${item.requestedQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (item.requiresExactInventory) {
                     Text(
                        text = "Wymagana dokładna lokalizacja",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (item.sourceLocationId != null) {
                    Text(
                        text = "Lokalizacja źródłowa ID: ${item.sourceLocationId}", // Ideally we would show name, but we only have ID here.
                        // To show name we would need to pass it or look it up.
                        // For now, let's assume the user knows or we can improve this later.
                        // Actually, we can add locationName to OrderItemRequest.
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (item.notes?.isNotEmpty() == true) {
                    Text(
                        text = "Notatki: ${item.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Usuń",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun getOrderTypeDisplayName(type: OrderType): String {
    return when (type) {
        OrderType.INBOUND -> "Przyjęcie"
        OrderType.OUTBOUND -> "Wydanie"
        OrderType.TRANSFER -> "Przesunięcie"
        OrderType.PICK -> "Kompletacja"
        OrderType.PUTAWAY -> "Rozkładanie"
        OrderType.CYCLE_COUNT -> "Inwentaryzacja"
        OrderType.REPLENISHMENT -> "Uzupełnienie"
        OrderType.RETURN -> "Zwrot"
        OrderType.ADJUSTMENT -> "Korekta"
        OrderType.MAINTENANCE -> "Konserwacja"
        OrderType.QUALITY_CHECK -> "Kontrola Jakości"
    }
}

fun getOrderTypeDescription(type: OrderType): String {
    return when (type) {
        OrderType.INBOUND -> "Przyjęcie towaru do magazynu"
        OrderType.OUTBOUND -> "Wydanie towaru z magazynu"
        OrderType.TRANSFER -> "Przeniesienie między lokalizacjami"
        OrderType.PICK -> "Kompletacja zlecenia"
        OrderType.PUTAWAY -> "Odkładanie towaru na miejsce"
        OrderType.CYCLE_COUNT -> "Inwentaryzacja ciągła"
        OrderType.REPLENISHMENT -> "Uzupełnienie zapasów"
        OrderType.RETURN -> "Zwrot towaru"
        OrderType.ADJUSTMENT -> "Korekta stanu magazynowego"
        OrderType.MAINTENANCE -> "Konserwacja/przegląd"
        OrderType.QUALITY_CHECK -> "Kontrola jakości"
    }
}