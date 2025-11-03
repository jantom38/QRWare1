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
        sort: String = "id,asc"
    ): PaginatedResponse<ProductDTO> {
        return apiService.getAllProducts(page, size, sort)
    }

    // Tworzenie nowego produktu
    suspend fun createProduct(request: CreateProductRequest): ProductDTO {
        return apiService.createProduct(request)
    }

    // Możesz tu dodać resztę funkcji (getProductById, updateProduct itd.)
    // w miarę potrzeby, używając tego samego wzorca.
}