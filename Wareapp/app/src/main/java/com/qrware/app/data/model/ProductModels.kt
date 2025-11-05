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
    val brand: String?,
    val model: String?,
    val version: String?
)

data class Category(
    val id: Long,
    val name: String,
    val description: String?,
    val code: String,
    val active: Boolean,
    val parentCategory: Category?
)

data class CreateProductRequest(
    val sku: String,
    val name: String,
    val description: String? = null,
    val price: BigDecimal? = null,
    val unit: String = "PIECE",
    val weight: BigDecimal? = null,
    val length: BigDecimal? = null,
    val width: BigDecimal? = null,
    val height: BigDecimal? = null,
    val categoryId: Long? = null,
    val active: Boolean = true
)

data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val unit: String? = null,
    val weight: BigDecimal? = null,
    val length: BigDecimal? = null,
    val width: BigDecimal? = null,
    val height: BigDecimal? = null,
    val categoryId: Long? = null,
    val active: Boolean? = null
)

// Category requests
data class CreateCategoryRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val parentId: Long? = null,
    val active: Boolean = true,
    val sortOrder: Int? = null
)

data class UpdateCategoryRequest(
    val name: String? = null,
    val description: String? = null,
    val parentId: Long? = null,
    val active: Boolean? = null,
    val sortOrder: Int? = null,
    val removeParent: Boolean = false
)