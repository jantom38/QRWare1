package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage // Wymaga biblioteki Coil
import com.qrware.app.data.model.GenerateQRRequest
import com.qrware.app.data.model.QRCodeType
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.QRCodeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRGeneratorScreen(
    navController: NavController,
    appContainer: AppContainer,
    initialType: String? = null,
    initialEntityId: Long? = null
) {
    val viewModel: QRCodeViewModel = viewModel(factory = appContainer.qrCodeViewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val generatedQRCode by viewModel.generatedQRCode.collectAsState()

    // Inicjalizacja stanu formularza
    val defaultType = initialType?.let {
        try { QRCodeType.valueOf(it) } catch(e: Exception) { QRCodeType.CUSTOM }
    } ?: QRCodeType.CUSTOM

    var selectedType by remember { mutableStateOf(defaultType) }
    var data by remember { mutableStateOf("") }
    var entityType by remember { mutableStateOf(initialType ?: "") }
    var entityId by remember { mutableStateOf(initialEntityId?.toString() ?: "") }
    var description by remember { mutableStateOf("") }

    // Automatyczne uzupełnianie danych jeśli przekazano parametry
    LaunchedEffect(initialType, initialEntityId) {
        if (initialType != null && initialEntityId != null) {
            data = when (initialType) {
                "PRODUCT" -> "PRODUCT:$initialEntityId"
                "LOCATION" -> "LOCATION:$initialEntityId"
                "INVENTORY_ITEM" -> "ITEM:$initialEntityId"
                else -> "$initialType:$initialEntityId"
            }
            description = "QR dla $initialType #$initialEntityId"
        }
    }

    // Czyść stan przy wyjściu z ekranu
    DisposableEffect(Unit) {
        onDispose { viewModel.clearGeneratedQRCode() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generator QR") },
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

                        // Obraz z serwera (używamy 10.0.2.2 dla emulatora, zmień na IP serwera dla fizycznego urządzenia)
                        val imageUrl = "http://10.0.2.2:8080/api/qr-codes/image/${generatedQRCode!!.imagePath}"

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Generated QR Code",
                            modifier = Modifier
                                .size(250.dp)
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Kod: ${generatedQRCode!!.code}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Dane: ${generatedQRCode!!.data}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.clearGeneratedQRCode() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generuj kolejny")
                        }

                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Wróć do listy")
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
                        Text("Nowy kod QR", style = MaterialTheme.typography.titleMedium)

                        // Wybór typu (Dropdown)
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedType.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Typ") },
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

                        OutlinedTextField(
                            value = data,
                            onValueChange = { data = it },
                            label = { Text("Dane / Treść kodu") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = entityType,
                                onValueChange = { entityType = it },
                                label = { Text("Typ encji") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = entityId,
                                onValueChange = { if (it.all { c -> c.isDigit() }) entityId = it },
                                label = { Text("ID encji") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Powód / Opis") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.error != null) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = {
                                val request = GenerateQRRequest(
                                    code = "", // Generowane przez backend
                                    type = selectedType,
                                    data = data,
                                    entityType = entityType.ifBlank { null },
                                    entityId = entityId.toLongOrNull(),
                                    generationReason = description.ifBlank { null },
                                    generatedBy = "MobileApp"
                                )
                                viewModel.generateQRCode(request)
                            },
                            enabled = !uiState.isLoading && data.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.QrCode, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Wygeneruj")
                            }
                        }
                    }
                }
            }
        }
    }
}