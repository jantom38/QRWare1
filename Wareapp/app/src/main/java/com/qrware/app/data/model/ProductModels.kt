package com.qrware.app.data.model

import java.math.BigDecimal

data class Product(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val barcode: String?,
    val category: Category,
    val price: BigDecimal?,
    val cost: BigDecimal?,
    val weight: BigDecimal?,
    val dimensionsLength: BigDecimal?,
    val dimensionsWidth: BigDecimal?,
    val dimensionsHeight: BigDecimal?,
    val unitOfMeasure: String,
    val minimumStock: Int,
    val maximumStock: Int?,
    val reorderPoint: Int?,
    val active: Boolean,
    val perishable: Boolean,
    val hazardous: Boolean,
    val fragile: Boolean,
    val manufacturer: String?,
    val supplier: String?,
    val storageConditions: String?
)

data class Category(
    val id: Long,
    val name: String,
    val description: String?,
    val code: String,
    val active: Boolean,
    val parentCategory: Category?,
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

data class CreateProductRequest(
    val sku: String,
    val name: String,
    val description: String? = null,
    val price: BigDecimal? = null,
    val cost: BigDecimal? = null,
    val unit: String = "PIECE",
    val weight: BigDecimal? = null,
    val length: BigDecimal? = null,
    val width: BigDecimal? = null,
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

data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val cost: BigDecimal? = null,
    val unit: String? = null,
    val weight: BigDecimal? = null,
    val length: BigDecimal? = null,
    val width: BigDecimal? = null,
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