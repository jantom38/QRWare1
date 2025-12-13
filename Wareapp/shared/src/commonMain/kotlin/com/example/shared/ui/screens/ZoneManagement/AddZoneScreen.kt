package com.example.shared.ui.screens.ZoneManagement

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.shared.ui.viewmodel.AddZoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddZoneScreen(
    navController: NavController,
    viewModel: AddZoneViewModel // ZMIANA: Przyjmujemy ViewModel
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
                title = { Text("Dodaj Nową Strefę") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        ZoneForm(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            isEditMode = false,
            onSave = { viewModel.createZone() },

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