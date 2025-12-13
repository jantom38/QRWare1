package com.qrware.app.ui.screens.basic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrware.app.di.AppContainer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSettingsScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val serverConfigManager = appContainer.serverConfigManager
    
    var serverIp by remember { mutableStateOf(serverConfigManager.getServerIp()) }
    var serverPort by remember { mutableStateOf(serverConfigManager.getServerPort()) }
    var useHttps by remember { mutableStateOf(serverConfigManager.getUseHttps()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia Serwera") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    // Przycisk resetowania do domyślnych
                    IconButton(
                        onClick = {
                            serverConfigManager.resetToDefaults()
                            serverIp = serverConfigManager.getServerIp()
                            serverPort = serverConfigManager.getServerPort()
                            useHttps = serverConfigManager.getUseHttps()
                            showSuccessMessage = true
                        }
                    ) {
                        Icon(Icons.Default.RestoreFromTrash, contentDescription = "Reset do domyślnych")
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Aktualny URL serwera
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Aktualny URL serwera:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = serverConfigManager.getServerUrl(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Adres IP serwera
            OutlinedTextField(
                value = serverIp,
                onValueChange = { serverIp = it },
                label = { Text("Adres IP serwera") },
                placeholder = { Text("np. 192.168.1.100") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Port serwera
            OutlinedTextField(
                value = serverPort,
                onValueChange = { serverPort = it },
                label = { Text("Port serwera") },
                placeholder = { Text("np. 8080") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // HTTPS switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Używaj HTTPS",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Bezpieczne połączenie SSL",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = useHttps,
                    onCheckedChange = { useHttps = it }
                )
            }

            // Popularne IP dla deweloperów
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Popularne adresy IP:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    serverConfigManager.getCommonDevIPs().forEach { ip ->
                        TextButton(
                            onClick = { serverIp = ip },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(ip)
                                if (ip == "10.0.2.2") {
                                    Text("(Android Emulator)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // Przycisk zapisu
            Button(
                onClick = {
                    serverConfigManager.setServerIp(serverIp.trim())
                    serverConfigManager.setServerPort(serverPort.trim())
                    serverConfigManager.setUseHttps(useHttps)
                    appContainer.refreshNetworkConfig()
                    showSuccessMessage = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serverIp.isNotBlank() && serverPort.isNotBlank()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Zapisz i zastosuj")
            }

            // Informacja o ponownym uruchomieniu
            if (serverConfigManager.isCustomConfig()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "💡 Tip: Po zmianie ustawień serwera może być konieczne ponowne zalogowanie.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Komunikat sukcesu
            if (showSuccessMessage) {
                LaunchedEffect(Unit) {
                    delay(3000)
                    showSuccessMessage = false
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "✅ Ustawienia zostały zapisane!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}