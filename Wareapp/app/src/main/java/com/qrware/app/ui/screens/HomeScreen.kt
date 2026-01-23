package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val userState by viewModel.userState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Scaffold { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
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
                            onClick = { navController.navigate("manage_users") }
                        )
                    }

                    if (hasPermission("PRODUCT_READ") || hasPermission("PRODUCT_WRITE")) {
                        MenuButton(
                            text = "Zarządzanie Produktami",
                            onClick = { navController.navigate("manage_products") }
                        )
                        MenuButton(
                            text = "Zarządzaj Kategoriami", 
                            onClick = { navController.navigate("manage_categories") }
                        )
                    }

                    if (hasPermission("INVENTORY_READ") || hasPermission("INVENTORY_WRITE")) {
                        MenuButton(
                            text = "Zarządzanie Stanem Magazynowym",
                            onClick = { navController.navigate("inventory") }
                        )
                    }

                    if (hasPermission("LOCATION_READ") || hasPermission("LOCATION_WRITE")) {
                        MenuButton(
                            text = "Zarządzanie Lokalizacjami",
                            onClick = { navController.navigate("manage_locations") }
                        )
                    }

                    if (hasPermission("ZONE_READ") || hasPermission("ZONE_WRITE")) {
                        MenuButton(
                            text = "Zarządzanie Strefami",
                            onClick = { navController.navigate("manage_zones") }
                        )
                    }

                    if (hasPermission("QR_SCAN")) {
                        MenuButton(
                            text = "Skanuj Kod QR",
                            onClick = { navController.navigate("qr_scan") }
                        )
                    }

                    if (hasPermission("QR_GENERATE")) {
                        MenuButton(
                            text = "Zarządzaj Kodami QR",
                            onClick = { navController.navigate("manage_qr") }
                        )
                    }

                    if (hasPermission("MOVEMENT_READ")) {
                        MenuButton(
                            text = "Historia Ruchów",
                            onClick = { navController.navigate("movement_history") }
                        )
                    }

                    if (hasPermission("ORDER_READ")) {
                        MenuButton(
                            text = "Moje Zlecenia",
                            onClick = { navController.navigate("my_orders") }
                        )
                    }

                    if (hasPermission("ORDER_WRITE") || hasRole("ADMIN")) {
                        MenuButton(
                            text = "Zarządzanie Zleceniami",
                            onClick = { navController.navigate("manage_orders") }
                        )
                    }

                    MenuButton(
                        text = "Sprawdź Stan Systemu",
                        onClick = { navController.navigate("health") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { 
                            viewModel.logout()
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
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