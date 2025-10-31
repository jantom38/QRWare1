package com.qrware.app.data.dto

/**
 * Uproszczony model Kategorii, pasujący do DTO z serwera.
 */
data class CategoryDTO(
    val id: Long,
    val name: String,
    val code: String?
)