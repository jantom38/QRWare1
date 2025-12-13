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
import com.qrware.app.ui.screens.InventoryManagement.InventoryDetailsScreen
import com.qrware.app.ui.screens.InventoryManagement.AddInventoryScreen
import com.qrware.app.ui.screens.InventoryManagement.ManageInventoryScreen
import com.qrware.app.ui.screens.HistoryManagement.MovementHistoryScreen
import com.qrware.app.ui.screens.UserManagement.AddUserScreen
import com.qrware.app.ui.screens.UserManagement.AdminRoutes
import com.qrware.app.ui.screens.UserManagement.EditUserScreen
import com.qrware.app.ui.screens.UserManagement.ListUsersScreen
import com.qrware.app.ui.screens.UserManagement.ManagePermissionsScreen
import com.qrware.app.ui.screens.UserManagement.ManageRolesScreen
import com.qrware.app.ui.screens.UserManagement.ManageUsersScreen
// --- IMPORTY STREF (ZONE) ---
import com.qrware.app.ui.screens.ZoneManagement.AddZoneScreen
import com.qrware.app.ui.screens.ZoneManagement.EditZoneScreen // <--- TEN IMPORT BYŁ POTRZEBNY
import com.qrware.app.ui.screens.ZoneManagement.ManageZonesScreen
import com.qrware.app.ui.screens.OrderManagement.MyOrdersScreen
import com.qrware.app.ui.screens.OrderManagement.OrderDetailsScreen
import com.qrware.app.ui.screens.OrderManagement.QRScanOrderScreen
import com.qrware.app.ui.screens.OrderManagement.ManageOrdersScreen
import com.qrware.app.ui.screens.OrderManagement.CreateOrderScreen
import com.qrware.app.ui.screens.basic.HealthCheckScreen
import com.qrware.app.ui.screens.basic.HomeScreen
import com.qrware.app.ui.screens.basic.LoginScreen
import com.qrware.app.ui.screens.basic.RegisterScreen
import com.qrware.app.ui.screens.basic.ServerSettingsScreen
// ----------------------------
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

            // --- LOGOWANIE I REJESTRACJA ---
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

            // --- QR KODY ---
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

            composable("health") {
                val healthViewModel: HealthViewModel = viewModel(
                    factory = HealthViewModelFactory(appContainer.healthRepository)
                )
                HealthCheckScreen(navController = navController, viewModel = healthViewModel)
            }

            // --- ZARZĄDZANIE UŻYTKOWNIKAMI ---
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

            // --- PRODUKTY I MAGAZYN ---

            composable("inventory") {
                ManageInventoryScreen(navController = navController, appContainer = appContainer)
            }

            composable("manage_products") {
                ManageProductsScreen(navController = navController, appContainer = appContainer)
            }

            composable("add_product") {
                val addProductViewModel: AddProductViewModel = viewModel(
                    factory = appContainer.addProductViewModelFactory
                )
                AddProductScreen(
                    navController = navController,
                    viewModel = addProductViewModel
                )
            }

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
                    navController.popBackStack()
                }
            }

            composable("manage_categories") {
                ManageCategoriesScreen(navController = navController, appContainer = appContainer)
            }

            composable("qr_scan") {
                QRScanScreen(navController = navController, appContainer = appContainer)
            }
            composable ( "server_settings") {
                ServerSettingsScreen(navController = navController, appContainer = appContainer)
            }

            composable("manage_qr") {
                ManageQRCodesScreen(navController = navController, appContainer = appContainer)
            }

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

            // --- ZARZĄDZANIE STREFAMI (ZONES) ---

            composable("manage_zones") {
                ManageZonesScreen(navController = navController, appContainer = appContainer)
            }

            composable("add_zone") {
                AddZoneScreen(navController = navController, appContainer = appContainer)
            }

            // <--- TUTAJ DODAŁEM BRAKUJĄCĄ TRASĘ DO EDYCJI STREFY --->
            composable(
                route = "edit_zone/{zoneId}",
                arguments = listOf(navArgument("zoneId") { type = NavType.LongType })
            ) { backStackEntry ->
                val zoneId = backStackEntry.arguments?.getLong("zoneId")
                if (zoneId != null) {
                    EditZoneScreen(navController = navController, appContainer = appContainer, zoneId = zoneId)
                } else {
                    navController.popBackStack()
                }
            }
            // --------------------------------------------------------

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

            // --- LOKALIZACJE ---

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
                    navController.popBackStack()
                }
            }

            // --- SZCZEGÓŁY INVENTORY ---

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
                    navController.popBackStack()
                }
            }

            // --- HISTORIA RUCHÓW ---

            composable("movement_history") {
                MovementHistoryScreen(
                    navController = navController,
                    appContainer = appContainer
                )
            }

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

            // === ORDER ROUTES ===
            
            composable("my_orders") {
                MyOrdersScreen(
                    navController = navController,
                    orderRepository = appContainer.orderRepository
                )
            }

            composable("manage_orders") {
                ManageOrdersScreen(
                    navController = navController,
                    orderRepository = appContainer.orderRepository
                )
            }

            composable("create_order") {
                CreateOrderScreen(
                    navController = navController,
                    orderRepository = appContainer.orderRepository,
                    userRepository = appContainer.userManagementRepository,
                    locationRepository = appContainer.locationRepository,
                    productRepository = appContainer.productRepository,
                    orderItemRepository = appContainer.orderItemRepository,
                    inventoryRepository = appContainer.inventoryRepository
                )
            }

            composable(
                route = "order_details/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId")
                if (orderId != null) {
                    OrderDetailsScreen(
                        orderId = orderId,
                        navController = navController,
                        orderRepository = appContainer.orderRepository,
                        orderItemRepository = appContainer.orderItemRepository
                    )
                } else {
                    navController.popBackStack()
                }
            }

            composable(
                route = "qr_scan_order/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId")
                if (orderId != null) {
                    QRScanOrderScreen(
                        orderId = orderId,
                        navController = navController,
                        orderRepository = appContainer.orderRepository,
                        orderItemRepository = appContainer.orderItemRepository
                    )
                } else {
                    navController.popBackStack()
                }
            }

            composable(
                route = "qr_scan_item/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId")
                if (itemId != null) {
                    // For individual item scanning, we can reuse QRScanOrderScreen
                    // or create a separate QRScanItemScreen if needed
                    QRScanOrderScreen(
                        orderId = 0L, // Will be ignored for item-specific scanning
                        navController = navController,
                        orderRepository = appContainer.orderRepository,
                        orderItemRepository = appContainer.orderItemRepository
                    )
                } else {
                    navController.popBackStack()
                }
            }
        }
    }
}