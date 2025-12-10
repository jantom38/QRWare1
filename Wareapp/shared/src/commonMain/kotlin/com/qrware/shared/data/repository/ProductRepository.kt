package com.qrware.shared.data.repository

import com.qrware.shared.data.model.*
import com.qrware.shared.data.network.ProductApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Product Repository - zarządzanie produktami i kategoriami
 * Business logic layer dla produktów
 */
class ProductRepository(
    private val productApiService: ProductApiService
) {
    
    // State management dla UI
    private val _productsState = MutableStateFlow<List<Product>>(emptyList())
    val productsState: StateFlow<List<Product>> = _productsState.asStateFlow()
    
    private val _categoriesState = MutableStateFlow<List<Category>>(emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- PRODUCTS ---

    /**
     * Pobierz wszystkie produkty z cache refresh
     */
    suspend fun loadAllProducts(
        page: Int = 0,
        size: Int = 20,
        activeOnly: Boolean = true
    ): Result<PaginatedResponse<Product>> {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val result = productApiService.getAllProducts(
                page = page,
                size = size,
                active = if (activeOnly) true else null
            )
            
            result.fold(
                onSuccess = { paginatedResponse ->
                    if (page == 0) {
                        // First page - replace cache
                        _productsState.value = paginatedResponse.content
                    } else {
                        // Subsequent pages - append to cache
                        _productsState.value = _productsState.value + paginatedResponse.content
                    }
                    _isLoading.value = false
                    Result.success(paginatedResponse)
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load products: ${error.message}"
                    _isLoading.value = false
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _errorMessage.value = "Error loading products: ${e.message}"
            _isLoading.value = false
            Result.failure(e)
        }
    }

    /**
     * Pobierz produkt po ID
     */
    suspend fun getProductById(productId: Long): Result<Product> {
        return try {
            productApiService.getProductById(productId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz produkt po SKU
     */
    suspend fun getProductBySku(sku: String): Result<Product> {
        if (sku.isBlank()) {
            return Result.failure(Exception("SKU cannot be empty"))
        }
        
        return try {
            productApiService.getProductBySku(sku)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyszukaj produkty
     */
    suspend fun searchProducts(query: String): Result<List<Product>> {
        if (query.isBlank()) {
            return Result.success(_productsState.value)
        }
        
        return try {
            val result = productApiService.searchProducts(query)
            result.fold(
                onSuccess = { products ->
                    Result.success(products)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz produkty z kategorii
     */
    suspend fun getProductsByCategory(categoryId: Long): Result<List<Product>> {
        return try {
            productApiService.getProductsByCategory(categoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stwórz nowy produkt
     */
    suspend fun createProduct(
        sku: String,
        name: String,
        description: String? = null,
        price: java.math.BigDecimal? = null,
        categoryId: Long? = null,
        active: Boolean = true
    ): Result<Product> {
        // Walidacja
        when {
            sku.isBlank() -> return Result.failure(Exception("SKU is required"))
            name.isBlank() -> return Result.failure(Exception("Product name is required"))
            sku.length < 3 -> return Result.failure(Exception("SKU must be at least 3 characters"))
            name.length < 2 -> return Result.failure(Exception("Product name must be at least 2 characters"))
            price != null && price < java.math.BigDecimal.ZERO -> return Result.failure(Exception("Price cannot be negative"))
        }
        
        return try {
            val request = CreateProductRequest(
                sku = sku.trim(),
                name = name.trim(),
                description = description?.trim(),
                price = price,
                categoryId = categoryId,
                active = active
            )
            
            val result = productApiService.createProduct(request)
            result.fold(
                onSuccess = { product ->
                    // Update cache
                    _productsState.value = _productsState.value + product
                    Result.success(product)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aktualizuj produkt
     */
    suspend fun updateProduct(
        productId: Long,
        name: String? = null,
        description: String? = null,
        price: java.math.BigDecimal? = null,
        categoryId: Long? = null,
        active: Boolean? = null
    ): Result<Product> {
        // Walidacja
        if (name != null && name.isBlank()) {
            return Result.failure(Exception("Product name cannot be empty"))
        }
        if (price != null && price < java.math.BigDecimal.ZERO) {
            return Result.failure(Exception("Price cannot be negative"))
        }
        
        return try {
            val request = UpdateProductRequest(
                name = name?.trim(),
                description = description?.trim(),
                price = price,
                categoryId = categoryId,
                active = active
            )
            
            val result = productApiService.updateProduct(productId, request)
            result.fold(
                onSuccess = { product ->
                    // Update cache
                    _productsState.value = _productsState.value.map { 
                        if (it.id == productId) product else it 
                    }
                    Result.success(product)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Usuń produkt
     */
    suspend fun deleteProduct(productId: Long): Result<Unit> {
        return try {
            val result = productApiService.deleteProduct(productId)
            result.fold(
                onSuccess = {
                    // Remove from cache
                    _productsState.value = _productsState.value.filter { it.id != productId }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Przełącz status aktywności produktu
     */
    suspend fun toggleProductActive(productId: Long): Result<Product> {
        return try {
            val result = productApiService.toggleProductActive(productId)
            result.fold(
                onSuccess = { product ->
                    // Update cache
                    _productsState.value = _productsState.value.map { 
                        if (it.id == productId) product else it 
                    }
                    Result.success(product)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CATEGORIES ---

    /**
     * Pobierz wszystkie kategorie
     */
    suspend fun loadAllCategories(): Result<List<Category>> {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val result = productApiService.getAllCategories()
            result.fold(
                onSuccess = { categories ->
                    _categoriesState.value = categories
                    _isLoading.value = false
                    Result.success(categories)
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load categories: ${error.message}"
                    _isLoading.value = false
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _errorMessage.value = "Error loading categories: ${e.message}"
            _isLoading.value = false
            Result.failure(e)
        }
    }

    /**
     * Pobierz kategorię po ID
     */
    suspend fun getCategoryById(categoryId: Long): Result<Category> {
        return try {
            productApiService.getCategoryById(categoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobierz kategorie główne
     */
    suspend fun getRootCategories(): Result<List<Category>> {
        return try {
            productApiService.getRootCategories()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stwórz nową kategorię
     */
    suspend fun createCategory(
        code: String,
        name: String,
        description: String? = null,
        parentId: Long? = null,
        active: Boolean = true
    ): Result<Category> {
        // Walidacja
        when {
            code.isBlank() -> return Result.failure(Exception("Category code is required"))
            name.isBlank() -> return Result.failure(Exception("Category name is required"))
            code.length < 2 -> return Result.failure(Exception("Category code must be at least 2 characters"))
        }
        
        return try {
            val request = CreateCategoryRequest(
                code = code.trim().uppercase(),
                name = name.trim(),
                description = description?.trim(),
                parentId = parentId,
                active = active
            )
            
            val result = productApiService.createCategory(request)
            result.fold(
                onSuccess = { category ->
                    // Update cache
                    _categoriesState.value = _categoriesState.value + category
                    Result.success(category)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyszukaj kategorie
     */
    suspend fun searchCategories(query: String): Result<List<Category>> {
        if (query.isBlank()) {
            return Result.success(_categoriesState.value)
        }
        
        return try {
            productApiService.searchCategories(query)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wyczyść error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Refresh cache
     */
    suspend fun refreshData() {
        loadAllProducts(page = 0, size = 50)
        loadAllCategories()
    }
}