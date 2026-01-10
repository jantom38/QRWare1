package com.qrware.app.ui.screens.LocationManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.ManageLocationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailsScreen(
    navController: NavController,
    appContainer: AppContainer,
    locationId: Long
) {
    val viewModel: ManageLocationsViewModel = viewModel(
        factory = appContainer.manageLocationsViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(locationId) {
        viewModel.getLocationDetails(locationId)
    }

    val location = uiState.selectedLocation

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły Lokalizacji") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    if (location != null) {
                        IconButton(onClick = { navController.navigate("edit_location/${location.id}") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (location != null) {
                FloatingActionButton(onClick = {
                    navController.navigate("generate_qr/LOCATION/${location.id}")
                }) {
                    Icon(Icons.Default.QrCode, contentDescription = "QR")
                }
            }
        }
    ) { paddingValues ->
        if (location == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Nie znaleziono lokalizacji")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Podstawowe informacje
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = location.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider()
                        
                        DetailRow("Kod", location.code)
                        DetailRow("Strefa", location.zone.name)
                        DetailRow("Typ", location.type?.displayName ?: "-")
                        DetailRow("Status", if (location.active) "Aktywna" else "Nieaktywna")
                        DetailRow("Kod kreskowy", location.barcode ?: "-")
                        
                        location.description?.let {
                            if (it.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Opis:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = it)
                            }
                        }
                    }
                }

                // Położenie
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Położenie",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider()
                        
                        DetailRow("Alejka", location.aisle ?: "-")
                        DetailRow("Regał", location.rack ?: "-")
                        DetailRow("Półka", location.shelf ?: "-")
                        DetailRow("Pozycja", location.bin ?: "-")
                        
                        if (location.xCoordinate != null || location.yCoordinate != null || location.zCoordinate != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Współrzędne:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("X: ${location.xCoordinate ?: "-"}")
                                Text("Y: ${location.yCoordinate ?: "-"}")
                                Text("Z: ${location.zCoordinate ?: "-"}")
                            }
                        }
                    }
                }

                // Pojemność
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pojemność",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider()
                        
                        DetailRow("Objętość", "${location.capacityVolume?.toPlainString() ?: "-"} m³")
                        DetailRow("Waga", "${location.capacityWeight?.toPlainString() ?: "-"} kg")
                        DetailRow("Ilość przedmiotów", "${location.capacityItems ?: "-"}")
                    }
                }

                // Warunki środowiskowe
                if (location.temperatureControlled || location.humidityControlled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Warunki środowiskowe",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            HorizontalDivider()
                            
                            if (location.temperatureControlled) {
                                DetailRow("Kontrola temperatury", "TAK")
                                DetailRow("Min. temperatura", "${location.temperatureMin ?: "-"} °C")
                                DetailRow("Max. temperatura", "${location.temperatureMax ?: "-"} °C")
                            } else {
                                DetailRow("Kontrola temperatury", "NIE")
                            }

                            if (location.humidityControlled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                DetailRow("Kontrola wilgotności", "TAK")
                                DetailRow("Min. wilgotność", "${location.humidityMin ?: "-"} %")
                                DetailRow("Max. wilgotność", "${location.humidityMax ?: "-"} %")
                            } else {
                                DetailRow("Kontrola wilgotności", "NIE")
                            }
                        }
                    }
                }

                // Właściwości i Bezpieczeństwo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Właściwości i Bezpieczeństwo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider()
                        
                        BooleanRow("Materiały niebezpieczne", location.hazardousMaterials)
                        BooleanRow("Przedmioty kruche", location.fragileItems)
                        BooleanRow("Możliwość pobierania (Pickable)", location.pickable)
                        BooleanRow("Możliwość przyjmowania (Receivable)", location.receivable)
                        DetailRow("Poziom bezpieczeństwa", location.securityLevel.toString())
                    }
                }
                
                Button(
                    onClick = { navController.navigate("movement_history/location/${location.id}") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Historia Ruchów")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BooleanRow(label: String, value: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (value) "TAK" else "NIE",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}