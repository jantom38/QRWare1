package com.qrware.app.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    var firstName: String,
    var lastName : String
)

data class RefreshTokenRequest(val refreshToken: String)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class AuthenticationResponse(
    val username: String,
    @SerializedName("accessToken")
    val token: String,
    @SerializedName("refreshToken")
    val refreshToken: String?,
    val roles: List<String>,
    val permissions: List<String>
)

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