package com.qrware.app.data.repository

import com.qrware.app.data.dto.ProductDTO
import com.qrware.app.data.model.CreateProductRequest
import com.qrware.app.data.model.PaginatedResponse
import com.qrware.app.data.model.UpdateProductRequest
import com.qrware.app.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
    // Pobieranie wszystkich (zgodnie z poprawionym ApiService)
    suspend fun getAllProducts(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc",
        active: Boolean? = null // <-- DODANA ZMIENNA
    ): PaginatedResponse<ProductDTO> {
        // ZMIANA: Przekazujemy 'active' do apiService
        return apiService.getAllProducts(page, size, sort, active)
    }

    // Tworzenie nowego produktu
    suspend fun createProduct(request: CreateProductRequest): ProductDTO {
        return apiService.createProduct(request)
    }

    // --- UZUPEŁNIONE METODY (bez zmian) ---

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

    suspend fun deleteProduct(productId: Long): Response<Unit> {
        return apiService.deleteProduct(productId)
    }

    suspend fun toggleProductActive(productId: Long): ProductDTO {
        return apiService.toggleProductActive(productId)
    }
}