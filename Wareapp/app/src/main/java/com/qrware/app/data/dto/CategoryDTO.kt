package com.qrware.app.data.dto

/**
 * Kompletny model Kategorii, pasujący do DTO z serwera.
 */
data class CategoryDTO(
    val id: Long,
    val name: String,
    val code: String,
    val description: String? = null,
    val active: Boolean = true,
    val sortOrder: Int? = null,
    val icon: String? = null,
    val color: String? = null,
    val requiresSpecialHandling: Boolean? = null,
    val storageTemperatureMin: Int? = null,
    val storageTemperatureMax: Int? = null,
    val storageHumidityMin: Int? = null,
    val storageHumidityMax: Int? = null,
    val parent: CategoryDTO? = null,
    val level: Int? = null,
    val fullPath: String? = null
)