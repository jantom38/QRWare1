package com.example.shared.ui.screens.basic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.ui.navigation.Navigator
import com.example.shared.data.model.UserInfoResponse
import com.example.shared.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(navigator: Navigator, viewModel: HomeViewModel) {
    val userState by viewModel.userState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            userState.let { user ->
                if (user != null) {
                    UserInfoCard(user)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    fun hasPermission(permission: String) = user.permissions.contains(permission)
                    fun hasRole(role: String) = user.roles.contains(role)

                    if (hasRole("ADMIN") || hasPermission("ADMIN_FULL")) {
                        MenuButton(
                            text = "Zarządzanie Użytkownikami",
                            onClick = { navigator.navigate("manage_users") }
                        )
                    }

                    if (hasPermission("PRODUCT_READ") || hasPermission("PRODUCT_WRITE")) {
                        MenuButton(
                            text = "Zarządzanie Produktami",
                            onClick = { navigator.navigate("manage_products") }
                        )
                        MenuButton(
                            text = "Zarządzaj Kategoriami", 
                            onClick = { navigator.navigate("manage_categories") }
                        )
                    }

                    if (hasPermission("INVENTORY_READ") || hasPermission("INVENTORY_WRITE")) {
                        MenuButton(
                            text = "Zarządzanie Stanem Magazynowym",
                            onClick = { navigator.navigate("inventory") }
                        )
                    }

                    if (hasPermission("LOCATION_READ") || hasPermission("LOCATION_WRITE")) {
                        MenuButton(
                            text = "Zarządzanie Lokalizacjami",
                            onClick = { navigator.navigate("manage_locations") }
                        )
                    }

                    if (hasPermission("ZONE_READ") || hasPermission("ZONE_WRITE")) {
                        MenuButton(
                            text = "Zarządzanie Strefami",
                            onClick = { navigator.navigate("manage_zones") }
                        )
                    }

                    if (hasPermission("QR_SCAN")) {
                        MenuButton(
                            text = "Skanuj Kod QR",
                            onClick = { navigator.navigate("qr_scan") }
                        )
                    }

                    if (hasPermission("QR_GENERATE")) {
                        MenuButton(
                            text = "Zarządzaj Kodami QR",
                            onClick = { navigator.navigate("manage_qr") }
                        )
                    }

                    if (hasPermission("MOVEMENT_READ")) {
                        MenuButton(
                            text = "Historia Ruchów",
                            onClick = { navigator.navigate("movement_history") }
                        )
                    }

                    if (hasPermission("ORDER_READ")) {
                        MenuButton(
                            text = "Moje Zamówienia",
                            onClick = { navigator.navigate("my_orders") }
                        )
                    }

                    if (hasPermission("ORDER_WRITE") || hasRole("ADMIN")) {
                        MenuButton(
                            text = "Zarządzanie Zamówieniami",
                            onClick = { navigator.navigate("manage_orders") }
                        )
                    }

                    // Przycisk dostępny dla wszystkich użytkowników
                    MenuButton(
                        text = "Sprawdź Stan Systemu",
                        onClick = { navigator.navigate("health") }
                    )

                    // Przycisk wylogowania
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { 
                            viewModel.logout()
                            navigator.navigateAndClearBackStack("login")
                            /*
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                            */
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Wyloguj się")
                    }

                } else {
                    CircularProgressIndicator()
                }
            }
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

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text)
    }
    Spacer(modifier = Modifier.height(8.dp))
}