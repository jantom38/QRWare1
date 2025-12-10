package com.qrware.shared.data.network

import com.qrware.shared.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Ktor-based Auth API Service dla komunikacji z backend
 * Migracja z Retrofit AuthService na KMP
 */
class AuthApiService(private val httpClient: HttpClient) {
    
    companion object {
        // Auth endpoints - zgodne z backend Spring Boot
        private const val AUTH_LOGIN = "/api/auth/login"
        private const val AUTH_REGISTER = "/api/auth/register" 
        private const val AUTH_LOGOUT = "/api/auth/logout"
        private const val AUTH_ME = "/api/auth/me"
        private const val AUTH_REFRESH = "/api/auth/refresh"
        private const val AUTH_CHANGE_PASSWORD = "/api/auth/change-password"
        private const val HEALTH_CHECK = "/api/health"
    }

    /**
     * Login użytkownika
     * POST /api/auth/login
     */
    suspend fun login(request: LoginRequest): Result<ApiResponse<AuthenticationResponse>> {
        return try {
            val response = httpClient.post(AUTH_LOGIN) {
                setBody(request)
            }
            Result.success(response.body<ApiResponse<AuthenticationResponse>>())
        } catch (e: UnauthorizedException) {
            Result.failure(Exception("Invalid username or password"))
        } catch (e: NetworkException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    /**
     * Rejestracja nowego użytkownika
     * POST /api/auth/register
     */
    suspend fun register(request: RegisterRequest): Result<ApiResponse<AuthenticationResponse>> {
        return try {
            val response = httpClient.post(AUTH_REGISTER) {
                setBody(request)
            }
            Result.success(response.body<ApiResponse<AuthenticationResponse>>())
        } catch (e: ValidationException) {
            Result.failure(Exception("Validation error: ${e.message}"))
        } catch (e: NetworkException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Registration failed: ${e.message}"))
        }
    }

    /**
     * Pobranie danych aktualnego użytkownika
     * GET /api/auth/me
     */
    suspend fun getCurrentUser(): Result<ApiResponse<UserInfoResponse>> {
        return try {
            val response = httpClient.get(AUTH_ME)
            Result.success(response.body<ApiResponse<UserInfoResponse>>())
        } catch (e: UnauthorizedException) {
            Result.failure(Exception("Authentication required"))
        } catch (e: NetworkException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get user info: ${e.message}"))
        }
    }

    /**
     * Wylogowanie użytkownika
     * POST /api/auth/logout
     */
    suspend fun logout(): Result<ApiResponse<Unit>> {
        return try {
            val response = httpClient.post(AUTH_LOGOUT)
            Result.success(response.body<ApiResponse<Unit>>())
        } catch (e: NetworkException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Logout failed: ${e.message}"))
        }
    }

    /**
     * Odświeżenie tokena dostępu
     * POST /api/auth/refresh
     */
    suspend fun refreshToken(request: RefreshTokenRequest): Result<ApiResponse<AuthenticationResponse>> {
        return try {
            val response = httpClient.post(AUTH_REFRESH) {
                setBody(request)
            }
            Result.success(response.body<ApiResponse<AuthenticationResponse>>())
        } catch (e: UnauthorizedException) {
            Result.failure(Exception("Refresh token expired or invalid"))
        } catch (e: NetworkException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Token refresh failed: ${e.message}"))
        }
    }

    /**
     * Zmiana hasła użytkownika
     * POST /api/auth/change-password
     */
    suspend fun changePassword(request: ChangePasswordRequest): Result<ApiResponse<Unit>> {
        return try {
            val response = httpClient.post(AUTH_CHANGE_PASSWORD) {
                setBody(request)
            }
            Result.success(response.body<ApiResponse<Unit>>())
        } catch (e: UnauthorizedException) {
            Result.failure(Exception("Authentication required"))
        } catch (e: ValidationException) {
            Result.failure(Exception("Invalid password data"))
        } catch (e: NetworkException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Password change failed: ${e.message}"))
        }
    }

    /**
     * Health check serwera
     * GET /api/health
     */
    suspend fun healthCheck(): Result<Map<String, String>> {
        return try {
            val response = httpClient.get(HEALTH_CHECK)
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val body = response.body<Map<String, String>>()
                    Result.success(body)
                }
                else -> {
                    Result.failure(Exception("Server unhealthy: ${response.status}"))
                }
            }
        } catch (e: ConnectionException) {
            Result.failure(Exception("Cannot connect to server: ${e.message}"))
        } catch (e: TimeoutException) {
            Result.failure(Exception("Server timeout: ${e.message}"))
        } catch (e: NetworkException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Health check failed: ${e.message}"))
        }
    }

    /**
     * Sprawdzenie czy token jest ważny
     * Pomocnicza metoda dla token validation
     */
    suspend fun validateToken(): Result<Boolean> {
        return try {
            val userResult = getCurrentUser()
            Result.success(userResult.isSuccess)
        } catch (e: Exception) {
            Result.success(false)
        }
    }

    /**
     * Ping serwera - szybki test połączenia
     */
    suspend fun ping(): Result<Long> {
        return try {
            val startTime = System.currentTimeMillis()
            healthCheck()
            val endTime = System.currentTimeMillis()
            Result.success(endTime - startTime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}