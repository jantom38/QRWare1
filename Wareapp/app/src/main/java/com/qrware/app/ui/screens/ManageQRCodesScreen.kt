package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.model.*
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.QRCodeViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageQRCodesScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val viewModel: QRCodeViewModel = viewModel(
        factory = appContainer.qrCodeViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()
    val stats by viewModel.stats.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedQRCode by remember { mutableStateOf<QRCodeData?>(null) }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarządzaj Kodami QR") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadQRCodes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj kod QR")
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
        ) {
            // Statystyki
            stats?.let { statsData ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Statystyki kodów QR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Łącznie", statsData.totalCodes.toString())
                            StatItem("Aktywne", statsData.activeCodes.toString())
                            StatItem("Nieaktywne", statsData.inactiveCodes.toString())
                            StatItem("Skanowania", statsData.totalScans.toString())
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Filtry
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.loadQRCodes() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Wszystkie")
                }
                Button(
                    onClick = { viewModel.loadActiveQRCodes() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aktywne")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filtry po typie
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QRCodeType.values().forEach { type ->
                    FilterChip(
                        onClick = { viewModel.loadQRCodesByType(type) },
                        label = { Text(getQRTypeDisplayName(type), style = MaterialTheme.typography.bodySmall) },
                        selected = false,
                        modifier = Modifier.weight(1f, false)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista kodów QR
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.qrCodes) { qrCode ->
                        QRCodeCard(
                            qrCode = qrCode,
                            onEdit = {
                                selectedQRCode = qrCode
                                showEditDialog = true
                            },
                            onDelete = {
                                viewModel.deleteQRCode(qrCode.id)
                            },
                            onToggleActive = {
                                viewModel.toggleQRCodeActive(qrCode.id)
                            }
                        )
                    }
                }

                // Paginacja
                if (uiState.totalPages > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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

    // Dialog dodawania kodu QR
    if (showAddDialog) {
        AddQRCodeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { request ->
                viewModel.generateQRCode(request)
                showAddDialog = false
            }
        )
    }

    // Dialog edycji kodu QR
    if (showEditDialog && selectedQRCode != null) {
        EditQRCodeDialog(
            qrCode = selectedQRCode!!,
            onDismiss = { 
                showEditDialog = false
                selectedQRCode = null
            },
            onConfirm = { request ->
                viewModel.updateQRCode(selectedQRCode!!.id, request)
                showEditDialog = false
                selectedQRCode = null
            }
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun QRCodeCard(
    qrCode: QRCodeData,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = qrCode.code,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Typ: ${getQRTypeDisplayName(qrCode.type)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (qrCode.entityType != null && qrCode.entityId != null) {
                        Text(
                            text = "Encja: ${qrCode.entityType} (${qrCode.entityId})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Skanowania: ${qrCode.scanCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    qrCode.lastScanned?.let { lastScanned ->
                        Text(
                            text = "Ostatnie skanowanie: ${lastScanned.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Badge(
                        containerColor = if (qrCode.active) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    ) {
                        Text(if (qrCode.active) "Aktywny" else "Nieaktywny")
                    }
                    qrCode.expiresAt?.let { expiresAt ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wygasa: ${expiresAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                }

                IconButton(onClick = onToggleActive) {
                    Icon(
                        if (qrCode.active) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (qrCode.active) "Dezaktywuj" else "Aktywuj"
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQRCodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (GenerateQRRequest) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var data by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(QRCodeType.CUSTOM) }
    var entityType by remember { mutableStateOf("") }
    var entityId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wygeneruj kod QR") },
        text = {
            Column {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Kod QR") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Dropdown dla typu
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = getQRTypeDisplayName(selectedType),
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
                                text = { Text(getQRTypeDisplayName(type)) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it },
                    label = { Text("Dane") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = entityType,
                    onValueChange = { entityType = it },
                    label = { Text("Typ encji (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = entityId,
                    onValueChange = { entityId = it },
                    label = { Text("ID encji (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        GenerateQRRequest(
                            code = code,
                            type = selectedType,
                            data = data,
                            entityType = entityType.takeIf { it.isNotBlank() },
                            entityId = entityId.takeIf { it.isNotBlank() }?.toLongOrNull(),
                            generationReason = "Utworzony ręcznie przez użytkownika"
                        )
                    )
                },
                enabled = code.isNotBlank() && data.isNotBlank()
            ) {
                Text("Wygeneruj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun EditQRCodeDialog(
    qrCode: QRCodeData,
    onDismiss: () -> Unit,
    onConfirm: (UpdateQRRequest) -> Unit
) {
    var data by remember { mutableStateOf(qrCode.data) }
    var metadata by remember { mutableStateOf(qrCode.metadata ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj kod QR") },
        text = {
            Column {
                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it },
                    label = { Text("Dane") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = metadata,
                    onValueChange = { metadata = it },
                    label = { Text("Metadane") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        UpdateQRRequest(
                            data = data.takeIf { it != qrCode.data },
                            metadata = metadata.takeIf { it != qrCode.metadata }
                        )
                    )
                },
                enabled = data.isNotBlank()
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
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