package com.qrware.shared.data.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ProductDTO(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val cost: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val weight: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val dimensionsLength: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val dimensionsWidth: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
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

// Alias for backward compatibility
typealias Product = ProductDTO

@Serializable
data class CategoryDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val code: String,
    val active: Boolean,
    val parentCategory: CategoryDTO?,
    val sortOrder: Int?,
    val icon: String?,
    val color: String?,
    val requiresSpecialHandling: Boolean?,
    val storageTemperatureMin: Int?,
    val storageTemperatureMax: Int?,
    val storageHumidityMin: Int?,
    val storageHumidityMax: Int?,
    val level: Int?,
    val fullPath: String?
)

// Alias for backward compatibility
typealias Category = CategoryDTO

@Serializable
data class CreateProductRequest(
    val sku: String,
    val name: String,
    val description: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val cost: BigDecimal? = null,
    val unit: String = "PIECE",
    @Serializable(with = BigDecimalSerializer::class)
    val weight: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val length: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val width: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val height: BigDecimal? = null,
    val minimumStock: Int? = null,
    val maximumStock: Int? = null,
    val reorderPoint: Int? = null,
    val active: Boolean = true,
    val perishable: Boolean = false,
    val hazardous: Boolean = false,
    val fragile: Boolean = false,
    val manufacturer: String? = null,
    val supplier: String? = null,
    val storageConditions: String? = null,
    val barcode: String? = null,
    val categoryId: Long? = null
)

@Serializable
data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val cost: BigDecimal? = null,
    val unit: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val weight: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val length: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val width: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val height: BigDecimal? = null,
    val minimumStock: Int? = null,
    val maximumStock: Int? = null,
    val reorderPoint: Int? = null,
    val active: Boolean? = null,
    val perishable: Boolean? = null,
    val hazardous: Boolean? = null,
    val fragile: Boolean? = null,
    val manufacturer: String? = null,
    val supplier: String? = null,
    val storageConditions: String? = null,
    val barcode: String? = null,
    val categoryId: Long? = null
)

// Category requests
@Serializable
data class CreateCategoryRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val parentId: Long? = null,
    val active: Boolean = true,
    val sortOrder: Int? = null,
    val icon: String? = null,
    val color: String? = null,
    val requiresSpecialHandling: Boolean = false,
    val storageTemperatureMin: Int? = null,
    val storageTemperatureMax: Int? = null,
    val storageHumidityMin: Int? = null,
    val storageHumidityMax: Int? = null
)

@Serializable
data class UpdateCategoryRequest(
    val name: String? = null,
    val description: String? = null,
    val parentId: Long? = null,
    val active: Boolean? = null,
    val sortOrder: Int? = null,
    val removeParent: Boolean = false,
    val icon: String? = null,
    val color: String? = null,
    val requiresSpecialHandling: Boolean? = null,
    val storageTemperatureMin: Int? = null,
    val storageTemperatureMax: Int? = null,
    val storageHumidityMin: Int? = null,
    val storageHumidityMax: Int? = null
)