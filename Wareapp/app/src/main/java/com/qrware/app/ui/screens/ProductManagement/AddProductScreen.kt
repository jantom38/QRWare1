package com.qrware.app.ui.screens.ProductManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrware.app.ui.viewmodel.AddProductViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: AddProductViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Stany dla pól formularza
    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("PIECE") }
    var minimumStock by remember { mutableStateOf("") }
    var maximumStock by remember { mutableStateOf("") }
    var reorderPoint by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var storageConditions by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }
    var perishable by remember { mutableStateOf(false) }
    var hazardous by remember { mutableStateOf(false) }
    var fragile by remember { mutableStateOf(false) }

    // Obsługa komunikatów (błąd lub sukces)
    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            delay(1500) // Daj czas na przeczytanie
            viewModel.clearMessages()
            navController.popBackStack() // Wróć do ekranu listy
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Dodaj Nowy Produkt") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Wprowadź dane nowego produktu", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU (Wymagane)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nazwa (Wymagane)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Cena (np. 123.45)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Koszt (np. 80.00)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Waga (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = length,
                    onValueChange = { length = it },
                    label = { Text("Długość (cm)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = { Text("Szerokość (cm)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Wysokość (cm)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )
            }

            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Jednostka miary") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minimumStock,
                    onValueChange = { minimumStock = it },
                    label = { Text("Min. zapas") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = maximumStock,
                    onValueChange = { maximumStock = it },
                    label = { Text("Max. zapas") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = reorderPoint,
                    onValueChange = { reorderPoint = it },
                    label = { Text("Punkt zamówienia") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
            }

            OutlinedTextField(
                value = manufacturer,
                onValueChange = { manufacturer = it },
                label = { Text("Producent") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = supplier,
                onValueChange = { supplier = it },
                label = { Text("Dostawca") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = storageConditions,
                onValueChange = { storageConditions = it },
                label = { Text("Warunki przechowywania") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Kod kreskowy") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Checkboxy dla właściwości produktu
            Text("Właściwości produktu", style = MaterialTheme.typography.titleMedium)
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = active, onCheckedChange = { active = it })
                Text("Aktywny", modifier = Modifier.padding(start = 8.dp))
            }
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = perishable, onCheckedChange = { perishable = it })
                Text("Psujący się", modifier = Modifier.padding(start = 8.dp))
            }
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = hazardous, onCheckedChange = { hazardous = it })
                Text("Niebezpieczny", modifier = Modifier.padding(start = 8.dp))
            }
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = fragile, onCheckedChange = { fragile = it })
                Text("Kruchy", modifier = Modifier.padding(start = 8.dp))
            }

            OutlinedTextField(
                value = categoryId,
                onValueChange = { categoryId = it },
                label = { Text("ID Kategorii (np. 1)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val priceDecimal = price.toBigDecimalOrNull()
                    val costDecimal = cost.toBigDecimalOrNull()
                    val weightDecimal = weight.toBigDecimalOrNull()
                    val lengthDecimal = length.toBigDecimalOrNull()
                    val widthDecimal = width.toBigDecimalOrNull()
                    val heightDecimal = height.toBigDecimalOrNull()
                    val minStockInt = minimumStock.toIntOrNull()
                    val maxStockInt = maximumStock.toIntOrNull()
                    val reorderPointInt = reorderPoint.toIntOrNull()
                    val catIdLong = categoryId.toLongOrNull()
                    
                    viewModel.createProduct(
                        sku = sku,
                        name = name,
                        description = description.takeIf { it.isNotBlank() },
                        price = priceDecimal,
                        cost = costDecimal,
                        unit = unit.takeIf { it.isNotBlank() },
                        weight = weightDecimal,
                        length = lengthDecimal,
                        width = widthDecimal,
                        height = heightDecimal,
                        minimumStock = minStockInt,
                        maximumStock = maxStockInt,
                        reorderPoint = reorderPointInt,
                        active = active,
                        perishable = perishable,
                        hazardous = hazardous,
                        fragile = fragile,
                        manufacturer = manufacturer.takeIf { it.isNotBlank() },
                        supplier = supplier.takeIf { it.isNotBlank() },
                        storageConditions = storageConditions.takeIf { it.isNotBlank() },
                        barcode = barcode.takeIf { it.isNotBlank() },
                        categoryId = catIdLong
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                // Przycisk jest nieaktywny, jeśli trwa ładowanie lub brakuje wymaganych pól
                enabled = !uiState.isLoading && sku.isNotBlank() && name.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Zapisz Produkt")
                }
            }
        }
    }
}