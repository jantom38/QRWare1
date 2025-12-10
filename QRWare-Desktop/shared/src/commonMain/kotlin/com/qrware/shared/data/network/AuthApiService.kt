package com.qrware.shared.data.network

import com.qrware.shared.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthApiService(private val httpClient: HttpClient) {
    
    companion object {
        private const val AUTH_BASE = "/api/auth"
        private const val LOGIN_ENDPOINT = "$AUTH_BASE/login"
        private const val REGISTER_ENDPOINT = "$AUTH_BASE/register"
        private const val REFRESH_ENDPOINT = "$AUTH_BASE/refresh"
        private const val LOGOUT_ENDPOINT = "$AUTH_BASE/logout"
        private const val USER_INFO_ENDPOINT = "$AUTH_BASE/user-info"
        private const val CHANGE_PASSWORD_ENDPOINT = "$AUTH_BASE/change-password"
        private const val HEALTH_ENDPOINT = "/api/health"
    }

    /**
     * Login user with credentials
     */
    suspend fun login(request: LoginRequest): Result<ApiResponse<AuthResponse>> {
        return try {
            val response = httpClient.post(LOGIN_ENDPOINT) {
                setBody(request)
            }
            Result.success(response.body<ApiResponse<AuthResponse>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register new user
     */
    suspend fun register(request: RegisterRequest): Result<ApiResponse<AuthResponse>> {
        return try {
            val response = httpClient.post(REGISTER_ENDPOINT) {
                setBody(request)
            }
            Result.success(response.body<ApiResponse<AuthResponse>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refresh access token
     */
    suspend fun refreshToken(request: RefreshTokenRequest): Result<ApiResponse<AuthResponse>> {
        return try {
            val response = httpClient.post(REFRESH_ENDPOINT) {
                setBody(request)
            }
            Result.success(response.body<ApiResponse<AuthResponse>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user information
     */
    suspend fun getUserInfo(): Result<ApiResponse<UserInfo>> {
        return try {
            val response = httpClient.get(USER_INFO_ENDPOINT)
            Result.success(response.body<ApiResponse<UserInfo>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Change user password
     */
    suspend fun changePassword(request: ChangePasswordRequest): Result<ApiResponse<Unit>> {
        return try {
            val response = httpClient.post(CHANGE_PASSWORD_ENDPOINT) {
                setBody(request)
            }
            Result.success(response.body<ApiResponse<Unit>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout user (invalidate tokens)
     */
    suspend fun logout(): Result<ApiResponse<Unit>> {
        return try {
            val response = httpClient.post(LOGOUT_ENDPOINT)
            Result.success(response.body<ApiResponse<Unit>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check server health
     */
    suspend fun checkHealth(): Result<ApiResponse<Map<String, Any>>> {
        return try {
            val response = httpClient.get(HEALTH_ENDPOINT)
            Result.success(response.body<ApiResponse<Map<String, Any>>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate token by making a simple authenticated request
     */
    suspend fun validateToken(): Result<Boolean> {
        return try {
            val response = httpClient.get(USER_INFO_ENDPOINT)
            Result.success(response.status == HttpStatusCode.OK)
        } catch (e: UnauthorizedException) {
            Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}