package com.qrware.app.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.screens.*
import com.qrware.app.ui.screens.LocationManagement.AddLocationScreen
import com.qrware.app.ui.screens.LocationManagement.EditLocationScreen
import com.qrware.app.ui.screens.LocationManagement.ManageLocationsScreen
import com.qrware.app.ui.screens.ProductManagement.AddProductScreen
import com.qrware.app.ui.screens.ProductManagement.EditProductScreen
import com.qrware.app.ui.screens.ProductManagement.ManageProductsScreen
import com.qrware.app.ui.screens.ProductManagement.ProductDetailsScreen
import com.qrware.app.ui.screens.InventoryDetailsScreen
import com.qrware.app.ui.screens.MovementHistoryScreen
import com.qrware.app.ui.screens.UserManagement.AddUserScreen
import com.qrware.app.ui.screens.UserManagement.AdminRoutes
import com.qrware.app.ui.screens.UserManagement.EditUserScreen
import com.qrware.app.ui.screens.UserManagement.ListUsersScreen
import com.qrware.app.ui.screens.UserManagement.ManagePermissionsScreen
import com.qrware.app.ui.screens.UserManagement.ManageRolesScreen
import com.qrware.app.ui.screens.UserManagement.ManageUsersScreen
import com.qrware.app.ui.viewmodel.*
import com.qrware.app.ui.viewmodel.ProductsManagement.AddProductViewModel
import com.qrware.app.ui.viewmodel.UserManagament.AddUserViewModel
import com.qrware.app.ui.viewmodel.UserManagament.EditUserViewModel
import com.qrware.app.ui.viewmodel.UserManagament.ListUsersViewModel
import com.qrware.app.ui.viewmodel.UserManagament.ListUsersViewModelFactory
import com.qrware.app.ui.viewmodel.UserManagament.LoginViewModel
import com.qrware.app.ui.viewmodel.UserManagament.LoginViewModelFactory
import com.qrware.app.ui.viewmodel.UserManagament.RegisterViewModel
import com.qrware.app.ui.viewmodel.UserManagament.RegisterViewModelFactory
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
                    factory = LoginViewModelFactory(
                        appContainer.authRepository,
                        appContainer.tokenManager
                    )
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
            composable(
                route = "qr_generate?type={type}&id={id}",
                arguments = listOf(
                    navArgument("type") { nullable = true },
                    navArgument("id") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val id = backStackEntry.arguments?.getLong("id").takeIf { it != -1L }

                QRGeneratorScreen(
                    navController = navController,
                    appContainer = appContainer,
                    initialType = type,
                    initialEntityId = id
                )
            }
            composable(
                route = "products/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId")
                // ProductDetailsScreen(productId = productId) <- Twój ekran szczegółów
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

            composable("inventory") {
                ManageInventoryScreen(navController = navController, appContainer = appContainer)
            }

            composable("manage_products") {
                ManageProductsScreen(navController = navController, appContainer = appContainer)
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
            // ... wewnątrz NavHost(...)
            composable(
                route = "edit_product/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId")
                if (productId != null) {
                    EditProductScreen(
                        navController = navController,
                        appContainer = appContainer,
                        productId = productId
                    )
                } else {
                    // Obsłuż błąd - np. wróć
                    navController.popBackStack()
                }
            }


            composable("manage_categories") {
                ManageCategoriesScreen(navController = navController, appContainer = appContainer)
            }

            composable("qr_scan") {
                QRScanScreen(navController = navController, appContainer = appContainer)
            }
            composable ( "server_settings")
            {
                ServerSettingsScreen(navController=navController, appContainer= appContainer)
            }

            composable("manage_qr") {
                ManageQRCodesScreen(navController = navController, appContainer = appContainer)
            }

            // Ekran generowania QR kodu z parametrami
            composable(
                route = "generate_qr/{type}/{id}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("id") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val qrType = backStackEntry.arguments?.getString("type") ?: "PRODUCT"
                val entityId = backStackEntry.arguments?.getLong("id") ?: 0L
                ManageQRCodesScreen(
                    navController = navController,
                    appContainer = appContainer,
                    initialType = qrType,
                    initialEntityId = entityId
                )
            }

            composable(
                route = "product_details/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                ProductDetailsScreen(
                    navController = navController,
                    appContainer = appContainer,
                    productId = productId
                )
            }

            // Ekran dodawania pozycji do magazynu z predefiniowanym produktem
            composable(
                route = "add_inventory/{type}/{id}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("id") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val entityType = backStackEntry.arguments?.getString("type") ?: "PRODUCT"
                val entityId = backStackEntry.arguments?.getLong("id") ?: 0L
                AddInventoryScreen(
                    navController = navController,
                    appContainer = appContainer,
                    presetProductId = if (entityType == "PRODUCT") entityId else null
                )
            }
            // Lokalizacje
            composable("manage_locations") {
                ManageLocationsScreen(
                    navController = navController,
                    appContainer = appContainer
                )
            }

            composable("add_location") {
                AddLocationScreen(
                    navController = navController,
                    appContainer = appContainer
                )
            }

            composable(
                route = "edit_location/{locationId}",
                arguments = listOf(navArgument("locationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val locationId = backStackEntry.arguments?.getLong("locationId")
                if (locationId != null) {
                    EditLocationScreen(
                        navController = navController,
                        appContainer = appContainer,
                        locationId = locationId
                    )
                } else {
                    // Obsłuż błąd
                    navController.popBackStack()
                }
            }

            // Ekran szczegółów pozycji magazynowej
            composable(
                route = "inventory_details/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId")
                if (itemId != null) {
                    InventoryDetailsScreen(
                        navController = navController,
                        appContainer = appContainer,
                        itemId = itemId
                    )
                } else {
                    // Obsłuż błąd
                    navController.popBackStack()
                }
            }

            // Ekran historii ruchów magazynowych
            composable("movement_history") {
                MovementHistoryScreen(
                    navController = navController,
                    appContainer = appContainer
                )
            }

            // Historia ruchów dla konkretnej pozycji magazynowej
            composable(
                route = "movement_history/item/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId")
                if (itemId != null) {
                    MovementHistoryScreen(
                        navController = navController,
                        appContainer = appContainer,
                        itemId = itemId
                    )
                } else {
                    navController.popBackStack()
                }
            }

            // Historia ruchów dla produktu
            composable(
                route = "movement_history/product/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId")
                if (productId != null) {
                    MovementHistoryScreen(
                        navController = navController,
                        appContainer = appContainer,
                        productId = productId
                    )
                } else {
                    navController.popBackStack()
                }
            }

            // Historia ruchów dla lokalizacji
            composable(
                route = "movement_history/location/{locationId}",
                arguments = listOf(navArgument("locationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val locationId = backStackEntry.arguments?.getLong("locationId")
                if (locationId != null) {
                    MovementHistoryScreen(
                        navController = navController,
                        appContainer = appContainer,
                        locationId = locationId
                    )
                } else {
                    navController.popBackStack()
                }
            }
        }
    }
}

