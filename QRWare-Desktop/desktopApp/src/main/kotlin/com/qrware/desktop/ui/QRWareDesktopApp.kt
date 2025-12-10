package com.qrware.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qrware.desktop.ui.screens.HomeScreen
import com.qrware.desktop.ui.screens.LoginScreen
import com.qrware.desktop.ui.theme.QRWareTheme
import com.qrware.shared.data.model.AuthState
import com.qrware.shared.di.SharedDI
import kotlinx.coroutines.flow.collectAsState

@Composable
fun QRWareDesktopApp() {
    QRWareTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val authRepository = remember { SharedDI.getInstance().getAuthRepository() }
            val authState by authRepository.authState.collectAsState()
            val isAuthenticated by authRepository.isAuthenticated.collectAsState()

            when {
                isAuthenticated -> {
                    HomeScreen(
                        onLogout = {
                            // Handle logout
                        }
                    )
                }
                authState is AuthState.Loading -> {
                    LoadingScreen()
                }
                else -> {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}