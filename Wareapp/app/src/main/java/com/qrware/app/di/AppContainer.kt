package com.qrware.app.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.remote.*
import com.qrware.app.data.repository.*
import com.qrware.app.security.TokenManager
import com.qrware.app.data.remote.ApiService
import com.qrware.app.data.repository.UserManagementRepository
import com.qrware.app.ui.viewmodel.AddProductViewModelFactory
import com.qrware.app.ui.viewmodel.AddUserViewModelFactory
import com.qrware.app.ui.viewmodel.EditUserViewModelFactory
import com.qrware.app.ui.viewmodel.InventoryViewModelFactory
import com.qrware.app.ui.viewmodel.ListUsersViewModelFactory
import com.qrware.app.ui.viewmodel.ManagePermissionsViewModelFactory
import com.qrware.app.ui.viewmodel.ManageRolesViewModelFactory
import retrofit2.Retrofit

// Prosty kontener do wstrzykiwania zależności (Dependency Injection)
class AppContainer(context: Context) {
    val tokenManager = TokenManager(context)
    private val okHttpClient = NetworkModule.createClient(tokenManager)
    private val retrofit: Retrofit = NetworkModule.createRetrofit(okHttpClient)

    // Services
    private val authService: AuthService by lazy { retrofit.create(AuthService::class.java) }
    private val testService: TestService by lazy { retrofit.create(TestService::class.java) }
    private val healthService: HealthService by lazy { retrofit.create(HealthService::class.java) }
    private val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
    // Repositories
    val authRepository: AuthRepository by lazy { AuthRepository(authService) }
    val testRepository: TestRepository by lazy { TestRepository(testService) }
    val healthRepository: HealthRepository by lazy { HealthRepository(healthService) }


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
    val addProductViewModelFactory by lazy {
        AddProductViewModelFactory(productRepository)
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

    val inventoryViewModelFactory: InventoryViewModelFactory
        get() = InventoryViewModelFactory(productRepository) // Przekazujemy productRepository

    // NOWA METODA: Fabryka dla EditUserViewModel
    // Potrzebuje userId, więc jest to funkcja, a nie 'val'
    fun createEditUserViewModelFactory(userId: Long): ViewModelProvider.Factory {
        return EditUserViewModelFactory(userManagementRepository, userId)
    }

}