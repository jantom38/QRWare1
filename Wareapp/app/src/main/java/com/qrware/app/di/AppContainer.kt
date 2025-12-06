package com.qrware.app.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.qrware.app.data.preferences.ServerConfigManager
import com.qrware.app.data.remote.*
import com.qrware.app.data.repository.*
import com.qrware.app.security.TokenManager
import com.qrware.app.data.remote.ApiService
import com.qrware.app.data.repository.UserManagementRepository
import com.qrware.app.data.api.MovementHistoryApiService
import com.qrware.app.data.repository.MovementHistoryRepository
import com.qrware.app.security.TokenManager as LocalTokenManager
import com.qrware.app.ui.viewmodel.ProductsManagement.AddInventoryViewModelFactory
import com.qrware.app.ui.viewmodel.ProductsManagement.AddProductViewModelFactory
import com.qrware.app.ui.viewmodel.UserManagament.AddUserViewModelFactory
import com.qrware.app.ui.viewmodel.ProductsManagement.CategoryViewModelFactory
import com.qrware.app.ui.viewmodel.ProductsManagement.EditProductViewModelFactory
import com.qrware.app.ui.viewmodel.ProductsManagement.InventoryDetailsViewModelFactory
import com.qrware.app.ui.viewmodel.ProductsManagement.ManageInventoryViewModelFactory
import com.qrware.app.ui.viewmodel.QRCodeViewModelFactory
import com.qrware.app.ui.viewmodel.MovementHistoryViewModelFactory

import com.qrware.app.ui.viewmodel.UserManagament.EditUserViewModelFactory
import com.qrware.app.ui.viewmodel.ProductsManagement.ManageProductsViewModelFactory
import com.qrware.app.ui.viewmodel.UserManagament.ListUsersViewModelFactory
import com.qrware.app.ui.viewmodel.UserManagament.ManagePermissionsViewModelFactory
import com.qrware.app.ui.viewmodel.UserManagament.ManageRolesViewModelFactory
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import com.qrware.app.data.repository.LocationRepository
import com.qrware.app.ui.viewmodel.AddLocationViewModelFactory
import com.qrware.app.ui.viewmodel.EditLocationViewModelFactory
import com.qrware.app.ui.viewmodel.ManageLocationsViewModelFactory

// Importy dla Zone
import com.qrware.app.ui.viewmodel.AddZoneViewModelFactory
import com.qrware.app.ui.viewmodel.EditZoneViewModelFactory
import com.qrware.app.ui.viewmodel.ManageZonesViewModelFactory

class AppContainer(context: Context) {
    val tokenManager = TokenManager(context)
    val serverConfigManager = ServerConfigManager(context)
    val gson = Gson()

    init {
        // Inicjalizuj NetworkModule z konfiguracją serwera
        NetworkModule.init(serverConfigManager)
    }

    // Make these properties that recreate when configuration changes
    private var _okHttpClient: OkHttpClient? = null
    private var _retrofit: Retrofit? = null
    
    private val okHttpClient: OkHttpClient
        get() {
            if (_okHttpClient == null) {
                _okHttpClient = NetworkModule.createClient(tokenManager)
            }
            return _okHttpClient!!
        }
    
    private val retrofit: Retrofit
        get() {
            if (_retrofit == null) {
                _retrofit = NetworkModule.createRetrofit(okHttpClient)
            }
            return _retrofit!!
        }

    // Services - these will now be recreated when retrofit is recreated
    private var _authService: AuthService? = null
    private var _testService: TestService? = null
    private var _healthService: HealthService? = null
    private var _apiService: ApiService? = null
    private var _movementHistoryApiService: MovementHistoryApiService? = null
    private var _orderApiService: com.qrware.app.data.api.OrderApiService? = null
    
    private val authService: AuthService
        get() {
            if (_authService == null) {
                _authService = retrofit.create(AuthService::class.java)
            }
            return _authService!!
        }
    
    private val testService: TestService
        get() {
            if (_testService == null) {
                _testService = retrofit.create(TestService::class.java)
            }
            return _testService!!
        }
    
    private val healthService: HealthService
        get() {
            if (_healthService == null) {
                _healthService = retrofit.create(HealthService::class.java)
            }
            return _healthService!!
        }
    
    private val apiService: ApiService
        get() {
            if (_apiService == null) {
                _apiService = retrofit.create(ApiService::class.java)
            }
            return _apiService!!
        }
    
    private val movementHistoryApiService: MovementHistoryApiService
        get() {
            if (_movementHistoryApiService == null) {
                _movementHistoryApiService = retrofit.create(MovementHistoryApiService::class.java)
            }
            return _movementHistoryApiService!!
        }
    
    private val orderApiService: com.qrware.app.data.api.OrderApiService
        get() {
            if (_orderApiService == null) {
                _orderApiService = retrofit.create(com.qrware.app.data.api.OrderApiService::class.java)
            }
            return _orderApiService!!
        }

    // Repositories
    val authRepository: AuthRepository by lazy { AuthRepository(authService) }
    val testRepository: TestRepository by lazy { TestRepository(testService) }
    val healthRepository: HealthRepository by lazy { HealthRepository(healthService) }

    val locationRepository by lazy {
        LocationRepository(apiService)
    }

    // --- REPOSITORIES ---
    val userManagementRepository by lazy {
        UserManagementRepository(apiService)
    }

    val inventoryRepository by lazy {
        InventoryRepository(apiService)
    }

    val productRepository by lazy {
        ProductRepository(apiService)
    }

    val categoryRepository by lazy {
        CategoryRepository(apiService)
    }

    val qrCodeRepository by lazy {
        QRCodeRepository(apiService)
    }

    val zoneRepository by lazy {
        ZoneRepository(apiService)
    }

    val movementHistoryRepository by lazy {
        MovementHistoryRepository(movementHistoryApiService)
    }

    val orderRepository by lazy {
        com.qrware.app.data.repository.OrderRepository(orderApiService)
    }

    val orderItemRepository by lazy {
        com.qrware.app.data.repository.OrderItemRepository(orderApiService, gson)
    }

    // --- VIEWMODEL FACTORIES ---

    val addProductViewModelFactory by lazy {
        AddProductViewModelFactory(productRepository)
    }

    val addInventoryViewModelFactory by lazy {
        AddInventoryViewModelFactory(inventoryRepository, productRepository, locationRepository)
    }

    // Fabryka dla ListUsersViewModel
    val listUsersViewModelFactory: ViewModelProvider.Factory by lazy {
        ListUsersViewModelFactory(userManagementRepository)
    }
    val addUserViewModelFactory: ViewModelProvider.Factory by lazy {
        AddUserViewModelFactory(userManagementRepository)
    }
    val manageRolesViewModelFactory: ViewModelProvider.Factory by lazy {
        ManageRolesViewModelFactory(userManagementRepository)
    }

    val managePermissionsViewModelFactory: ViewModelProvider.Factory by lazy {
        ManagePermissionsViewModelFactory(userManagementRepository)
    }

    val ProductsViewModelFactory: ViewModelProvider.Factory by lazy {
        ManageProductsViewModelFactory(productRepository)
    }
    val InventoryViewModelFactory: ViewModelProvider.Factory by lazy {
        ManageInventoryViewModelFactory(inventoryRepository)
    }

    val categoryViewModelFactory: ViewModelProvider.Factory by lazy {
        CategoryViewModelFactory(categoryRepository)
    }

    val qrCodeViewModelFactory: ViewModelProvider.Factory by lazy {
        QRCodeViewModelFactory(qrCodeRepository)
    }

    val movementHistoryViewModelFactory: ViewModelProvider.Factory by lazy {
        MovementHistoryViewModelFactory(movementHistoryRepository)
    }

    fun createEditProductViewModelFactory(productId: Long): ViewModelProvider.Factory {
        return EditProductViewModelFactory(productRepository, productId)
    }

    // --- LOCATION FACTORIES ---

    val manageLocationsViewModelFactory: ViewModelProvider.Factory by lazy {
        ManageLocationsViewModelFactory(locationRepository)
    }

    val addLocationViewModelFactory: ViewModelProvider.Factory by lazy {
        AddLocationViewModelFactory(locationRepository)
    }

    fun createEditLocationViewModelFactory(locationId: Long): ViewModelProvider.Factory {
        return EditLocationViewModelFactory(locationRepository, locationId)
    }

    // --- ZONE FACTORIES ---

    val addZoneViewModelFactory: ViewModelProvider.Factory by lazy {
        AddZoneViewModelFactory(zoneRepository)
    }

    fun createEditZoneViewModelFactory(zoneId: Long): ViewModelProvider.Factory {
        return EditZoneViewModelFactory(zoneRepository, zoneId)
    }

    // Fabryka dla listy stref (ManageZonesViewModel)
    val manageZonesViewModelFactory: ViewModelProvider.Factory by lazy {
        ManageZonesViewModelFactory(zoneRepository)
    }

    // --- USER FACTORIES ---

    // NOWA METODA: Fabryka dla EditUserViewModel
    fun createEditUserViewModelFactory(userId: Long): ViewModelProvider.Factory {
        return EditUserViewModelFactory(userManagementRepository, userId)
    }

    // Fabryka dla InventoryDetailsViewModel
    fun createInventoryDetailsViewModelFactory(itemId: Long): ViewModelProvider.Factory {
        return InventoryDetailsViewModelFactory(inventoryRepository, itemId)
    }

    /**
     * Odświeża konfigurację sieci po zmianie ustawień serwera
     */
    fun refreshNetworkConfig() {
        // Reinicjalizuj NetworkModule z nową konfiguracją
        NetworkModule.init(serverConfigManager)
        
        // Wyczyść wszystkie cached instancje żeby zostały odtworzone z nowym URL
        _okHttpClient = null
        _retrofit = null
        _authService = null
        _testService = null
        _healthService = null
        _apiService = null
        _movementHistoryApiService = null
        _orderApiService = null
    }
}