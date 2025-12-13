package com.example.shared.ui.screens.ProductManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shared.ui.viewmodel.ProductsManagement.ProductDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    navController: NavController,
    viewModel: ProductDetailsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.product?.name ?: "Szczegóły Produktu",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    if (uiState.product != null) {
                        IconButton(onClick = {
                            uiState.product?.id?.let { id ->
                                navController.navigate("edit_product/$id")
                            }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.product != null) {
                FloatingActionButton(onClick = {
                    uiState.product?.id?.let { id ->
                        navController.navigate("generate_qr/PRODUCT/$id")
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "QR")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = { viewModel.loadProductDetails() }) {
                        Text("Spróbuj ponownie")
                    }
                }
            } else {
                uiState.product?.let { product ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "SKU: ${product.sku}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Badge(
                                        containerColor = if(product.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    ) {
                                        Text(if(product.active) "AKTYWNY" else "NIEAKTYWNY")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${product.price ?: "-"} PLN",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                if (product.category != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(product.category.name) },
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        if (!product.description.isNullOrBlank()) {
                            SectionHeader("Opis")
                            Text(
                                text = product.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            HorizontalDivider()
                        }

                        SectionHeader("Ustawienia Magazynowe")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoItem("Min. zapas", "${product.minimumStock ?: "-"}")
                            InfoItem("Max. zapas", "${product.maximumStock ?: "-"}")
                            InfoItem("Punkt zam.", "${product.reorderPoint ?: "-"}")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aktualny stan dostępny w module Magazyn",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        HorizontalDivider()

                        SectionHeader("Wymiary i Waga")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoItem("Waga", "${product.weight ?: "-"} kg")
                            InfoItem("Dł.", "${product.dimensionsLength ?: "-"} cm")
                            InfoItem("Szer.", "${product.dimensionsWidth ?: "-"} cm")
                            InfoItem("Wys.", "${product.dimensionsHeight ?: "-"} cm")
                        }
                        HorizontalDivider()

                        SectionHeader("Właściwości")
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AttributeRow("Psujący się", product.perishable ?: false)
                            AttributeRow("Niebezpieczny", product.hazardous ?: false)
                            AttributeRow("Kruchy", product.fragile ?: false)
                        }
                        HorizontalDivider()

                        SectionHeader("Informacje Dodatkowe")
                        DetailRow("Jednostka miary", product.unitOfMeasure ?: "-")
                        DetailRow("Producent", product.manufacturer ?: "-")
                        DetailRow("Dostawca", product.supplier ?: "-")
                        DetailRow("Kod kreskowy", product.barcode ?: "-")
                        DetailRow("Warunki przechowywania", product.storageConditions ?: "-")
                        DetailRow("Koszt zakupu", "${product.cost ?: "-"} PLN")

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = { navController.navigate("add_inventory/PRODUCT/${product.id}") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Przyjmij na magazyn")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AttributeRow(label: String, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        if (isActive) {
            Text("TAK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        } else {
            Text("NIE", color = Color.Gray)
        }
    }
}