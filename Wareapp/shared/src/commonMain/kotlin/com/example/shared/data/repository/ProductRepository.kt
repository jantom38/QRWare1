package com.example.shared.data.repository

import com.example.shared.data.dto.ProductDTO
import com.example.shared.data.model.CreateProductRequest
import com.example.shared.data.model.PaginatedResponse
import com.example.shared.data.model.UpdateProductRequest
import com.example.shared.data.remote.ApiService

class ProductRepository(
    private val apiService: ApiService
) {
    suspend fun getAllProducts(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc",
        active: Boolean? = null
    ): PaginatedResponse<ProductDTO> {
        return apiService.getAllProducts(page, size, sort, active)
    }

    suspend fun createProduct(request: CreateProductRequest): ProductDTO {
        return apiService.createProduct(request)
    }

    suspend fun getProductById(productId: Long): ProductDTO {
        return apiService.getProductById(productId)
    }

    suspend fun getProductBySku(sku: String): ProductDTO {
        return apiService.getProductBySku(sku)
    }

    suspend fun getProductsByCategory(categoryId: Long): List<ProductDTO> {
        return apiService.getProductsByCategory(categoryId)
    }

    suspend fun searchProducts(query: String): List<ProductDTO> {
        return apiService.searchProducts(query)
    }

    suspend fun getActiveProducts(): List<ProductDTO> {
        return apiService.getActiveProducts()
    }

    suspend fun updateProduct(productId: Long, request: UpdateProductRequest): ProductDTO {
        return apiService.updateProduct(productId, request)
    }

    suspend fun deleteProduct(productId: Long) {
        apiService.deleteProduct(productId)
    }

    suspend fun toggleProductActive(productId: Long): ProductDTO {
        return apiService.toggleProductActive(productId)
    }
}