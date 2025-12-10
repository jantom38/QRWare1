package com.qrware.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "QRWare - Warehouse Management",
                        fontWeight = FontWeight.Medium
                    ) 
                },
                actions = {
                    // User info
                    Text(
                        text = "Welcome, Admin",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Welcome Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Dashboard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Welcome to QRWare Desktop! Select an action below to get started.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quick Actions Grid
            Text(
                text = "Quick Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(getQuickActions()) { action ->
                    QuickActionCard(
                        icon = action.icon,
                        title = action.title,
                        description = action.description,
                        onClick = action.onClick
                    )
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
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class QuickAction(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

private fun getQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction(
            icon = Icons.Default.QrCode,
            title = "QR Scanner",
            description = "Scan warehouse items",
            onClick = { /* TODO: Navigate to QR scanner */ }
        ),
        QuickAction(
            icon = Icons.Default.Inventory,
            title = "Inventory",
            description = "Manage inventory",
            onClick = { /* TODO: Navigate to inventory */ }
        ),
        QuickAction(
            icon = Icons.Default.ShoppingCart,
            title = "Orders",
            description = "View and manage orders",
            onClick = { /* TODO: Navigate to orders */ }
        ),
        QuickAction(
            icon = Icons.Default.LocationOn,
            title = "Locations",
            description = "Manage warehouse locations",
            onClick = { /* TODO: Navigate to locations */ }
        ),
        QuickAction(
            icon = Icons.Default.Category,
            title = "Products",
            description = "Manage products",
            onClick = { /* TODO: Navigate to products */ }
        ),
        QuickAction(
            icon = Icons.Default.People,
            title = "Users",
            description = "User management",
            onClick = { /* TODO: Navigate to users */ }
        ),
        QuickAction(
            icon = Icons.Default.Analytics,
            title = "Reports",
            description = "View analytics and reports",
            onClick = { /* TODO: Navigate to reports */ }
        ),
        QuickAction(
            icon = Icons.Default.History,
            title = "Movement History",
            description = "Track item movements",
            onClick = { /* TODO: Navigate to movement history */ }
        ),
        QuickAction(
            icon = Icons.Default.Settings,
            title = "Settings",
            description = "Application settings",
            onClick = { /* TODO: Navigate to settings */ }
        )
    )
}