// Ścieżka: app/src/main/java/com/qrware/app/data/model/RolePermissionModels.kt
package com.qrware.app.data.model

import com.google.gson.annotations.SerializedName

// --- MODELE DLA RÓL ---

data class RoleResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("permissions")
    val permissions: List<String>
)

data class RoleRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("permissions")
    val permissions: Set<String>,

    @SerializedName("active")
    val active: Boolean
)

// --- MODELE DLA UPRAWNIEŃ ---

data class PermissionResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("resource")
    val resource: String,

    @SerializedName("action")
    val action: String,

    @SerializedName("active")
    val active: Boolean
)

data class PermissionRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("resource")
    val resource: String,

    @SerializedName("action")
    val action: String,

    @SerializedName("active")
    val active: Boolean
)