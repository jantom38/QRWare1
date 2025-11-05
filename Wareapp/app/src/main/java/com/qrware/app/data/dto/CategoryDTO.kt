package com.qrware.app.data.dto

/**
 * Model Kategorii, pasujący do DTO z serwera.
 */
data class CategoryDTO(
    val id: Long,
    val name: String,
    val code: String,
    val description: String? = null,
    val active: Boolean = true,
    val sortOrder: Int? = null
)