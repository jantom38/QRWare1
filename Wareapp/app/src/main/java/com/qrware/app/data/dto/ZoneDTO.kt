package com.qrware.app.data.dto

/**
 * Uproszczony model Strefy, pasujący do DTO z serwera.
 */
data class ZoneDTO(
    val id: Long,
    val name: String,
    val code: String?
)