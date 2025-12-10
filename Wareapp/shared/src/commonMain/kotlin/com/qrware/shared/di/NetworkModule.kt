package com.qrware.shared.di

import com.qrware.shared.data.auth.TokenManager
import com.qrware.shared.data.network.AuthApiService
import com.qrware.shared.data.network.ProductApiService
import com.qrware.shared.data.network.InventoryApiService
import com.qrware.shared.data.network.HttpClientFactory
import com.qrware.shared.data.repository.AuthRepository
import com.qrware.shared.data.repository.ProductRepository
import com.qrware.shared.data.repository.InventoryRepository
import io.ktor.client.*

/**
 * Dependency Injection module dla Network Layer
 * Zarządza HttpClient, API Services i Repositories
 */
class NetworkModule(
    private val baseUrl: String = "http://localhost:8080",
    private val enableLogging: Boolean = true
) {
    
    // Token Manager
    private val tokenManager = TokenManager()
    
    // Token provider - łączy się z TokenManager
    private val tokenProvider: suspend () -> String? = { tokenManager.getToken() }
    
    // HTTP Client instance  
    private val _httpClient: HttpClient by lazy {
        HttpClientFactory().create(
            baseUrl = baseUrl,
            tokenProvider = tokenProvider,
            enableLogging = enableLogging
        )
    }
    
    // API Services
    private val _authApiService: AuthApiService by lazy {
        AuthApiService(_httpClient)
    }
    
    private val _productApiService: ProductApiService by lazy {
        ProductApiService(_httpClient)
    }
    
    private val _inventoryApiService: InventoryApiService by lazy {
        InventoryApiService(_httpClient)
    }
    
    // Repositories
    private val _authRepository: AuthRepository by lazy {
        AuthRepository(_authApiService, tokenManager)
    }
    
    private val _productRepository: ProductRepository by lazy {
        ProductRepository(_productApiService)
    }
    
    private val _inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(_inventoryApiService)
    }
    
    // Public getters
    fun getHttpClient(): HttpClient = _httpClient
    fun getAuthApiService(): AuthApiService = _authApiService
    fun getProductApiService(): ProductApiService = _productApiService
    fun getInventoryApiService(): InventoryApiService = _inventoryApiService
    fun getAuthRepository(): AuthRepository = _authRepository
    fun getProductRepository(): ProductRepository = _productRepository
    fun getInventoryRepository(): InventoryRepository = _inventoryRepository
    fun getTokenManager(): TokenManager = tokenManager
    
    /**
     * Update token provider - usunąłem bo nie jest potrzebne
     * TokenManager automatycznie dostarcza token
     */
    
    /**
     * Update base URL (np. gdy user zmienia server settings)
     */
    fun updateBaseUrl(newBaseUrl: String): NetworkModule {
        return NetworkModule(newBaseUrl, enableLogging)
    }
    
    /**
     * Clean up resources
     */
    fun dispose() {
        _httpClient.close()
    }
}

/**
 * Singleton dla łatwego dostępu w całej aplikacji
 * UWAGA: W produkcji lepiej używać proper DI framework (Koin)
 */
object NetworkDI {
    private var _module: NetworkModule? = null
    
    fun initialize(baseUrl: String = "http://localhost:8080", enableLogging: Boolean = true) {
        _module?.dispose()
        _module = NetworkModule(baseUrl, enableLogging)
    }
    
    fun getModule(): NetworkModule {
        return _module ?: throw IllegalStateException("NetworkDI not initialized. Call initialize() first.")
    }
    
    // Convenience methods
    fun getAuthRepository(): AuthRepository = getModule().getAuthRepository()
    fun getProductRepository(): ProductRepository = getModule().getProductRepository()
    fun getInventoryRepository(): InventoryRepository = getModule().getInventoryRepository()
    fun getAuthApiService(): AuthApiService = getModule().getAuthApiService()
    fun getProductApiService(): ProductApiService = getModule().getProductApiService()
    fun getInventoryApiService(): InventoryApiService = getModule().getInventoryApiService()
    fun getHttpClient(): HttpClient = getModule().getHttpClient()
    
    fun updateServerUrl(newUrl: String) {
        val currentModule = _module
        val enableLogging = true // TODO: get from current module
        initialize(newUrl, enableLogging)
        currentModule?.dispose()
    }
    
    fun dispose() {
        _module?.dispose()
        _module = null
    }
}