package com.qrware.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qrware.desktop.ui.screens.HomeScreen
import com.qrware.desktop.ui.screens.LoginScreen
import com.qrware.desktop.ui.screens.ProductsScreen
import com.qrware.desktop.ui.screens.InventoryScreen
import com.qrware.desktop.ui.theme.QRWareDesktopTheme

@Composable
fun QRWareDesktopApp() {
    QRWareDesktopTheme {
        var isAuthenticated by remember { mutableStateOf(false) }
        var currentScreen by remember { mutableStateOf("home") }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isAuthenticated) {
                when (currentScreen) {
                    "home" -> HomeScreen(
                        onLogout = { isAuthenticated = false },
                        onNavigateToProducts = { currentScreen = "products" },
                        onNavigateToInventory = { currentScreen = "inventory" }
                    )
                    "products" -> ProductsScreen(
                        onBack = { currentScreen = "home" }
                    )
                    "inventory" -> InventoryScreen(
                        onBack = { currentScreen = "home" }
                    )
                }
            } else {
                LoginScreen(
                    onLoginSuccess = { isAuthenticated = true }
                )
            }
        }
    }
}