package com.qrware.app.data.dto

import java.math.BigDecimal

/**
 * Uproszczony model Produktu, pasujący do DTO z serwera.
 */
data class ProductDTO(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val price: BigDecimal?,
    val category: CategoryDTO? // Zagnieżdżone DTO

)