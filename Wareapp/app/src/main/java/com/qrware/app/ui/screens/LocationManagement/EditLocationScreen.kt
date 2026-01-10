package com.qrware.app.ui.screens.LocationManagement

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.EditLocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationScreen(
    navController: NavController,
    appContainer: AppContainer,
    locationId: Long
) {
    val viewModel: EditLocationViewModel = viewModel(
        factory = appContainer.createEditLocationViewModelFactory(locationId)
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
                title = { Text("Edytuj Lokalizację") },
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
            isEditMode = true,
            onSave = { viewModel.updateLocation() },
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