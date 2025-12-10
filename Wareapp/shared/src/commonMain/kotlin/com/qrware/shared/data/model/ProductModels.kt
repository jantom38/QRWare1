package com.qrware.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String? = null,
    val barcode: String? = null,
    val category: Category,
    val price: Double? = null,
    val cost: Double? = null,
    val weight: Double? = null,
    val dimensionsLength: Double? = null,
    val dimensionsWidth: Double? = null,
    val dimensionsHeight: Double? = null,
    val unitOfMeasure: String,
    val minimumStock: Int,
    val maximumStock: Int? = null,
    val reorderPoint: Int? = null,
    val active: Boolean,
    val perishable: Boolean,
    val hazardous: Boolean,
    val fragile: Boolean,
    val manufacturer: String? = null,
    val supplier: String? = null,
    val storageConditions: String? = null
)

@Serializable
data class Category(
    val id: Long,
    val name: String,
    val description: String? = null,
    val code: String,
    val active: Boolean,
    val parentCategory: Category? = null,
    val sortOrder: Int? = null,
    val icon: String? = null,
    val color: String? = null,
    val requiresSpecialHandling: Boolean? = null,
    val storageTemperatureMin: Int? = null,
    val storageTemperatureMax: Int? = null,
    val storageHumidityMin: Int? = null,
    val storageHumidityMax: Int? = null,
    val level: Int? = null,
    val fullPath: String? = null
)

@Serializable
data class CreateProductRequest(
    val sku: String,
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    val cost: Double? = null,
    val unit: String = "PIECE",
    val weight: Double? = null,
    val length: Double? = null,
    val width: Double? = null,
    val height: Double? = null,
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
    val price: Double? = null,
    val cost: Double? = null,
    val unit: String? = null,
    val weight: Double? = null,
    val length: Double? = null,
    val width: Double? = null,
    val height: Double? = null,
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