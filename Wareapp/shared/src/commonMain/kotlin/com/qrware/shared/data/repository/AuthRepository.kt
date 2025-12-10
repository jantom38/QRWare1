package com.qrware.shared.data.repository

import com.qrware.shared.data.model.*
import com.qrware.shared.data.network.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * KMP AuthRepository - zarządzanie autoryzacją cross-platform
 * Migracja z Android AuthRepository na shared Kotlin Multiplatform
 */
class AuthRepository(
    private val authApiService: AuthApiService
) {
    
    // Auth state management
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    // Current user data
    private var _currentUser: UserInfoResponse? = null
    val currentUser: UserInfoResponse? get() = _currentUser
    
    // Server connection status
    private val _isServerConnected = MutableStateFlow<Boolean?>(null)
    val isServerConnected: StateFlow<Boolean?> = _isServerConnected.asStateFlow()

    /**
     * Login użytkownika
     * @param usernameOrEmail - nazwa użytkownika lub email
     * @param password - hasło
     * @return Result z AuthenticationResponse lub błąd
     */
    suspend fun login(usernameOrEmail: String, password: String): Result<AuthenticationResponse> {
        // Walidacja input
        if (usernameOrEmail.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Username and password cannot be empty"))
        }
        
        _authState.value = AuthState.Loading
        
        return try {
            val loginRequest = LoginRequest(usernameOrEmail.trim(), password)
            val result = authApiService.login(loginRequest)
            
            result.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val authData = apiResponse.data
                        
                        // Pobierz dane użytkownika po udanym logowaniu
                        val userResult = getCurrentUser()
                        userResult.fold(
                            onSuccess = { userInfo ->
                                _currentUser = userInfo
                                _isAuthenticated.value = true
                                _authState.value = AuthState.Success(userInfo)
                                Result.success(authData)
                            },
                            onFailure = { userError ->
                                _authState.value = AuthState.Error("Failed to load user data: ${userError.message}")
                                Result.failure(userError)
                            }
                        )
                    } else {
                        val errorMsg = apiResponse.message.takeIf { it.isNotBlank() } 
                            ?: "Login failed"
                        _authState.value = AuthState.Error(errorMsg)
                        Result.failure(Exception(errorMsg))
                    }
                },
                onFailure = { error ->
                    val errorMsg = when {
                        error.message?.contains("Invalid username") == true -> "Invalid username or password"
                        error.message?.contains("Network error") == true -> "Network error. Check your connection."
                        error.message?.contains("connect") == true -> "Cannot connect to server. Is the backend running?"
                        else -> error.message ?: "Login failed"
                    }
                    _authState.value = AuthState.Error(errorMsg)
                    Result.failure(Exception(errorMsg))
                }
            )
        } catch (e: Exception) {
            val errorMsg = "Unexpected error during login: ${e.message}"
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Rejestracja nowego użytkownika
     */
    suspend fun register(
        username: String,
        email: String, 
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String
    ): Result<AuthenticationResponse> {
        // Walidacja
        val validationError = validateRegistrationData(
            username, email, password, confirmPassword, firstName, lastName
        )
        if (validationError != null) {
            return Result.failure(Exception(validationError))
        }
        
        _authState.value = AuthState.Loading
        
        return try {
            val registerRequest = RegisterRequest(
                username = username.trim(),
                email = email.trim(),
                password = password,
                firstName = firstName.trim(),
                lastName = lastName.trim()
            )
            
            val result = authApiService.register(registerRequest)
            
            result.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        // Po udanej rejestracji automatycznie zaloguj
                        login(username, password)
                    } else {
                        val errorMsg = apiResponse.message.takeIf { it.isNotBlank() } 
                            ?: "Registration failed"
                        _authState.value = AuthState.Error(errorMsg)
                        Result.failure(Exception(errorMsg))
                    }
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration failed")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            val errorMsg = "Registration failed: ${e.message}"
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Pobranie danych aktualnego użytkownika
     */
    suspend fun getCurrentUser(): Result<UserInfoResponse> {
        return try {
            val result = authApiService.getCurrentUser()
            
            result.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        _currentUser = apiResponse.data
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
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
     * Wylogowanie użytkownika
     */
    suspend fun logout(): Result<Unit> {
        return try {
            // Wywołaj API logout (nie bloków jeśli się nie uda)
            authApiService.logout()
            
            // Wyczyść lokalny stan niezależnie od wyniku API call
            _currentUser = null
            _isAuthenticated.value = false
            _authState.value = AuthState.Unauthenticated
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Wyczyść stan mimo błędu API
            _currentUser = null
            _isAuthenticated.value = false
            _authState.value = AuthState.Unauthenticated
            
            Result.success(Unit) // Logout zawsze "udany" lokalnie
        }
    }

    /**
     * Zmiana hasła
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String, 
        confirmPassword: String
    ): Result<Unit> {
        // Walidacja
        when {
            currentPassword.isBlank() -> return Result.failure(Exception("Current password is required"))
            newPassword.length < 6 -> return Result.failure(Exception("New password must be at least 6 characters"))
            newPassword != confirmPassword -> return Result.failure(Exception("Passwords do not match"))
            newPassword == currentPassword -> return Result.failure(Exception("New password must be different"))
        }
        
        return try {
            val request = ChangePasswordRequest(currentPassword, newPassword)
            val result = authApiService.changePassword(request)
            
            result.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(apiResponse.message))
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
     * Sprawdzenie połączenia z serwerem
     */
    suspend fun checkServerConnection(): Result<Boolean> {
        return try {
            val result = authApiService.healthCheck()
            val isConnected = result.isSuccess
            _isServerConnected.value = isConnected
            Result.success(isConnected)
        } catch (e: Exception) {
            _isServerConnected.value = false
            Result.failure(e)
        }
    }

    /**
     * Test ping do serwera
     */
    suspend fun pingServer(): Result<Long> {
        return authApiService.ping()
    }

    /**
     * Sprawdzenie uprawnień użytkownika
     */
    fun hasPermission(permission: String): Boolean {
        return _currentUser?.permissions?.contains(permission) == true
    }

    /**
     * Sprawdzenie roli użytkownika
     */
    fun hasRole(role: String): Boolean {
        return _currentUser?.roles?.contains(role) == true
    }

    /**
     * Sprawdzenie czy użytkownik jest administratorem
     */
    fun isAdmin(): Boolean {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN")
    }

    /**
     * Reset stanu autoryzacji
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
        _isAuthenticated.value = false
        _currentUser = null
    }

    /**
     * Walidacja danych rejestracji
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

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length > 5
    }

    private fun isStrongPassword(password: String): Boolean {
        return password.length >= 6 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
    }
}