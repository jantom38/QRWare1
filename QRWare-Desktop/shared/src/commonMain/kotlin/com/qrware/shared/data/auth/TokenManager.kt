package com.qrware.shared.data.auth

import com.qrware.shared.data.model.*
import com.qrware.shared.data.network.AuthApiService
import com.qrware.shared.data.storage.TokenStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class TokenManager(
    private val tokenStorage: TokenStorage,
    private val authApiService: AuthApiService,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private var refreshJob: Job? = null
    private var userInfo: UserInfo? = null

    init {
        // Check if user is already authenticated on startup
        scope.launch {
            checkAuthenticationStatus()
            startTokenRefreshScheduler()
        }
    }

    /**
     * Login with credentials
     */
    suspend fun login(username: String, password: String): Result<UserInfo> {
        _authState.value = AuthState.Loading
        
        return try {
            val loginRequest = LoginRequest(username, password)
            val response = authApiService.login(loginRequest)
            
            response.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val authData = apiResponse.data
                        
                        // Save tokens
                        val tokenData = TokenData(
                            accessToken = authData.accessToken,
                            refreshToken = authData.refreshToken,
                            expiresAt = System.currentTimeMillis() + authData.expiresIn * 1000
                        )
                        tokenStorage.saveTokens(tokenData)
                        
                        // Get user info
                        val userInfoResult = fetchUserInfo()
                        userInfoResult.fold(
                            onSuccess = { user ->
                                userInfo = user
                                _isAuthenticated.value = true
                                _authState.value = AuthState.Success(user)
                                Result.success(user)
                            },
                            onFailure = { error ->
                                _authState.value = AuthState.Error("Failed to fetch user info: ${error.message}")
                                Result.failure(error)
                            }
                        )
                    } else {
                        val error = apiResponse.error ?: "Login failed"
                        _authState.value = AuthState.Error(error)
                        Result.failure(Exception(error))
                    }
                },
                onFailure = { error ->
                    val errorMessage = when (error) {
                        is Exception -> error.message ?: "Network error"
                        else -> "Unknown error occurred"
                    }
                    _authState.value = AuthState.Error(errorMessage)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Unexpected error")
            Result.failure(e)
        }
    }

    /**
     * Logout user
     */
    suspend fun logout() {
        try {
            // Call logout API
            authApiService.logout()
        } catch (e: Exception) {
            // Continue with logout even if API call fails
        } finally {
            // Clear local data
            tokenStorage.clearTokens()
            userInfo = null
            _isAuthenticated.value = false
            _authState.value = AuthState.Unauthenticated
            refreshJob?.cancel()
        }
    }

    /**
     * Get current access token
     */
    suspend fun getAccessToken(): String? {
        val tokens = tokenStorage.getTokens()
        return if (tokens?.isExpired() == false) {
            tokens.accessToken
        } else if (tokens?.refreshToken != null) {
            // Try to refresh token
            val refreshResult = refreshTokens()
            if (refreshResult.isSuccess) {
                tokenStorage.getAccessToken()
            } else {
                null
            }
        } else {
            null
        }
    }

    /**
     * Get current user info
     */
    fun getCurrentUser(): UserInfo? = userInfo

    /**
     * Refresh access tokens
     */
    suspend fun refreshTokens(): Result<Unit> {
        return try {
            val currentTokens = tokenStorage.getTokens()
            if (currentTokens?.refreshToken == null) {
                return Result.failure(Exception("No refresh token available"))
            }

            val refreshRequest = RefreshTokenRequest(currentTokens.refreshToken)
            val response = authApiService.refreshToken(refreshRequest)

            response.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val authData = apiResponse.data
                        val newTokenData = TokenData(
                            accessToken = authData.accessToken,
                            refreshToken = authData.refreshToken,
                            expiresAt = System.currentTimeMillis() + authData.expiresIn * 1000
                        )
                        tokenStorage.saveTokens(newTokenData)
                        Result.success(Unit)
                    } else {
                        // Refresh failed, logout user
                        logout()
                        Result.failure(Exception(apiResponse.error ?: "Token refresh failed"))
                    }
                },
                onFailure = { error ->
                    // Refresh failed, logout user
                    logout()
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            logout()
            Result.failure(e)
        }
    }

    /**
     * Check if user is currently authenticated
     */
    private suspend fun checkAuthenticationStatus() {
        try {
            val hasValidTokens = tokenStorage.hasValidTokens()
            if (hasValidTokens) {
                // Validate with server
                val isValid = authApiService.validateToken()
                if (isValid.getOrDefault(false)) {
                    val userInfoResult = fetchUserInfo()
                    userInfoResult.fold(
                        onSuccess = { user ->
                            userInfo = user
                            _isAuthenticated.value = true
                            _authState.value = AuthState.Success(user)
                        },
                        onFailure = {
                            logout()
                        }
                    )
                } else {
                    logout()
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Fetch current user information
     */
    private suspend fun fetchUserInfo(): Result<UserInfo> {
        return authApiService.getUserInfo().fold(
            onSuccess = { apiResponse ->
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.error ?: "Failed to fetch user info"))
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Start automatic token refresh scheduler
     */
    private fun startTokenRefreshScheduler() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (isActive) {
                try {
                    val tokens = tokenStorage.getTokens()
                    if (tokens != null && tokens.isExpiringSoon(5)) {
                        refreshTokens()
                    }
                    delay(60000) // Check every minute
                } catch (e: Exception) {
                    delay(60000) // Wait before retrying
                }
            }
        }
    }

    /**
     * Clean up resources
     */
    fun dispose() {
        refreshJob?.cancel()
        scope.cancel()
    }
}