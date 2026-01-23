package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrware.app.data.model.SystemStatus
import com.qrware.app.ui.viewmodel.HealthUiState
import com.qrware.app.ui.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCheckScreen(navController: NavController, viewModel: HealthViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Health Status") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HealthUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HealthUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HealthUiState.Success -> {
                    StatusDetails(state.systemStatus)
                }
            }
        }
    }
}

@Composable
fun StatusDetails(status: SystemStatus) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val statusColor = if (status.status == "UP") Color(0xFF388E3C) else Color.Red
            StatusCard(title = "Overall Status") {
                StatusRow("Status", status.status, valueColor = statusColor)
                StatusRow("Application", status.application)
                StatusRow("Version", status.version)
                StatusRow("Uptime", status.uptime)
            }
        }
        item {
            StatusCard(title = "Database") {
                val dbStatus = status.database["status"] as? String ?: "UNKNOWN"
                val dbStatusColor = if (dbStatus == "UP") Color(0xFF388E3C) else Color.Red
                StatusRow("Status", dbStatus, valueColor = dbStatusColor)
                StatusRow("Product", status.database["productName"] as? String ?: "N/A")
                StatusRow("URL", status.database["url"] as? String ?: "N/A")
            }
        }
        item {
            StatusCard(title = "Memory") {
                status.memory.forEach { (key, value) ->
                    StatusRow(key.replaceFirstChar { it.uppercase() }, value)
                }
            }
        }
        item {
            StatusCard(title = "System") {
                status.system.forEach { (key, value) ->
                    StatusRow(key.replaceFirstChar { it.uppercase() }, value)
                }
            }
        }
    }
}

@Composable
fun StatusCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
fun StatusRow(label: String, value: String, valueColor: Color = LocalContentColor.current) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(text = value, color = valueColor, modifier = Modifier.weight(1f))
    }
}