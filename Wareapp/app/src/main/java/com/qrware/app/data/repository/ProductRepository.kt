package com.qrware.app.data.repository

import com.qrware.app.data.model.*
import com.qrware.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllProducts(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc"
    ) = apiService.getAllProducts(page, size, sort)

    suspend fun getProductById(productId: Long) = 
        apiService.getProductById(productId)

    suspend fun getProductBySku(sku: String) = 
        apiService.getProductBySku(sku)

    suspend fun getProductsByCategory(categoryId: Long) = 
        apiService.getProductsByCategory(categoryId)

    suspend fun searchProducts(query: String) = 
        apiService.searchProducts(query)

    suspend fun getActiveProducts() = 
        apiService.getActiveProducts()

    suspend fun createProduct(request: CreateProductRequest) = 
        apiService.createProduct(request)

    suspend fun updateProduct(productId: Long, request: UpdateProductRequest) = 
        apiService.updateProduct(productId, request)

    suspend fun deleteProduct(productId: Long) = 
        apiService.deleteProduct(productId)

    suspend fun toggleProductActive(productId: Long) = 
        apiService.toggleProductActive(productId)
}