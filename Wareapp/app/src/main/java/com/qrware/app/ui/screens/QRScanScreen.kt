package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.model.QRCodeType
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.QRCodeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val viewModel: QRCodeViewModel = viewModel(
        factory = appContainer.qrCodeViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    var manualCode by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(scanResult) {
        if (scanResult != null && scanResult!!.success) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearScanResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skanuj Kod QR") },
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
            // Header z instrukcjami
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Skanowanie Kodu QR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Skieruj aparat na kod QR lub wprowadź kod ręcznie",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Przycisk otwierania kamery (placeholder)
            Button(
                onClick = { 
                    // TODO: Implementacja skanowania z kamery
                    // Tymczasowo pokaż pole ręcznego wprowadzania
                    showManualInput = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Otwórz Aparat")
            }

            // Ręczne wprowadzanie kodu
            if (showManualInput) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Wprowadź kod ręcznie",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualCode,
                            onValueChange = { manualCode = it },
                            label = { Text("Kod QR") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (manualCode.isNotBlank()) {
                                        viewModel.scanQRCode(manualCode)
                                    }
                                },
                                enabled = manualCode.isNotBlank() && !uiState.isScanning,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Skanuj")
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    manualCode = ""
                                    showManualInput = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Anuluj")
                            }
                        }
                    }
                }
            }

            // Komunikaty błędów/sukcesu
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Wynik skanowania
            scanResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (result.success) "✅ Skanowanie udane" else "❌ Skanowanie nieudane",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (result.success) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        if (result.success) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                "Kod: ${result.code}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Text(
                                "Typ: ${getQRTypeDisplayName(result.type)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            if (result.entityType != null && result.entityId != null) {
                                Text(
                                    "Encja: ${result.entityType} (ID: ${result.entityId})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Text(
                                "Dane: ${result.data}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Nawiguj do odpowiedniego ekranu na podstawie typu
                                        when (result.type) {
                                            QRCodeType.PRODUCT -> {
                                                if (result.entityId != null) {
                                                    navController.navigate("product_details/${result.entityId}")
                                                }
                                            }
                                            QRCodeType.INVENTORY_ITEM -> {
                                                navController.navigate("inventory")
                                            }
                                            QRCodeType.LOCATION -> {
                                                navController.navigate("locations")
                                            }
                                            else -> {
                                                // Dla niestandardowych kodów
                                            }
                                        }
                                    }
                                ) {
                                    Text("Przejdź do szczegółów")
                                }
                                
                                OutlinedButton(
                                    onClick = {
                                        viewModel.clearScanResult()
                                        manualCode = ""
                                    }
                                ) {
                                    Text("Skanuj kolejny")
                                }
                            }
                        } else {
                            result.message?.let { message ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // Szybkie akcje - przykładowe kody do testowania
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Szybkie testowanie",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.scanQRCode("PRODUCT_001")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Produkt")
                        }
                        Button(
                            onClick = {
                                viewModel.scanQRCode("LOC_A01")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Lokalizacja")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getQRTypeDisplayName(type: QRCodeType): String {
    return when (type) {
        QRCodeType.PRODUCT -> "Produkt"
        QRCodeType.LOCATION -> "Lokalizacja"
        QRCodeType.INVENTORY_ITEM -> "Pozycja magazynowa"
        QRCodeType.SHIPMENT -> "Przesyłka"
        QRCodeType.CUSTOM -> "Niestandardowy"
    }
}