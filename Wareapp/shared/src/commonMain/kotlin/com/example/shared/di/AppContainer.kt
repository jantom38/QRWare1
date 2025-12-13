package com.example.shared.di

import com.example.shared.data.api.MovementHistoryApiService
import com.example.shared.data.api.OrderApiService
import com.example.shared.data.preferences.ServerConfigManager
import com.example.shared.data.remote.*
import com.example.shared.data.repository.*
import com.example.shared.security.TokenManager
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient

class AppContainer(private val settings: Settings) {

    // Managers
    val tokenManager = TokenManager(settings)
    // Zakładam, że ServerConfigManager też przerobiłeś na Settings
    val serverConfigManager = ServerConfigManager(settings)

    init {
        NetworkModule.init(serverConfigManager)
    }

    // Network Client
    private val httpClient: HttpClient by lazy {
        NetworkModule.createClient(tokenManager)
    }

    // Services (Ktor implementation)
    val authService: AuthService by lazy { AuthService(httpClient) }
    val testService: TestService by lazy { TestService(httpClient) }
    val healthService: HealthService by lazy { HealthService(httpClient) }
    val apiService: ApiService by lazy { ApiService(httpClient) }
    val movementHistoryApiService: MovementHistoryApiService by lazy { MovementHistoryApiService(httpClient) }
    val orderApiService: OrderApiService by lazy { OrderApiService(httpClient) }

    // Repositories
    val authRepository: AuthRepository by lazy { AuthRepository(authService) }
    val testRepository: TestRepository by lazy { TestRepository(testService) }
    val healthRepository: HealthRepository by lazy { HealthRepository(healthService) }
    val locationRepository: LocationRepository by lazy { LocationRepository(apiService) }
    val userManagementRepository: UserManagementRepository by lazy { UserManagementRepository(apiService) }
    val inventoryRepository: InventoryRepository by lazy { InventoryRepository(apiService) }
    val productRepository: ProductRepository by lazy { ProductRepository(apiService) }
    val categoryRepository: CategoryRepository by lazy { CategoryRepository(apiService) }
    val qrCodeRepository: QRCodeRepository by lazy { QRCodeRepository(apiService) }
    val zoneRepository: ZoneRepository by lazy { ZoneRepository(apiService) }
    val movementHistoryRepository: MovementHistoryRepository by lazy { MovementHistoryRepository(movementHistoryApiService) }
    val orderRepository: OrderRepository by lazy { OrderRepository(orderApiService) }
    val orderItemRepository: OrderItemRepository by lazy { OrderItemRepository(orderApiService) }

    fun refreshNetworkConfig() {
        NetworkModule.init(serverConfigManager)
        // W Ktorze zwykle nie trzeba resetować klienta, chyba że zmienia się URL bazowy wewnątrz NetworkModule.
        // Jeśli tak, tutaj możesz dodać logikę resetu.
    }
}