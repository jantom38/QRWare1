package com.qrware.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class UserInfo(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val roles: List<String>,
    val permissions: List<String>,
    val isActive: Boolean,
    val createdAt: String,
    val lastLoginAt: String?
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val error: String? = null,
    val timestamp: String
)

// Auth State dla UI
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val userInfo: UserInfo) : AuthState()
    data class Error(val message: String) : AuthState()
    data object Unauthenticated : AuthState()
}

// Token storage model
@Serializable
data class TokenData(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() >= expiresAt
    }
    
    fun isExpiringSoon(bufferMinutes: Int = 5): Boolean {
        val bufferMs = bufferMinutes * 60 * 1000
        return System.currentTimeMillis() >= (expiresAt - bufferMs)
    }
}