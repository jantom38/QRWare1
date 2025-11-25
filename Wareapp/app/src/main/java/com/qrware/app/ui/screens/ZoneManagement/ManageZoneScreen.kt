package com.qrware.app.ui.screens.ZoneManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.dto.ZoneDTO
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.ManageZonesViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageZonesScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    // This line corresponds to your error. 'manageZonesViewModelFactory' must exist in AppContainer
    val viewModel: ManageZonesViewModel = viewModel(
        factory = appContainer.manageZonesViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()

    // Obsługa komunikatów (znikają po 3 sek)
    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarządzanie Strefami") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadZones() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_zone") }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj Strefę")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Komunikaty błędów/sukcesu
                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                }
                uiState.successMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                }

                if (uiState.zones.isEmpty() && !uiState.isLoading) {
                    Text(
                        "Brak stref. Dodaj nową strefę.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.zones) { zone ->
                            ZoneCard(
                                zone = zone,
                                onEdit = { navController.navigate("edit_zone/${zone.id}") },
                                onDelete = { viewModel.deleteZone(zone.id!!) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZoneCard(
    zone: ZoneDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${zone.name} (${zone.code})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Typ: ${zone.type.displayName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                // Status aktywności
                Badge(containerColor = if (zone.active) Color(0xFF4CAF50) else Color.Gray) {
                    Text(if (zone.active) "Aktywna" else "Nieaktywna", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Statystyki (jeśli są dostępne)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Lokalizacje: ${zone.locationCount}", style = MaterialTheme.typography.bodySmall)
                Text("Zajętość: ${(zone.occupancyRate * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edytuj")
                }
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Usuń")
                }
            }
        }
    }
}