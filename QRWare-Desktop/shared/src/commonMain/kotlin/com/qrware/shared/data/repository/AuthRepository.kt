package com.qrware.shared.data.repository

import com.qrware.shared.data.auth.TokenManager
import com.qrware.shared.data.model.*
import com.qrware.shared.data.network.AuthApiService
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService
) {
    
    // Expose authentication state
    val authState: StateFlow<AuthState> = tokenManager.authState
    val isAuthenticated: StateFlow<Boolean> = tokenManager.isAuthenticated

    /**
     * Login user with username and password
     */
    suspend fun login(username: String, password: String): Result<UserInfo> {
        return if (username.isBlank() || password.isBlank()) {
            Result.failure(Exception("Username and password cannot be empty"))
        } else {
            tokenManager.login(username.trim(), password)
        }
    }

    /**
     * Register new user
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String
    ): Result<UserInfo> {
        return try {
            // Validation
            val validationError = validateRegistrationData(
                username, email, password, confirmPassword, firstName, lastName
            )
            if (validationError != null) {
                return Result.failure(Exception(validationError))
            }

            val registerRequest = RegisterRequest(
                username = username.trim(),
                email = email.trim(),
                password = password,
                confirmPassword = confirmPassword,
                firstName = firstName.trim(),
                lastName = lastName.trim()
            )

            val response = authApiService.register(registerRequest)
            response.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        // After successful registration, login the user
                        login(username, password)
                    } else {
                        Result.failure(Exception(apiResponse.error ?: "Registration failed"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout current user
     */
    suspend fun logout() {
        tokenManager.logout()
    }

    /**
     * Change user password
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        return try {
            // Validation
            when {
                currentPassword.isBlank() -> {
                    Result.failure(Exception("Current password is required"))
                }
                newPassword.length < 6 -> {
                    Result.failure(Exception("New password must be at least 6 characters"))
                }
                newPassword != confirmPassword -> {
                    Result.failure(Exception("New passwords do not match"))
                }
                newPassword == currentPassword -> {
                    Result.failure(Exception("New password must be different from current password"))
                }
                else -> {
                    val request = ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )

                    val response = authApiService.changePassword(request)
                    response.fold(
                        onSuccess = { apiResponse ->
                            if (apiResponse.success) {
                                Result.success(Unit)
                            } else {
                                Result.failure(Exception(apiResponse.error ?: "Password change failed"))
                            }
                        },
                        onFailure = { error ->
                            Result.failure(error)
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user information
     */
    fun getCurrentUser(): UserInfo? {
        return tokenManager.getCurrentUser()
    }

    /**
     * Check if user has specific permission
     */
    fun hasPermission(permission: String): Boolean {
        val user = getCurrentUser()
        return user?.permissions?.contains(permission) == true
    }

    /**
     * Check if user has specific role
     */
    fun hasRole(role: String): Boolean {
        val user = getCurrentUser()
        return user?.roles?.contains(role) == true
    }

    /**
     * Check if user is admin
     */
    fun isAdmin(): Boolean {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN")
    }

    /**
     * Check if user is manager
     */
    fun isManager(): Boolean {
        return hasRole("MANAGER") || isAdmin()
    }

    /**
     * Refresh authentication tokens
     */
    suspend fun refreshTokens(): Result<Unit> {
        return tokenManager.refreshTokens()
    }

    /**
     * Check server health
     */
    suspend fun checkServerHealth(): Result<Map<String, Any>> {
        return authApiService.checkHealth().fold(
            onSuccess = { apiResponse ->
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception("Server health check failed"))
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Get access token for manual API calls
     */
    suspend fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    /**
     * Validate registration data
     */
    private fun validateRegistrationData(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String
    ): String? {
        return when {
            username.isBlank() -> "Username is required"
            username.length < 3 -> "Username must be at least 3 characters"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Username can only contain letters, numbers, and underscores"
            email.isBlank() -> "Email is required"
            !isValidEmail(email) -> "Invalid email format"
            firstName.isBlank() -> "First name is required"
            lastName.isBlank() -> "Last name is required"
            password.length < 6 -> "Password must be at least 6 characters"
            password != confirmPassword -> "Passwords do not match"
            !isStrongPassword(password) -> "Password must contain at least one uppercase letter, one lowercase letter, and one number"
            else -> null
        }
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length > 5
    }

    /**
     * Check if password is strong enough
     */
    private fun isStrongPassword(password: String): Boolean {
        return password.length >= 6 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
    }

    /**
     * Clean up resources
     */
    fun dispose() {
        tokenManager.dispose()
    }
}