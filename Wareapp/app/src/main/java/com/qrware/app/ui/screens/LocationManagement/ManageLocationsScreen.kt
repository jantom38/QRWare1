package com.qrware.app.ui.screens.LocationManagement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.dto.LocationDTO
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.ManageLocationsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLocationsScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val viewModel: ManageLocationsViewModel = viewModel(
        factory = appContainer.manageLocationsViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista Lokalizacji") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadLocations() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_location")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj nową lokalizację")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.searchLocations(it)
                },
                label = { Text("Szukaj lokalizacji...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )
            
            StatusFilterRow(
                selectedFilter = uiState.activeFilter,
                onFilterSelected = { activeStatus ->
                    viewModel.filterByActiveStatus(activeStatus)
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(text = error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            uiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(text = message, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading && uiState.locations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.locations) { location ->
                        LocationCard(
                            location = location,
                            onDeleteItem = {
                                viewModel.deleteLocation(location.id)
                            },
                            onEditItem = {
                                navController.navigate("edit_location/${location.id}")
                            },
                            onClick = {
                                navController.navigate("location_details/${location.id}")
                            }
                        )
                    }
                }

                if (uiState.totalPages > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { viewModel.previousPage() }, enabled = uiState.currentPage > 0) { Text("Poprzednia") }
                        Text("${uiState.currentPage + 1} / ${uiState.totalPages}")
                        Button(onClick = { viewModel.nextPage() }, enabled = uiState.currentPage < uiState.totalPages - 1) { Text("Następna") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusFilterRow(
    selectedFilter: Boolean?,
    onFilterSelected: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { FilterChip(selected = selectedFilter == true, onClick = { onFilterSelected(true) }, label = { Text("Aktywne") }) }
        item { FilterChip(selected = selectedFilter == false, onClick = { onFilterSelected(false) }, label = { Text("Nieaktywne") }) }
        item { FilterChip(selected = selectedFilter == null, onClick = { onFilterSelected(null) }, label = { Text("Wszystkie") }) }
    }
}

@Composable
fun LocationCard(
    location: LocationDTO,
    onDeleteItem: () -> Unit,
    onEditItem: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kod: ${location.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val locationPath = listOfNotNull(
                        location.aisle?.let { "Al: $it" },
                        location.rack?.let { "Reg: $it" },
                        location.shelf?.let { "Pół: $it" },
                        location.bin?.let { "Poz: $it" }
                    ).joinToString(" / ")

                    if (locationPath.isNotBlank()) {
                        Text(
                            text = locationPath,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    location.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Badge(
                        containerColor = if (location.active) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = if (location.active) "Aktywna" else "Nieaktywna",
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            text = location.zone.name,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    location.type?.let { locationType ->
                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text(
                                text = locationType.displayName,
                                modifier = Modifier.padding(horizontal = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEditItem) {
                    Icon(Icons.Default.Edit, contentDescription = "Edytuj Lokalizację")
                }
                IconButton(onClick = onDeleteItem) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń Lokalizację")
                }
            }
        }
    }
}