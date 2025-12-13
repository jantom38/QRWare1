package com.example.shared.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDTO(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val price: Double?, // Zmiana z BigDecimal
    val cost: Double?,  // Zmiana z BigDecimal
    val weight: Double?, // Zmiana z BigDecimal
    val dimensionsLength: Double?,
    val dimensionsWidth: Double?,
    val dimensionsHeight: Double?,
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