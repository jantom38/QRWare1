package com.qrware.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.screens.*
import com.qrware.app.ui.screens.UserManagement.AddUserScreen
import com.qrware.app.ui.screens.UserManagement.AdminRoutes
import com.qrware.app.ui.screens.UserManagement.EditUserScreen
import com.qrware.app.ui.screens.UserManagement.ListUsersScreen
import com.qrware.app.ui.screens.UserManagement.ManagePermissionsScreen
import com.qrware.app.ui.screens.UserManagement.ManageRolesScreen
import com.qrware.app.ui.screens.UserManagement.ManageUsersScreen
import com.qrware.app.ui.viewmodel.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Sprawdź token, aby zdecydować o ekranie startowym
    LaunchedEffect(key1 = Unit) {
        coroutineScope.launch {
            val token = appContainer.tokenManager.getToken.first()
            startDestination = if (token.isNullOrBlank()) "login" else "login"
        }
    }
    if (startDestination != null) {
        NavHost(navController = navController, startDestination = startDestination!!) {
            // ... (Trasy login, register, home, health bez zmian) ...
            composable("login") {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(appContainer.authRepository, appContainer.tokenManager)
                )
                LoginScreen(navController = navController, viewModel = loginViewModel)
            }
            composable("register") {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = RegisterViewModelFactory(appContainer.authRepository)
                )
                RegisterScreen(navController = navController, viewModel = registerViewModel)
            }
            composable("home") {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(appContainer.authRepository)
                )
                HomeScreen(navController = navController, viewModel = homeViewModel)
            }
            composable("health") {
                val healthViewModel: HealthViewModel = viewModel(
                    factory = HealthViewModelFactory(appContainer.healthRepository)
                )
                HealthCheckScreen(navController = navController, viewModel = healthViewModel)
            }

            // ... (Sekcja Zarządzania Użytkownikami bez zmian) ...
            composable("manage_users") {
                ManageUsersScreen(navController = navController)
            }
            composable("manage_users_list") {
                val listUsersViewModel: ListUsersViewModel = viewModel(
                    factory = ListUsersViewModelFactory(appContainer.userManagementRepository)
                )
                ListUsersScreen(navController = navController, viewModel = listUsersViewModel)
            }
            composable(AdminRoutes.MANAGE_ROLES) {
                ManageRolesScreen(
                    navController = navController,
                    viewModel = viewModel(factory = appContainer.manageRolesViewModelFactory)
                )
            }
            composable(AdminRoutes.MANAGE_PERMISSIONS) {
                ManagePermissionsScreen(
                    navController = navController,
                    viewModel = viewModel(factory = appContainer.managePermissionsViewModelFactory)
                )
            }
            composable(
                route = "admin_edit_user/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.LongType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getLong("userId")
                if (userId == null) {
                    Text("Błąd: Brak ID użytkownika")
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    val editUserViewModel: EditUserViewModel = viewModel(
                        factory = appContainer.createEditUserViewModelFactory(userId)
                    )
                    EditUserScreen(
                        navController = navController,
                        viewModel = editUserViewModel
                    )
                }
            }
            composable("admin_add_user") {
                val addUserViewModel: AddUserViewModel = viewModel(
                    factory = appContainer.addUserViewModelFactory
                )
                AddUserScreen(
                    navController = navController,
                    viewModel = addUserViewModel
                )
            }

            // --- SEKCJA PRODUKTÓW I MAGAZYNU ---

            composable("manage_products") {
                // TODO: Zastąp tymczasowy tekst prawdziwym ekranem
                Text("Ekran Zarządzania Produktami", modifier = Modifier.padding(32.dp).fillMaxSize())
            }

            composable("inventory") {
                InventoryScreen(navController = navController, appContainer = appContainer)
            }

            // --- NOWA TRASA DLA DODAWANIA PRODUKTU ---
            composable("add_product") {
                // Upewnij się, że masz `addProductViewModelFactory` w AppContainer
                // (Instrukcje, jak to dodać, znajdziesz poniżej)
                val addProductViewModel: AddProductViewModel = viewModel(
                    factory = appContainer.addProductViewModelFactory
                )
                AddProductScreen(
                    navController = navController,
                    viewModel = addProductViewModel
                )
            }

            composable("scan_qr") {
                ScanQrScreen(navController = navController)
            }
        }
    }
}