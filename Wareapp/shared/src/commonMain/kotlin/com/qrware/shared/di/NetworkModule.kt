package com.qrware.shared.di

import com.qrware.shared.data.network.AuthApiService
import com.qrware.shared.data.network.HttpClientFactory
import com.qrware.shared.data.repository.AuthRepository
import io.ktor.client.*

/**
 * Dependency Injection module dla Network Layer
 * Zarządza HttpClient, API Services i Repositories
 */
class NetworkModule(
    private val baseUrl: String = "http://localhost:8080",
    private val enableLogging: Boolean = true
) {
    
    // Token provider - będzie używany przez HttpClient do auth
    private var tokenProvider: suspend () -> String? = { null }
    
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
    
    // Repositories
    private val _authRepository: AuthRepository by lazy {
        AuthRepository(_authApiService)
    }
    
    // Public getters
    fun getHttpClient(): HttpClient = _httpClient
    fun getAuthApiService(): AuthApiService = _authApiService
    fun getAuthRepository(): AuthRepository = _authRepository
    
    /**
     * Update token provider dla authenticated requests
     */
    fun updateTokenProvider(provider: suspend () -> String?) {
        tokenProvider = provider
    }
    
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
    fun getAuthApiService(): AuthApiService = getModule().getAuthApiService()
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