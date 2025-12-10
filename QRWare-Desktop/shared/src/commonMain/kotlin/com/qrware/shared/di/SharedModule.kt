package com.qrware.shared.di

import com.qrware.shared.data.auth.TokenManager
import com.qrware.shared.data.network.AuthApiService
import com.qrware.shared.data.network.HttpClientFactory
import com.qrware.shared.data.repository.AuthRepository
import com.qrware.shared.data.storage.PlatformTokenStorage
import com.qrware.shared.data.storage.TokenStorage
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Shared dependency injection container for all common dependencies
 */
class SharedModule(
    private val baseUrl: String = "http://localhost:8080",
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    
    // Storage
    private val tokenStorage: TokenStorage by lazy {
        PlatformTokenStorage()
    }
    
    // HTTP Client with token provider
    private val httpClient: HttpClient by lazy {
        HttpClientFactory().create(
            baseUrl = baseUrl,
            tokenProvider = {
                // This will be called synchronously, so we need to handle async token retrieval
                // For now, return null - the TokenManager will handle authentication
                null
            }
        )
    }
    
    // HTTP Client for authenticated requests
    private val authenticatedHttpClient: HttpClient by lazy {
        HttpClientFactory().create(
            baseUrl = baseUrl,
            tokenProvider = {
                // This is a synchronous callback, but token retrieval is async
                // We'll handle this differently in practice
                null
            }
        )
    }
    
    // API Services
    private val authApiService: AuthApiService by lazy {
        AuthApiService(httpClient)
    }
    
    // Auth Management
    private val tokenManager: TokenManager by lazy {
        TokenManager(tokenStorage, authApiService, scope)
    }
    
    // Repositories
    private val authRepository: AuthRepository by lazy {
        AuthRepository(tokenManager, authApiService)
    }
    
    // Public getters for dependencies
    fun getTokenStorage(): TokenStorage = tokenStorage
    fun getHttpClient(): HttpClient = httpClient
    fun getAuthenticatedHttpClient(): HttpClient = authenticatedHttpClient
    fun getAuthApiService(): AuthApiService = authApiService
    fun getTokenManager(): TokenManager = tokenManager
    fun getAuthRepository(): AuthRepository = authRepository
    
    /**
     * Create authenticated HTTP client with current tokens
     */
    fun createAuthenticatedHttpClient(): HttpClient {
        return HttpClientFactory().create(
            baseUrl = baseUrl,
            tokenProvider = {
                // Note: This is synchronous but token retrieval is async
                // In practice, we'll need to handle this differently
                // For now, this creates a basic client
                null
            }
        )
    }
    
    /**
     * Update base URL for all network clients
     */
    fun updateBaseUrl(newBaseUrl: String): SharedModule {
        return SharedModule(newBaseUrl, scope)
    }
    
    /**
     * Clean up all resources
     */
    fun dispose() {
        authRepository.dispose()
        httpClient.close()
        authenticatedHttpClient.close()
    }
}

/**
 * Global singleton instance - use carefully!
 * Better to inject this into your app's DI container
 */
object SharedDI {
    private var _instance: SharedModule? = null
    
    fun initialize(
        baseUrl: String = "http://localhost:8080",
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    ) {
        _instance = SharedModule(baseUrl, scope)
    }
    
    fun getInstance(): SharedModule {
        return _instance ?: throw IllegalStateException("SharedDI not initialized. Call initialize() first.")
    }
    
    fun dispose() {
        _instance?.dispose()
        _instance = null
    }
}