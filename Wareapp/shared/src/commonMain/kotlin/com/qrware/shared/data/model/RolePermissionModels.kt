package com.qrware.shared.data.model

import kotlinx.serialization.Serializable

// --- MODELE DLA RÓL ---

@Serializable
data class RoleResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val active: Boolean,
    val permissions: List<String>
)

@Serializable
data class RoleRequest(
    val name: String,
    val description: String? = null,
    val permissions: Set<String>,
    val active: Boolean
)

// --- MODELE DLA UPRAWNIEŃ ---

@Serializable
data class PermissionResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val resource: String,
    val action: String,
    val active: Boolean
)

@Serializable
data class PermissionRequest(
    val name: String,
    val description: String? = null,
    val resource: String,
    val action: String,
    val active: Boolean
)