// Ścieżka: shared/src/commonMain/kotlin/com/example/shared/data/model/RolePermissionModels.kt
package com.example.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- MODELE DLA RÓL ---

@Serializable
data class RoleResponse(
    @SerialName("id")
    val id: Long,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String?,

    @SerialName("active")
    val active: Boolean,

    @SerialName("permissions")
    val permissions: List<String>
)

@Serializable
data class RoleRequest(
    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String?,

    @SerialName("permissions")
    val permissions: Set<String>,

    @SerialName("active")
    val active: Boolean
)

// --- MODELE DLA UPRAWNIEŃ ---

@Serializable
data class PermissionResponse(
    @SerialName("id")
    val id: Long,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String?,

    @SerialName("resource")
    val resource: String,

    @SerialName("action")
    val action: String,

    @SerialName("active")
    val active: Boolean
)

@Serializable
data class PermissionRequest(
    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String?,

    @SerialName("resource")
    val resource: String,

    @SerialName("action")
    val action: String,

    @SerialName("active")
    val active: Boolean
)