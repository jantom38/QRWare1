package com.qrware.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qrware.desktop.ui.screens.HomeScreen
import com.qrware.desktop.ui.screens.LoginScreen
import com.qrware.desktop.ui.theme.QRWareDesktopTheme

@Composable
fun QRWareDesktopApp() {
    QRWareDesktopTheme {
        var isAuthenticated by remember { mutableStateOf(false) }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isAuthenticated) {
                HomeScreen(
                    onLogout = { isAuthenticated = false }
                )
            } else {
                LoginScreen(
                    onLoginSuccess = { isAuthenticated = true }
                )
            }
        }
    }
}