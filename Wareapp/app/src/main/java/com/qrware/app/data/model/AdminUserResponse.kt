// Ścieżka: app/src/main/java/com/qrware/app/data/model/AdminUserResponse.kt
package com.qrware.app.data.model
import com.google.gson.annotations.SerializedName


/**
 * Model danych reprezentujący szczegóły użytkownika zwracane dla administratora.
 * Odpowiada klasie AdminUserResponse w UserManagementController.
 */
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
    val phone: String?, // Może być null

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("emailVerified")
    val emailVerified: Boolean,

    @SerializedName("lastLogin")
    val lastLogin: String?, // LocalDateTime jest zwykle serializowane jako String

    @SerializedName("accountNonLocked")
    val accountNonLocked: Boolean,

    @SerializedName("roles")
    val roles: List<String>,

    @SerializedName("permissions")
    val permissions: List<String>
)

/**
 * Generyczny model odpowiedzi dla stronicowanych (paginowanych) danych
 * ze Spring Boot Pageable.
 */
data class PaginatedResponse<T>(
    @SerializedName("content")
    val content: List<T>, // Lista obiektów na bieżącej stronie

    @SerializedName("totalPages")
    val totalPages: Int, // Całkowita liczba stron

    @SerializedName("totalElements")
    val totalElements: Long, // Całkowita liczba elementów

    @SerializedName("number")
    val number: Int, // Numer bieżącej strony (zaczyna się od 0)

    @SerializedName("size")
    val size: Int, // Liczba elementów na stronie

    @SerializedName("first")
    val first: Boolean, // Czy to pierwsza strona

    @SerializedName("last")
    val last: Boolean // Czy to ostatnia strona
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

// === NOWE DTO: Żądanie aktualizacji użytkownika przez Admina ===
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