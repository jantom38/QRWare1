package com.example.shared.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shared.di.AppContainer
import com.example.shared.ui.navigation.Navigator
import com.example.shared.ui.screens.InventoryManagement.InventoryDetailsScreen
import com.example.shared.ui.screens.InventoryManagement.ManageInventoryScreen
import com.example.shared.ui.screens.InventoryManagement.AddInventoryScreen
import com.example.shared.ui.screens.LocationManagement.AddLocationScreen
import com.example.shared.ui.screens.LocationManagement.EditLocationScreen
import com.example.shared.ui.screens.LocationManagement.ManageLocationsScreen
import com.example.shared.ui.screens.HistoryManagement.MovementHistoryScreen
import com.example.shared.ui.screens.OrderManagement.*
import com.example.shared.ui.screens.ProductManagement.AddProductScreen
import com.example.shared.ui.screens.ProductManagement.EditProductScreen
import com.example.shared.ui.screens.ProductManagement.ManageProductsScreen
import com.example.shared.ui.screens.ProductManagement.ProductDetailsScreen
import com.example.shared.ui.screens.QRGeneratorScreen
import com.example.shared.ui.screens.UserManagement.*
import com.example.shared.ui.screens.ZoneManagement.AddZoneScreen
import com.example.shared.ui.screens.ZoneManagement.EditZoneScreen
import com.example.shared.ui.screens.ZoneManagement.ManageZonesScreen
import com.example.shared.ui.screens.basic.*
import com.example.shared.ui.viewmodel.*
import com.example.shared.ui.viewmodel.UserManagament.*
import com.example.shared.ui.viewmodel.ProductsManagement.*

@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()

    val startDestination = if (appContainer.tokenManager.getAccessToken().isNullOrBlank()) "login" else "home"

    val navigator = object : Navigator {
        override fun navigate(route: String) { navController.navigate(route) }
        override fun navigateUp() { navController.navigateUp() }
        override fun popBackStack(): Boolean = navController.popBackStack()
        override fun navigateAndClearBackStack(route: String) {
            while (navController.popBackStack()) {}
            navController.navigate(route)
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            val loginViewModel = remember { LoginViewModel(appContainer.authRepository, appContainer.tokenManager) }
            LoginScreen(navigator = navigator, viewModel = loginViewModel)
        }

        composable("register") {
            val registerViewModel = remember {
                RegisterViewModel(appContainer.authRepository)
            }
            RegisterScreen(navigator = navigator, viewModel = registerViewModel)
        }

        composable("home") {
            val homeViewModel = remember {
                HomeViewModel(appContainer.authRepository)
            }
            HomeScreen(navigator = navigator, viewModel = homeViewModel)
        }

        composable("inventory") {
            val viewModel = viewModel<ManageInventoryViewModel> {
                ManageInventoryViewModel(appContainer.inventoryRepository)
            }
            ManageInventoryScreen(navController = navController, viewModel = viewModel)
        }

        composable(
            route = "inventory_details/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable

            val viewModel = viewModel<InventoryDetailsViewModel> {
                InventoryDetailsViewModel(appContainer.inventoryRepository, itemId)
            }
            InventoryDetailsScreen(navController = navController, viewModel = viewModel, itemId = itemId)
        }

        composable(
            route = "add_inventory/{type}/{id}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val entityType = backStackEntry.arguments?.getString("type") ?: "PRODUCT"
            val entityId = backStackEntry.arguments?.getLong("id") ?: 0L

            val viewModel = viewModel<AddInventoryViewModel> {
                AddInventoryViewModel(
                    appContainer.inventoryRepository,
                    appContainer.productRepository,
                    appContainer.locationRepository
                )
            }

            AddInventoryScreen(
                navController = navController,
                viewModel = viewModel,
                presetProductId = if (entityType == "PRODUCT") entityId else null
            )
        }

//        composable("qr_generate?type={type}&id={id}") { backStackEntry ->
//            val type = backStackEntry.arguments?.getString("type")
//            val id = backStackEntry.arguments?.getLong("id").takeIf { it != -1L }
//
//            val viewModel = viewModel<QRCodeViewModel> {
//                QRCodeViewModel(appContainer.qrCodeRepository)
//            }
//            QRGeneratorScreen(navigator = navigator, viewModel = viewModel, initialType = type, initialEntityId = id)
//        }

        composable("health") {
            val viewModel = remember {
                HealthViewModel(appContainer.healthRepository)
            }
            HealthCheckScreen(navigator = navigator, viewModel = viewModel)
        }

        composable("manage_users") {
            ManageUsersScreen(navController = navController)
        }

        composable("manage_users_list") {
            val viewModel = viewModel<ListUsersViewModel> {
                ListUsersViewModel(appContainer.userManagementRepository)
            }
            ListUsersScreen(navController = navController, viewModel = viewModel)
        }

        composable("manage_products") {
            val viewModel = viewModel<ManageProductsViewModel> {
                ManageProductsViewModel(appContainer.productRepository)
            }
            ManageProductsScreen(navController = navController, viewModel = viewModel)
        }

        composable("server_settings") {
            ServerSettingsScreen(navigator = navigator, appContainer = appContainer)
        }
    }
}