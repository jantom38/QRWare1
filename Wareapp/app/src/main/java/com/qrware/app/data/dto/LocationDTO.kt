package com.qrware.app.data.dto

/**
 * Uproszczony model Lokalizacji, pasujący do DTO z serwera.
 */
data class LocationDTO(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val zone: ZoneDTO? // Zagnieżdżone DTO
)