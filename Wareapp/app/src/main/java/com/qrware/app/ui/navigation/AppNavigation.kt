package com.qrware.app.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

            // --- SEKCJA ZARZĄDZANIA UŻYTKOWNIKAMI ---

            // 1. Główny ekran "hub" z kafelkami
            composable("manage_users") {
                ManageUsersScreen(navController = navController)
            }

            // 2. Ekran listy użytkowników (docelowy)
            composable("manage_users_list") {
                // Tworzymy ViewModel używając Factory i AppContainer
                val listUsersViewModel: ListUsersViewModel = viewModel(
                    factory = ListUsersViewModelFactory(appContainer.userManagementRepository)
                )
                ListUsersScreen(navController = navController, viewModel = listUsersViewModel)
            }

            // 3. Ekran dodawania użytkownika (docelowy)

            // 4. Ekran zarządzania rolami (docelowy)
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

            // --- POZOSTAŁE TRASY (BEZ DUPLIKATÓW) ---

            composable("manage_products") {
                ManageProductsScreen(navController = navController)
            }
            composable("inventory") {
                InventoryScreen(navController = navController)
            }
            composable("scan_qr") {
                ScanQrScreen(navController = navController)
            }

            composable(
                route = "admin_edit_user/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.LongType })
            ) { backStackEntry ->

                // Pobierz ID użytkownika z argumentów nawigacji
                val userId = backStackEntry.arguments?.getLong("userId")

                if (userId == null) {
                    // Obsługa błędu - nawiguj z powrotem, jeśli nie ma ID
                    Text("Błąd: Brak ID użytkownika")
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                } else {
                    // Utwórz ViewModel używając dedykowanej fabryki
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
                // Utwórz ViewModel używając dedykowanej fabryki
                val addUserViewModel: AddUserViewModel = viewModel(
                    factory = appContainer.addUserViewModelFactory
                )
                AddUserScreen(
                    navController = navController,
                    viewModel = addUserViewModel
                )
            }
            }
        }
    }


