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
import androidx.compose.ui.graphics.Color

// Data class for order item creation
data class OrderItemRequest(
    val productId: Long,
    val productName: String,
    val productSku: String,
    val requestedQuantity: Int,
    val notes: String? = null,
    val requiresExactInventory: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTypeDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (OrderType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wybierz typ zamówienia") },
        text = {
            // Ograniczamy wysokość listy, aby dialog nie był za duży na małych ekranach
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
                                // Obsługa nullable/pustych pól w AdminUserResponse
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
                                // POPRAWKA: Używamy pola 'code' z LocationDTO
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
                                // POPRAWKA: Używamy obiektu 'zone' z LocationDTO
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
    availableInventory: Map<Long, Int> = emptyMap(),
    sourceLocation: LocationDTO? = null,
    onDismiss: () -> Unit,
    onItemAdded: (OrderItemRequest) -> Unit,
    onRefreshInventory: () -> Unit = {}
) {
    var selectedProduct by remember { mutableStateOf<ProductDTO?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var showProductDialog by remember { mutableStateOf(false) }
    var requiresExactInventory by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Dodaj pozycję zamówienia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Product selection
                OutlinedTextField(
                    value = selectedProduct?.name ?: "",
                    onValueChange = { },
                    label = { Text("Produkt *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProductDialog = true }, // Umożliwia kliknięcie w pole tekstowe
                    readOnly = true,
                    enabled = false, // Wyłączamy edycję, ale kliknięcie obsługujemy wyżej lub przez ikonę
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showProductDialog = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Wybierz")
                        }
                    },
                    isError = selectedProduct == null
                )

                if (selectedProduct != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val availableQty = availableInventory[selectedProduct!!.id] ?: 0
                    val hasStock = availableQty > 0
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasStock) 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else 
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = selectedProduct!!.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SKU: ${selectedProduct!!.sku}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // Stock information
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    if (sourceLocation != null) {
                                        Text(
                                            text = "Dostępne w ${sourceLocation.name}:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$availableQty szt.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Text(
                                            text = "Wybierz lokalizację źródłową",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                if (sourceLocation != null) {
                                    TextButton(
                                        onClick = onRefreshInventory
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Odśwież",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Odśwież", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            
                            // Warning for no stock
                            if (!hasStock && sourceLocation != null && requiresExactInventory) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Ostrzeżenie",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Brak produktu w tej lokalizacji",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            if (selectedProduct!!.description?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedProduct!!.description!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        // Pozwalamy tylko na cyfry
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
                    isError = quantity.toIntOrNull() == null || (quantity.toIntOrNull() ?: 0) <= 0
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notatki (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fulfillment Type Section
                Text(
                    text = "Typ realizacji",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (requiresExactInventory) Color(0xFFFF9800).copy(alpha = 0.1f) else Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = !requiresExactInventory,
                                onCheckedChange = { requiresExactInventory = !it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF4CAF50),
                                    checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (requiresExactInventory) "Dokładny stan magazynowy" else "Elastyczna realizacja",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (requiresExactInventory) Color(0xFFFF9800) else Color(0xFF4CAF50)
                                )
                                Text(
                                    text = if (requiresExactInventory) 
                                        "Zamówienie musi być zrealizowane z dokładnie określonego stanu magazynowego" 
                                    else 
                                        "Zamówienie może być zrealizowane z dowolnej dostępnej lokalizacji",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
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
                            val product = selectedProduct
                            val qty = quantity.toIntOrNull()

                            if (product != null && qty != null && qty > 0) {
                                val orderItem = OrderItemRequest(
                                    productId = product.id,
                                    productName = product.name,
                                    productSku = product.sku,
                                    requestedQuantity = qty,
                                    notes = notes.takeIf { it.isNotBlank() },
                                    requiresExactInventory = requiresExactInventory
                                )
                                onItemAdded(orderItem)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedProduct != null && 
                                 (quantity.toIntOrNull() ?: 0) > 0 && 
                                 (if (requiresExactInventory && sourceLocation != null) {
                                     val availableQty = availableInventory[selectedProduct?.id] ?: 0
                                     val requestedQty = quantity.toIntOrNull() ?: 0
                                     availableQty >= requestedQty
                                 } else true)
                    ) {
                        val product = selectedProduct
                        val qty = quantity.toIntOrNull() ?: 0
                        val availableQty = if (product != null) availableInventory[product.id] ?: 0 else 0
                        
                        val isValid = if (requiresExactInventory && sourceLocation != null && product != null) {
                            availableQty >= qty
                        } else true
                        
                        Text(
                            if (!isValid) "Niewystarczający stan" 
                            else "Dodaj"
                        )
                    }
                }
            }
        }
    }

    // Product selection dialog
    if (showProductDialog) {
        ProductSelectionDialog(
            products = products,
            onDismiss = { showProductDialog = false },
            onProductSelected = { product ->
                selectedProduct = product
                showProductDialog = false
            }
        )
    }
}

@Composable
fun ProductSelectionDialog(
    products: List<ProductDTO>,
    onDismiss: () -> Unit,
    onProductSelected: (ProductDTO) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Proste filtrowanie po stronie klienta
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

                // Search
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

                // Products list
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

// Funkcja pomocnicza do tłumaczenia typów na język polski (przyjazna nazwa)
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
        OrderType.PICK -> "Kompletacja zamówienia"
        OrderType.PUTAWAY -> "Odkładanie towaru na miejsce"
        OrderType.CYCLE_COUNT -> "Inwentaryzacja ciągła"
        OrderType.REPLENISHMENT -> "Uzupełnienie zapasów"
        OrderType.RETURN -> "Zwrot towaru"
        OrderType.ADJUSTMENT -> "Korekta stanu magazynowego"
        OrderType.MAINTENANCE -> "Konserwacja/przegląd"
        OrderType.QUALITY_CHECK -> "Kontrola jakości"
    }
}