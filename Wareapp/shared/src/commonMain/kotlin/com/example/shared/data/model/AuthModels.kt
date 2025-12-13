package com.example.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    var firstName: String,
    var lastName : String
)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

@Serializable
data class AuthenticationResponse(
    val username: String,
    @SerialName("accessToken")
    val token: String,
    @SerialName("refreshToken")
    val refreshToken: String?,
    val roles: List<String>,
    val permissions: List<String>
)

@Serializable
data class UserInfoResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val fullName: String?,
    val phone: String?,
    val active: Boolean,
    val emailVerified: Boolean,
    val lastLogin: String?,
    val roles: List<String>,
    val permissions: List<String>
)