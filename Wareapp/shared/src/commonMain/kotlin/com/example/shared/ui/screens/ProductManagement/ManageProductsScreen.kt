package com.example.shared.ui.screens.ProductManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shared.data.dto.ProductDTO
import com.example.shared.ui.viewmodel.ProductsManagement.ManageProductsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(
    navController: NavController,
    viewModel: ManageProductsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // SZYBKI FIX dla uprawnień: Zakładamy, że Admin ma wszystko.
    // W prawdziwej aplikacji te dane powinny przychodzić z ViewModela lub AuthRepository.
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
                title = { Text("Lista Produktów") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        searchQuery = ""
                        viewModel.loadProducts()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasFullPermissions) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("add_product")
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj nowy produkt")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.searchProducts(query)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = { Text("Szukaj produktu po nazwie...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.searchProducts("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                        }
                    }
                },
                singleLine = true
            )

            if (searchQuery.isEmpty()) {
                StatusFilterRow(
                    selectedFilter = uiState.activeFilter,
                    onFilterSelected = { activeStatus ->
                        viewModel.filterByActiveStatus(activeStatus)
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            uiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    items(uiState.products) { product ->
                        ProductCard(
                            product = product,
                            onCardClick = {
                                navController.navigate("product_details/${product.id}")
                            },
                            onDeleteItem = { viewModel.deleteProduct(product.id) },
                            onEditItem = { navController.navigate("edit_product/${product.id}") },
                            onGenerateQRItem = { navController.navigate("generate_qr/PRODUCT/${product.id}") },
                            onAddToInventory = { navController.navigate("add_inventory/PRODUCT/${product.id}") }
                        )
                    }
                }

                if (uiState.totalPages > 1 && searchQuery.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusFilterRow(
    selectedFilter: Boolean?,
    onFilterSelected: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == true,
                onClick = { onFilterSelected(true) },
                label = { Text("Aktywne") }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == false,
                onClick = { onFilterSelected(false) },
                label = { Text("Nieaktywne") }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("Wszystkie") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: ProductDTO,
    onCardClick: () -> Unit,
    onDeleteItem: (() -> Unit)? = null,
    onEditItem: (() -> Unit)? = null,
    onGenerateQRItem: (() -> Unit)? = null,
    onAddToInventory: (() -> Unit)? = null
) {
    Card(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SKU: ${product.sku}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    product.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    product.price?.let {
                        Text(
                            text = "$it PLN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    product.category?.let {
                        Badge {
                            Text(it.name)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                onAddToInventory?.let { action ->
                    IconButton(onClick = action) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj do magazynu")
                    }
                }
                onGenerateQRItem?.let { action ->
                    IconButton(onClick = action) {
                        Icon(Icons.Default.Phone, contentDescription = "Generuj kod QR")
                    }
                }
                onEditItem?.let { action ->
                    IconButton(onClick = action) {
                        Icon(Icons.Default.Edit, contentDescription = "Edytuj Produkt")
                    }
                }
                onDeleteItem?.let { action ->
                    IconButton(onClick = action) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń Produkt")
                    }
                }
            }
        }
    }
}