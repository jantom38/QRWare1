package com.qrware.app.ui.screens.ProductManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.EditProductViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    navController: NavController,
    appContainer: AppContainer,
    productId: Long
) {
    val viewModel: EditProductViewModel = viewModel(
        factory = appContainer.createEditProductViewModelFactory(productId)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Nawiguj wstecz po udanej aktualizacji
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            snackbarHostState.showSnackbar("Produkt zaktualizowany pomyślnie!")
            delay(1000) // Daj użytkownikowi chwilę na przeczytanie
            navController.popBackStack()
        }
    }

    // Pokaż błędy
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(uiState.error!!)
            viewModel.clearError() // Wyczyść błąd po pokazaniu
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edytuj Produkt") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.product == null) {
            // Ładowanie początkowe
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.product != null) {
            // Formularz
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nazwa Produktu*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.name.isBlank()
                )

                OutlinedTextField(
                    value = uiState.sku,
                    onValueChange = viewModel::onSkuChange,
                    label = { Text("SKU*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.sku.isBlank()
                    // Rozważ ustawienie enabled = false, jeśli SKU nie powinno być edytowalne
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )

                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = viewModel::onPriceChange,
                    label = { Text("Cena (np. 123.45)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // --- DODANY KOMPONENT SWITCH ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Produkt Aktywny",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = uiState.active,
                        onCheckedChange = viewModel::onActiveChange
                    )
                }
                // --- KONIEC KOMPONENTU SWITCH ---


                // TODO: Dodać pole wyboru kategorii (np. DropdownMenu)
                // Na razie pokazujemy ID
                Text(
                    text = "Kategoria ID: ${uiState.categoryId ?: "Brak"}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp) // Dodany padding dla odstępu
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.updateProduct() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Zapisz Zmiany")
                    }
                }
            }
        } else if (uiState.error != null && !uiState.isLoading) {
            // Błąd ładowania
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "Nie można załadować produktu.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}