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

// Zakładamy istnienie tych klas w data/model, jeśli ich nie ma, należy je utworzyć
// data class OrderItemRequest(...)
// enum class OrderType { ... }
// data class AdminUserResponse(...)

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
    onDismiss: () -> Unit,
    onItemAdded: (OrderItemRequest) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<ProductDTO?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var showProductDialog by remember { mutableStateOf(false) }

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
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            if (selectedProduct!!.description?.isNotEmpty() == true) {
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
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                                onItemAdded(orderItem)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedProduct != null && (quantity.toIntOrNull() ?: 0) > 0
                    ) {
                        Text("Dodaj")
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
                                if (product.name?.isNotEmpty() == true) {
                                    Text(
                                        text = "Kategoria: ${product.name}",
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