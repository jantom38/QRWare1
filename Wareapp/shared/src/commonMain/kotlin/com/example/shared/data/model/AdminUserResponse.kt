package com.example.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminUserResponse(
    @SerialName("id")
    val id: Long,

    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String,

    @SerialName("firstName")
    val firstName: String,

    @SerialName("lastName")
    val lastName: String,

    @SerialName("phone")
    val phone: String?,

    @SerialName("active")
    val active: Boolean,

    @SerialName("emailVerified")
    val emailVerified: Boolean,

    @SerialName("lastLogin")
    val lastLogin: String?,

    @SerialName("accountNonLocked")
    val accountNonLocked: Boolean,

    @SerialName("roles")
    val roles: List<String>,

    @SerialName("permissions")
    val permissions: List<String>
)

@Serializable
data class PaginatedResponse<T>(
    @SerialName("content")
    val content: List<T>,

    @SerialName("totalPages")
    val totalPages: Int,

    @SerialName("totalElements")
    val totalElements: Long,

    @SerialName("number")
    val number: Int,

    @SerialName("size")
    val size: Int,

    @SerialName("first")
    val first: Boolean,

    @SerialName("last")
    val last: Boolean
)

@Serializable
data class AdminCreateUserRequest(
    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String,

    @SerialName("firstName")
    val firstName: String,

    @SerialName("lastName")
    val lastName: String,

    @SerialName("phone")
    val phone: String?,

    @SerialName("roles")
    val roles: Set<String>,

    @SerialName("active")
    val active: Boolean,

    @SerialName("emailVerified")
    val emailVerified: Boolean
)

@Serializable
data class UpdateUserRequest(
    @SerialName("email")
    val email: String?,

    @SerialName("firstName")
    val firstName: String?,

    @SerialName("lastName")
    val lastName: String?,

    @SerialName("phone")
    val phone: String?,

    @SerialName("active")
    val active: Boolean,

    @SerialName("emailVerified")
    val emailVerified: Boolean,

    @SerialName("roles")
    val roles: Set<String>
)