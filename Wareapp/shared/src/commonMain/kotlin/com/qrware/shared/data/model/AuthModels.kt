package com.qrware.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Request Bodies ---
@Serializable
data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

// --- Response Bodies ---
@Serializable
data class AuthenticationResponse(
    val username: String,
    @SerialName("accessToken")
    val token: String,
    @SerialName("refreshToken")
    val refreshToken: String? = null,
    val roles: List<String>,
    val permissions: List<String> = emptyList() // Opcjonalne z domyślną wartością
)

@Serializable
data class UserInfoResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val fullName: String? = null,
    val phone: String? = null,
    val active: Boolean,
    val emailVerified: Boolean,
    val lastLogin: String? = null,
    val roles: List<String>,
    val permissions: List<String>
)

// Auth State dla UI
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val userInfo: UserInfoResponse) : AuthState()
    data class Error(val message: String) : AuthState()
    data object Unauthenticated : AuthState()
}

// Token storage model
@Serializable
data class TokenData(
    val accessToken: String,
    val refreshToken: String?,
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