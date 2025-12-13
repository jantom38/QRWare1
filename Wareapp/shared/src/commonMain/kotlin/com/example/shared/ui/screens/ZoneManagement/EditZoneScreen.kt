package com.example.shared.ui.screens.ZoneManagement

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.shared.ui.viewmodel.EditZoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditZoneScreen(
    navController: NavController,
    viewModel: EditZoneViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj Strefę") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        // ZoneForm musi być dostępny (publiczny w AddZoneScreen.kt lub wspólnym pliku)
        // Zakładam, że AddZoneScreen.kt również został zaktualizowany w podobny sposób
        // i udostępnia ZoneForm. Jeśli nie, trzeba go przenieść do osobnego pliku.
        // W tym kontekście zakładam, że jest dostępny.
        ZoneForm(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            isEditMode = true,
            onSave = { viewModel.updateZone() },

            onCodeChange = viewModel::onCodeChange,
            onNameChange = viewModel::onNameChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onTypeSelected = viewModel::onTypeSelected,
            onActiveChange = viewModel::onActiveChange,
            onTempControlledChange = viewModel::onTempControlledChange,
            onTempMinChange = viewModel::onTempMinChange,
            onTempMaxChange = viewModel::onTempMaxChange,
            onHumidityControlledChange = viewModel::onHumidityControlledChange,
            onHumidityMinChange = viewModel::onHumidityMinChange,
            onHumidityMaxChange = viewModel::onHumidityMaxChange,
            onSecurityLevelChange = viewModel::onSecurityLevelChange,
            onHazardousChange = viewModel::onHazardousChange,
            onFragileChange = viewModel::onFragileChange,
            onPickingPriorityChange = viewModel::onPickingPriorityChange,
            onManagerChange = viewModel::onManagerChange,
            onContactInfoChange = viewModel::onContactInfoChange,
            onColorChange = viewModel::onColorChange
        )
    }
}