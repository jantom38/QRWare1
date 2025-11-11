package com.qrware.app.ui.screens.ProductManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // Upewnij się, że ten import jest
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.ManageProductsViewModel
import kotlinx.coroutines.delay

// import java.math.BigDecimal // Już niepotrzebny

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val viewModel: ManageProductsViewModel = viewModel(
        factory = appContainer.ProductsViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()

    // Usunięte: 'showEditDialog' i 'selectedProduct',
    // ponieważ nawigujemy do nowego ekranu.

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
                    IconButton(onClick = { viewModel.loadProducts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_product")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj nowy produkt")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // --- FILTRY ---
            StatusFilterRow(
                selectedFilter = uiState.activeFilter,
                onFilterSelected = { activeStatus ->
                    viewModel.filterByActiveStatus(activeStatus)
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            // --- KONIEC FILTRÓW ---

            // Komunikaty błędów/sukcesu
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
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.products) { product ->
                        ProductCard(
                            product = product,
                            onDeleteItem = {
                                viewModel.deleteProduct(product.id)
                            },
                            // --- ZMIANA: Dodana obsługa edycji ---
                            onEditItem = {
                                // Nawiguj do nowego ekranu edycji z ID produktu
                                navController.navigate("edit_product/${product.id}")
                            }
                        )
                    }
                }

                // Paginacja
                if (uiState.totalPages > 1) {
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

// --- COMPOSABLE DLA FILTRÓW (Bez zmian) ---
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
                selected = selectedFilter == true, // Aktywne
                onClick = { onFilterSelected(true) },
                label = { Text("Aktywne") }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == false, // Nieaktywne
                onClick = { onFilterSelected(false) },
                label = { Text("Nieaktywne") }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == null, // Wszystkie
                onClick = { onFilterSelected(null) },
                label = { Text("Wszystkie") }
            )
        }
    }
}


@Composable
fun ProductCard(
    product: ProductDTO,
    onDeleteItem: () -> Unit,
    onEditItem: () -> Unit // <-- ZMIANA: Dodany parametr
) {
    Card(
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
                // --- ZMIANA: Dodany przycisk Edycji ---
                IconButton(onClick = onEditItem) {
                    Icon(Icons.Default.Edit, contentDescription = "Edytuj Produkt")
                }
                IconButton(onClick = onDeleteItem) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń Produkt")
                }
            }
        }
    }
}