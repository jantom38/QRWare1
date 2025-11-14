package com.qrware.app.data.dto

import java.math.BigDecimal

/**
 * Kompletny model Produktu, pasujący do DTO z serwera.
 */
data class ProductDTO(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val price: BigDecimal?,
    val cost: BigDecimal?,
    val weight: BigDecimal?,
    val dimensionsLength: BigDecimal?,
    val dimensionsWidth: BigDecimal?,
    val dimensionsHeight: BigDecimal?,
    val unitOfMeasure: String?,
    val minimumStock: Int?,
    val maximumStock: Int?,
    val reorderPoint: Int?,
    val active: Boolean,
    val perishable: Boolean?,
    val hazardous: Boolean?,
    val fragile: Boolean?,
    val manufacturer: String?,
    val supplier: String?,
    val storageConditions: String?,
    val barcode: String?,
    val category: CategoryDTO?
)