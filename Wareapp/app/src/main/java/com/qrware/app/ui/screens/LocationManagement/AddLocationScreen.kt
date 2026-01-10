package com.qrware.app.ui.screens.LocationManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
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
import com.qrware.app.data.dto.ZoneDTO
import com.qrware.app.data.model.LocationType
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.AddLocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val viewModel: AddLocationViewModel = viewModel(
        factory = appContainer.addLocationViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj Lokalizację") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        LocationForm(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onSave = { viewModel.createLocation() },
            onCodeChange = viewModel::onCodeChange,
            onNameChange = viewModel::onNameChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onZoneSelected = viewModel::onZoneSelected,
            onTypeSelected = viewModel::onTypeSelected,
            onAisleChange = viewModel::onAisleChange,
            onRackChange = viewModel::onRackChange,
            onShelfChange = viewModel::onShelfChange,
            onBinChange = viewModel::onBinChange,
            onCapacityVolumeChange = viewModel::onCapacityVolumeChange,
            onCapacityWeightChange = viewModel::onCapacityWeightChange,
            onCapacityItemsChange = viewModel::onCapacityItemsChange,
            onTempControlledChange = viewModel::onTempControlledChange,
            onTempMinChange = viewModel::onTempMinChange,
            onTempMaxChange = viewModel::onTempMaxChange,
            onHumidityControlledChange = viewModel::onHumidityControlledChange,
            onHumidityMinChange = viewModel::onHumidityMinChange,
            onHumidityMaxChange = viewModel::onHumidityMaxChange,
            onHazardousChange = viewModel::onHazardousChange,
            onFragileChange = viewModel::onFragileChange,
            onSecurityLevelChange = viewModel::onSecurityLevelChange,
            onActiveChange = viewModel::onActiveChange,
            onPickableChange = viewModel::onPickableChange,
            onReceivableChange = viewModel::onReceivableChange,
            onQrCodeChange = viewModel::onQrCodeChange,
            onBarcodeChange = viewModel::onBarcodeChange,
            onXCoordinateChange = viewModel::onXCoordinateChange,
            onYCoordinateChange = viewModel::onYCoordinateChange,
            onZCoordinateChange = viewModel::onZCoordinateChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationForm(
    modifier: Modifier = Modifier,
    uiState: com.qrware.app.ui.viewmodel.LocationFormUiState,
    isEditMode: Boolean = false,
    onSave: () -> Unit,
    onCodeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onZoneSelected: (ZoneDTO?) -> Unit,
    onTypeSelected: (LocationType) -> Unit,
    onAisleChange: (String) -> Unit,
    onRackChange: (String) -> Unit,
    onShelfChange: (String) -> Unit,
    onBinChange: (String) -> Unit,
    onCapacityVolumeChange: (String) -> Unit,
    onCapacityWeightChange: (String) -> Unit,
    onCapacityItemsChange: (String) -> Unit,
    onTempControlledChange: (Boolean) -> Unit,
    onTempMinChange: (String) -> Unit,
    onTempMaxChange: (String) -> Unit,
    onHumidityControlledChange: (Boolean) -> Unit,
    onHumidityMinChange: (String) -> Unit,
    onHumidityMaxChange: (String) -> Unit,
    onHazardousChange: (Boolean) -> Unit,
    onFragileChange: (Boolean) -> Unit,
    onSecurityLevelChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onPickableChange: (Boolean) -> Unit,
    onReceivableChange: (Boolean) -> Unit,
    onQrCodeChange: (String) -> Unit,
    onBarcodeChange: (String) -> Unit,
    onXCoordinateChange: (String) -> Unit,
    onYCoordinateChange: (String) -> Unit,
    onZCoordinateChange: (String) -> Unit
) {
    var zoneExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { FormSectionHeader("Informacje Główne") }

        item {
            OutlinedTextField(
                value = uiState.code,
                onValueChange = onCodeChange,
                label = { Text("Kod Lokalizacji*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isEditMode,
                isError = uiState.code.isBlank()
            )
        }
        item {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Nazwa Lokalizacji*") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.name.isBlank()
            )
        }
        item {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            ExposedDropdownMenuBox(
                expanded = zoneExpanded,
                onExpandedChange = { zoneExpanded = !zoneExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.zones.find { it.id == uiState.zoneId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Strefa*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = zoneExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    isError = uiState.zoneId == null
                )
                ExposedDropdownMenu(
                    expanded = zoneExpanded,
                    onDismissRequest = { zoneExpanded = false }
                ) {
                    uiState.zones.forEach { zone ->
                        DropdownMenuItem(
                            text = { Text(zone.name) },
                            onClick = {
                                onZoneSelected(zone)
                                zoneExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.type.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Typ Lokalizacji*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    uiState.locationTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                onTypeSelected(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item { FormSectionHeader("Adres Magazynowy") }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormTextField(Modifier.weight(1f), uiState.aisle, onAisleChange, "Korytarz")
                FormTextField(Modifier.weight(1f), uiState.rack, onRackChange, "Regał")
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormTextField(Modifier.weight(1f), uiState.shelf, onShelfChange, "Półka")
                FormTextField(Modifier.weight(1f), uiState.bin, onBinChange, "Pozycja (Bin)")
            }
        }

        item { FormSectionHeader("Ustawienia") }
        item { FormSwitchRow(uiState.active, onActiveChange, "Aktywna") }
        item { FormSwitchRow(uiState.pickable, onPickableChange, "Możliwy pobór (Pickable)") }
        item { FormSwitchRow(uiState.receivable, onReceivableChange, "Możliwy przychód (Receivable)") }
        item { FormSwitchRow(uiState.hazardousMaterials, onHazardousChange, "Materiały niebezpieczne") }
        item { FormSwitchRow(uiState.fragileItems, onFragileChange, "Materiały delikatne") }

        item { FormSectionHeader("Kontrola Środowiska") }
        item { FormSwitchRow(uiState.temperatureControlled, onTempControlledChange, "Kontrola Temperatury") }
        if (uiState.temperatureControlled) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(Modifier.weight(1f), uiState.temperatureMin, onTempMinChange, "Temp. Min (°C)", KeyboardType.Number)
                    FormTextField(Modifier.weight(1f), uiState.temperatureMax, onTempMaxChange, "Temp. Max (°C)", KeyboardType.Number)
                }
            }
        }
        item { FormSwitchRow(uiState.humidityControlled, onHumidityControlledChange, "Kontrola Wilgotności") }
        if (uiState.humidityControlled) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(Modifier.weight(1f), uiState.humidityMin, onHumidityMinChange, "Wilg. Min (%)", KeyboardType.Number)
                    FormTextField(Modifier.weight(1f), uiState.humidityMax, onHumidityMaxChange, "Wilg. Max (%)", KeyboardType.Number)
                }
            }
        }

        item { FormSectionHeader("Pojemność i Kody") }
        item { FormTextField(Modifier.fillMaxWidth(), uiState.capacityVolume, onCapacityVolumeChange, "Pojemność (m³)", KeyboardType.Decimal) }
        item { FormTextField(Modifier.fillMaxWidth(), uiState.capacityWeight, onCapacityWeightChange, "Nośność (kg)", KeyboardType.Decimal) }
        item { FormTextField(Modifier.fillMaxWidth(), uiState.capacityItems, onCapacityItemsChange, "Pojemność (szt.)", KeyboardType.Number) }
        item { FormTextField(Modifier.fillMaxWidth(), uiState.securityLevel, onSecurityLevelChange, "Poziom Bezpieczeństwa (1-5)", KeyboardType.Number) }
        item { FormTextField(Modifier.fillMaxWidth(), uiState.qrCode, onQrCodeChange, "Kod QR") }
        item { FormTextField(Modifier.fillMaxWidth(), uiState.barcode, onBarcodeChange, "Kod Kreskowy") }

        item { FormSectionHeader("Współrzędne") }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormTextField(Modifier.weight(1f), uiState.xCoordinate, onXCoordinateChange, "X", KeyboardType.Decimal)
                FormTextField(Modifier.weight(1f), uiState.yCoordinate, onYCoordinateChange, "Y", KeyboardType.Decimal)
                FormTextField(Modifier.weight(1f), uiState.zCoordinate, onZCoordinateChange, "Z", KeyboardType.Decimal)
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = onSave,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(24.dp))
                else Text(if(isEditMode) "Aktualizuj" else "Zapisz")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun FormSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun FormSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun FormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}