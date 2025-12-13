package com.example.shared.ui.screens.basic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.shared.ui.navigation.Navigator
import com.example.shared.ui.viewmodel.UserManagament.LoginViewModel

@Composable
fun LoginScreen(navigator: Navigator, viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("user") } // Przykładowe dane
    var password by remember { mutableStateOf("password") } // Przykładowe dane

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            navigator.navigateAndClearBackStack("home")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Przycisk ustawień w prawym górnym rogu
        IconButton(
            onClick = { navigator.navigate("server_settings") },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ustawienia serwera",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("QRWare Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username or Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.login(username, password) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Login")
            }
        }

        uiState.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
            TextButton(onClick = { navigator.navigate("register") }) {
                Text("Nie masz konta? Zarejestruj się")
            }
        }
        }
    }
}