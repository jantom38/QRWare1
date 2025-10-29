package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrware.app.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("user") } // Przykładowe dane
    var password by remember { mutableStateOf("password") } // Przykładowe dane

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
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
            TextButton(onClick = { navController.navigate("register") }) {
                Text("Nie masz konta? Zarejestruj się")
            }
        }
    }
}