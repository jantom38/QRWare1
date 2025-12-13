package com.example.shared.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.shared.data.model.GenerateQRRequest
import com.example.shared.data.model.QRCodeType
import com.example.shared.ui.viewmodel.QRCodeViewModel

// Pomocnicza klasa dla dynamicznych pól
data class CustomField(
    var key: String = "",
    var value: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRGeneratorScreen(
    navController: NavController,
    viewModel: QRCodeViewModel, // ZMIANA: Przyjmujemy ViewModel
    initialType: String? = null,
    initialEntityId: Long? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val generatedQRCode by viewModel.generatedQRCode.collectAsState()

    // Inicjalizacja stanu formularza
    val defaultType = initialType?.let {
        try { QRCodeType.valueOf(it) } catch(e: Exception) { QRCodeType.CUSTOM }
    } ?: QRCodeType.CUSTOM

    var selectedType by remember { mutableStateOf(defaultType) }

    // Główne dane (np. nazwa produktu)
    var mainDataName by remember { mutableStateOf("") }

    // Dynamiczna lista pól dodatkowych
    val customFields = remember { mutableStateListOf<CustomField>() }

    var entityType by remember { mutableStateOf(initialType ?: "") }
    var entityId by remember { mutableStateOf(initialEntityId?.toString() ?: "") }
    var description by remember { mutableStateOf("") }

    // Automatyczne uzupełnianie danych jeśli przekazano parametry
    LaunchedEffect(initialType, initialEntityId) {
        if (initialType != null && initialEntityId != null) {
            mainDataName = "$initialType #$initialEntityId"
            description = "QR dla $initialType #$initialEntityId"
        }
    }

    // Czyść stan przy wyjściu z ekranu
    DisposableEffect(Unit) {
        onDispose { viewModel.clearGeneratedQRCode() }
    }

    // === PROSTE COFANIE ===
    val handleBackNavigation = {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generator QR") },
                navigationIcon = {
                    // Używamy naszej nowej funkcji powrotu
                    IconButton(onClick = { handleBackNavigation() }) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === WIDOK SUKCESU (WYGENEROWANY OBRAZ) ===
            if (generatedQRCode != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Kod QR Gotowy!", style = MaterialTheme.typography.headlineSmall)

                        Spacer(modifier = Modifier.height(24.dp))

                        // UWAGA: Zmień IP na właściwe dla Twojego środowiska (np. z pliku konfiguracyjnego)
                        // W KMP lepiej to przekazywać z zewnątrz lub trzymać w stałych
                        val imageUrl = "http://192.168.0.178:8080/api/qr-codes/image/${generatedQRCode!!.imagePath}"

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Generated QR Code",
                            modifier = Modifier
                                .size(250.dp)
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Kod Systemowy: ${generatedQRCode!!.code}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Zakodowana Treść:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            generatedQRCode!!.data,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.clearGeneratedQRCode()
                                // Opcjonalnie: wyczyść pola po sukcesie
                                // customFields.clear()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generuj kolejny")
                        }

                        OutlinedButton(
                            // Używamy naszej nowej funkcji powrotu również tutaj
                            onClick = { handleBackNavigation() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Wróć do szczegółów")
                        }
                    }
                }
            }
            // === WIDOK FORMULARZA ===
            else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Konfiguracja Kodu QR", style = MaterialTheme.typography.titleMedium)

                        // 1. Wybór typu
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedType.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Typ obiektu") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                QRCodeType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name) },
                                        onClick = {
                                            selectedType = type
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Główna nazwa / identyfikator
                        OutlinedTextField(
                            value = mainDataName,
                            onValueChange = { mainDataName = it },
                            label = { Text("Główna nazwa / opis") },
                            placeholder = { Text("np. Śruby M10 - Partia A") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // 3. Dynamiczne Pola (Custom Fields)
                        Text(
                            "Dodatkowe dane (w kodzie QR)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Te dane będą dostępne offline po zeskanowaniu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        customFields.forEachIndexed { index, field ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = field.key,
                                    onValueChange = { customFields[index] = field.copy(key = it) },
                                    label = { Text("Nazwa pola") },
                                    placeholder = { Text("np. Waga") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = field.value,
                                    onValueChange = { customFields[index] = field.copy(value = it) },
                                    label = { Text("Wartość") },
                                    placeholder = { Text("np. 20kg") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = { customFields.removeAt(index) }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Usuń pole",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { customFields.add(CustomField()) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dodaj pole")
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // 4. Powiązania systemowe (opcjonalne)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = entityType,
                                onValueChange = { entityType = it },
                                label = { Text("System Entity Type") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = entityId,
                                onValueChange = { if (it.all { c -> c.isDigit() }) entityId = it },
                                label = { Text("System ID") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Notatka wewnętrzna (tylko baza)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Wyświetlanie błędu
                        if (uiState.error != null) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Przycisk Generuj
                        Button(
                            onClick = {
                                // === LOGIKA SKLEJANIA DANYCH ===
                                val dataBuilder = StringBuilder()

                                // 1. Dodaj główną nazwę
                                if (mainDataName.isNotBlank()) {
                                    dataBuilder.append(mainDataName)
                                }

                                // 2. Dodaj pola niestandardowe w formacie "Klucz:Wartość"
                                // Oddzielone średnikami
                                customFields.forEach { field ->
                                    if (field.key.isNotBlank() && field.value.isNotBlank()) {
                                        if (dataBuilder.isNotEmpty()) dataBuilder.append(";")
                                        dataBuilder.append("${field.key}:${field.value}")
                                    }
                                }

                                val finalData = dataBuilder.toString()

                                val request = GenerateQRRequest(
                                    code = "", // Puste -> Backend generuje ID
                                    type = selectedType,
                                    data = finalData, // Tutaj wysyłamy nasz sklejony ciąg
                                    entityType = entityType.ifBlank { null },
                                    entityId = entityId.toLongOrNull(),
                                    generationReason = description.ifBlank { null },
                                    generatedBy = "MobileApp"
                                )
                                viewModel.generateQRCode(request)
                            },
                            enabled = !uiState.isLoading && (mainDataName.isNotBlank() || customFields.isNotEmpty()),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Wygeneruj Kod QR")
                            }
                        }
                    }
                }
            }
        }
    }
}