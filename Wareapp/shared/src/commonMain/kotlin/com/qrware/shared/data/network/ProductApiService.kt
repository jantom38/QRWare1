package com.qrware.shared.data.network

import com.qrware.shared.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Product API Service - Ktor implementation
 * Migracja z Android ApiService na KMP
 */
class ProductApiService(private val httpClient: HttpClient) {
    
    companion object {
        // Product endpoints
        private const val PRODUCTS_BASE = "/api/products"
        private const val CATEGORIES_BASE = "/api/categories"
    }

    // --- PRODUCTS ---
    
    /**
     * Pobierz wszystkie produkty z paginacją
     */
    suspend fun getAllProducts(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc",
        active: Boolean? = null
    ): Result<PaginatedResponse<Product>> {
        return try {
            val response = httpClient.get(PRODUCTS_BASE) {
                parameter("page", page)
                parameter("size", size)
                parameter("sort", sort)
                active?.let { parameter("active", it) }
            }
            Result.success(response.body<PaginatedResponse<Product>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz produkt po ID
     */
    suspend fun getProductById(productId: Long): Result<Product> {
        return try {
            val response = httpClient.get("$PRODUCTS_BASE/$productId")
            Result.success(response.body<Product>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz produkt po SKU
     */
    suspend fun getProductBySku(sku: String): Result<Product> {
        return try {
            val response = httpClient.get("$PRODUCTS_BASE/sku/$sku")
            Result.success(response.body<Product>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz produkty z kategorii
     */
    suspend fun getProductsByCategory(categoryId: Long): Result<List<Product>> {
        return try {
            val response = httpClient.get("$PRODUCTS_BASE/category/$categoryId")
            Result.success(response.body<List<Product>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyszukaj produkty
     */
    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val response = httpClient.get("$PRODUCTS_BASE/search") {
                parameter("query", query)
            }
            Result.success(response.body<List<Product>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz aktywne produkty
     */
    suspend fun getActiveProducts(): Result<List<Product>> {
        return try {
            val response = httpClient.get("$PRODUCTS_BASE/active")
            Result.success(response.body<List<Product>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stwórz nowy produkt
     */
    suspend fun createProduct(request: CreateProductRequest): Result<Product> {
        return try {
            val response = httpClient.post(PRODUCTS_BASE) {
                setBody(request)
            }
            Result.success(response.body<Product>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aktualizuj produkt
     */
    suspend fun updateProduct(productId: Long, request: UpdateProductRequest): Result<Product> {
        return try {
            val response = httpClient.put("$PRODUCTS_BASE/$productId") {
                setBody(request)
            }
            Result.success(response.body<Product>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Usuń produkt
     */
    suspend fun deleteProduct(productId: Long): Result<Unit> {
        return try {
            httpClient.delete("$PRODUCTS_BASE/$productId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Przełącz status aktywności produktu
     */
    suspend fun toggleProductActive(productId: Long): Result<Product> {
        return try {
            val response = httpClient.post("$PRODUCTS_BASE/$productId/toggle-active")
            Result.success(response.body<Product>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CATEGORIES ---

    /**
     * Pobierz wszystkie kategorie
     */
    suspend fun getAllCategories(): Result<List<Category>> {
        return try {
            val response = httpClient.get(CATEGORIES_BASE)
            Result.success(response.body<List<Category>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz kategorię po ID
     */
    suspend fun getCategoryById(categoryId: Long): Result<Category> {
        return try {
            val response = httpClient.get("$CATEGORIES_BASE/$categoryId")
            Result.success(response.body<Category>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz kategorie główne (bez parentów)
     */
    suspend fun getRootCategories(): Result<List<Category>> {
        return try {
            val response = httpClient.get("$CATEGORIES_BASE/root")
            Result.success(response.body<List<Category>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz podkategorie
     */
    suspend fun getSubCategories(parentId: Long): Result<List<Category>> {
        return try {
            val response = httpClient.get("$CATEGORIES_BASE/parent/$parentId")
            Result.success(response.body<List<Category>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stwórz nową kategorię
     */
    suspend fun createCategory(request: CreateCategoryRequest): Result<Category> {
        return try {
            val response = httpClient.post(CATEGORIES_BASE) {
                setBody(request)
            }
            Result.success(response.body<Category>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aktualizuj kategorię
     */
    suspend fun updateCategory(categoryId: Long, request: UpdateCategoryRequest): Result<Category> {
        return try {
            val response = httpClient.put("$CATEGORIES_BASE/$categoryId") {
                setBody(request)
            }
            Result.success(response.body<Category>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Usuń kategorię
     */
    suspend fun deleteCategory(categoryId: Long): Result<Unit> {
        return try {
            httpClient.delete("$CATEGORIES_BASE/$categoryId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyszukaj kategorie
     */
    suspend fun searchCategories(query: String): Result<List<Category>> {
        return try {
            val response = httpClient.get("$CATEGORIES_BASE/search") {
                parameter("query", query)
            }
            Result.success(response.body<List<Category>>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}