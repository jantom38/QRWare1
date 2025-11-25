package com.qrware.app.ui.screens.ZoneManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qrware.app.data.model.ZoneType
import com.qrware.app.ui.viewmodel.ZoneFormUiState
import com.qrware.app.ui.screens.LocationManagement.FormSectionHeader
import com.qrware.app.ui.screens.LocationManagement.FormSwitchRow
import com.qrware.app.ui.screens.LocationManagement.FormTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneForm(
    modifier: Modifier = Modifier,
    uiState: ZoneFormUiState,
    isEditMode: Boolean = false,
    onSave: () -> Unit,

    // Handlery
    onCodeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeSelected: (ZoneType) -> Unit,
    onActiveChange: (Boolean) -> Unit,

    onTempControlledChange: (Boolean) -> Unit,
    onTempMinChange: (String) -> Unit,
    onTempMaxChange: (String) -> Unit,

    onHumidityControlledChange: (Boolean) -> Unit,
    onHumidityMinChange: (String) -> Unit,
    onHumidityMaxChange: (String) -> Unit,

    onSecurityLevelChange: (String) -> Unit,
    onHazardousChange: (Boolean) -> Unit,
    onFragileChange: (Boolean) -> Unit,
    onPickingPriorityChange: (String) -> Unit,

    onManagerChange: (String) -> Unit,
    onContactInfoChange: (String) -> Unit,
    onColorChange: (String) -> Unit
) {
    var typeExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- GŁÓWNE ---
        item { FormSectionHeader("Informacje podstawowe") }

        item {
            OutlinedTextField(
                value = uiState.code,
                onValueChange = onCodeChange,
                label = { Text("Kod Strefy*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isEditMode, // Kod zazwyczaj stały
                isError = uiState.code.isBlank()
            )
        }
        item {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Nazwa Strefy*") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.name.isBlank()
            )
        }

        // Dropdown dla Typu (ZoneType)
        item {
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.type.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Typ Strefy*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    ZoneType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(type.displayName)
                                    Text(type.description, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                onTypeSelected(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        // --- USTAWIENIA I BEZPIECZEŃSTWO ---
        item { FormSectionHeader("Ustawienia Operacyjne") }
        item { FormSwitchRow(uiState.active, onActiveChange, "Aktywna") }
        item { FormSwitchRow(uiState.hazardousMaterials, onHazardousChange, "Materiały niebezpieczne (Hazmat)") }
        item { FormSwitchRow(uiState.fragileItems, onFragileChange, "Przedmioty delikatne") }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormTextField(Modifier.weight(1f), uiState.securityLevel, onSecurityLevelChange, "Poz. bezp. (1-5)", KeyboardType.Number)
                FormTextField(Modifier.weight(1f), uiState.pickingPriority, onPickingPriorityChange, "Priorytet (1-5)", KeyboardType.Number)
            }
        }

        // --- WARUNKI ŚRODOWISKOWE ---
        item { FormSectionHeader("Warunki Środowiskowe") }
        item { FormSwitchRow(uiState.temperatureControlled, onTempControlledChange, "Kontrola Temperatury") }
        if (uiState.temperatureControlled) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(Modifier.weight(1f), uiState.temperatureMin, onTempMinChange, "Min °C", KeyboardType.Number)
                    FormTextField(Modifier.weight(1f), uiState.temperatureMax, onTempMaxChange, "Max °C", KeyboardType.Number)
                }
            }
        }

        item { FormSwitchRow(uiState.humidityControlled, onHumidityControlledChange, "Kontrola Wilgotności") }
        if (uiState.humidityControlled) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(Modifier.weight(1f), uiState.humidityMin, onHumidityMinChange, "Min %", KeyboardType.Number)
                    FormTextField(Modifier.weight(1f), uiState.humidityMax, onHumidityMaxChange, "Max %", KeyboardType.Number)
                }
            }
        }

        // --- ZARZĄDZANIE ---
        item { FormSectionHeader("Dane Kontaktowe") }
        item { FormTextField(Modifier.fillMaxWidth(), uiState.manager, onManagerChange, "Kierownik Strefy") }
        item { FormTextField(Modifier.fillMaxWidth(), uiState.contactInfo, onContactInfoChange, "Kontakt") }
        item { FormTextField(Modifier.fillMaxWidth(), uiState.color, onColorChange, "Kolor (HEX)") }

        // --- PRZYCISKI ---
        item {
            Spacer(Modifier.height(16.dp))
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }
            Button(
                onClick = onSave,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text(if (isEditMode) "Zapisz Zmiany" else "Utwórz Strefę")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}