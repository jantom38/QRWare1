package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrware.app.data.model.UserInfoResponse
import com.qrware.app.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    // Stan użytkownika jest jedynym stanem pobieranym z HomeViewModel
    val userState by viewModel.userState.collectAsState()

    // Usunięto stany publicData i protectedData, ponieważ nie ma ich już w ViewModel

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Scaffold(
        // Możesz tu dodać TopAppBar, jeśli jest potrzebny

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Wycentrowanie dla CircularProgressIndicator
        ) {
            // Sprawdzamy, czy dane użytkownika zostały załadowane
            userState.let { user ->
                if (user != null) {
                    // Karta z informacjami o użytkowniku
                    UserInfoCard(user)

                    // Sprawdzenie, czy użytkownik ma rolę "ADMIN"
                    val isAdmin = user.roles.contains("ADMIN")

                    // Przyciski administracyjne widoczne tylko dla admina
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("manage_users") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Zarządzanie Użytkownikami")
                        }
                        Spacer(modifier = Modifier.height(8.dp)) // Dodatkowy odstęp
                        Button(
                            onClick = { navController.navigate("inventory") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Zarządzaj Produktami")
                        }
                    }

                    // Przycisk widoczny dla wszystkich zalogowanych użytkowników
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("health") },
                        modifier = Modifier.fillMaxWidth() // Ujednolicenie wyglądu
                    ) {
                        Text("Sprawdź Stan Systemu")
                    }

                    // TODO: Dodaj tutaj inne przyciski nawigacyjne, np. Skanuj QR, Inwentaryzacja
                    // Spacer(modifier = Modifier.height(8.dp))
                    // Button(onClick = { navController.navigate("scan_qr") }, ...)
                    // Spacer(modifier = Modifier.height(8.dp))
                    // Button(onClick = { navController.navigate("inventory") }, ...)


                } else {
                    // Wyświetlamy wskaźnik ładowania, dopóki dane użytkownika nie zostaną pobrane
                    CircularProgressIndicator()
                }
            }

            // Usunięto EndpointResultCard dla "Public Endpoint" i "Protected Endpoint"
        }
    }
}

@Composable
fun UserInfoCard(user: UserInfoResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Witaj, ${user.fullName ?: user.username}!",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Email: ${user.email}")
            Text("Role: ${user.roles.joinToString(", ")}")
        }
    }
}

// Usunięto funkcję EndpointResultCard, ponieważ nie jest już używana