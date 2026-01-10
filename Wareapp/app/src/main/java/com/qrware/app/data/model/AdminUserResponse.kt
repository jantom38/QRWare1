package com.qrware.app.data.model
import com.google.gson.annotations.SerializedName

data class AdminUserResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("emailVerified")
    val emailVerified: Boolean,

    @SerializedName("lastLogin")
    val lastLogin: String?,

    @SerializedName("accountNonLocked")
    val accountNonLocked: Boolean,

    @SerializedName("roles")
    val roles: List<String>,

    @SerializedName("permissions")
    val permissions: List<String>
)

data class PaginatedResponse<T>(
    @SerializedName("content")
    val content: List<T>,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("totalElements")
    val totalElements: Long,

    @SerializedName("number")
    val number: Int,

    @SerializedName("size")
    val size: Int,

    @SerializedName("first")
    val first: Boolean,

    @SerializedName("last")
    val last: Boolean
)

data class AdminCreateUserRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("roles")
    val roles: Set<String>,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("emailVerified")
    val emailVerified: Boolean
)

data class UpdateUserRequest(
    @SerializedName("email")
    val email: String?,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("emailVerified")
    val emailVerified: Boolean,

    @SerializedName("roles")
    val roles: Set<String>
)