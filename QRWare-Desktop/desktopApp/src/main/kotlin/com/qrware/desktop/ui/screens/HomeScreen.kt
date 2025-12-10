package com.qrware.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qrware.shared.data.model.UserInfo
import com.qrware.shared.di.SharedDI
import kotlinx.coroutines.flow.collectAsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    val authRepository = remember { SharedDI.getInstance().getAuthRepository() }
    val currentUser = authRepository.getCurrentUser()
    val scope = rememberCoroutineScope()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QRWare - Warehouse Management") },
                actions = {
                    // User info
                    currentUser?.let { user ->
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    
                    // Settings
                    IconButton(onClick = { /* TODO: Open settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    
                    // Logout
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Welcome message
                currentUser?.let { user ->
                    Card(
                        modifier = Modifier.padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Welcome back, ${user.firstName}!",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Role: ${user.roles.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (user.lastLoginAt != null) {
                                Text(
                                    text = "Last login: ${user.lastLoginAt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Quick actions grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    getQuickActions().forEach { action ->
                        QuickActionCard(
                            icon = action.icon,
                            title = action.title,
                            description = action.description,
                            onClick = action.onClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            authRepository.logout()
                        }
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class QuickAction(
    val icon: @Composable () -> Unit,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

@Composable
private fun getQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction(
            icon = { Icon(Icons.Default.QrCode, contentDescription = null) },
            title = "Scan QR",
            description = "Scan warehouse items",
            onClick = { /* TODO: Navigate to QR scanner */ }
        ),
        QuickAction(
            icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
            title = "Inventory",
            description = "Manage inventory",
            onClick = { /* TODO: Navigate to inventory */ }
        ),
        QuickAction(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            title = "Orders",
            description = "View orders",
            onClick = { /* TODO: Navigate to orders */ }
        ),
        QuickAction(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            title = "Locations",
            description = "Manage locations",
            onClick = { /* TODO: Navigate to locations */ }
        ),
        QuickAction(
            icon = { Icon(Icons.Default.Category, contentDescription = null) },
            title = "Products",
            description = "Manage products",
            onClick = { /* TODO: Navigate to products */ }
        ),
        QuickAction(
            icon = { Icon(Icons.Default.People, contentDescription = null) },
            title = "Users",
            description = "User management",
            onClick = { /* TODO: Navigate to users */ }
        )
    )
}