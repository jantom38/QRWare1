package com.qrware.app.data.remote

import com.qrware.app.data.model.*
import com.qrware.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

// ... (AuthService i reszta interfejsów bez zmian) ...
interface AuthService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthenticationResponse>>
    @GET("/api/auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserInfoResponse>>
    @POST("/api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthenticationResponse>>
}

interface ApiService {

    // ... (Endpointy Użytkowników, Ról, Uprawnień bez zmian) ...
    @GET("api/users")
    suspend fun getAllUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "id,asc"
    ): ApiResponse<PaginatedResponse<AdminUserResponse>>
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") userId: Long): ApiResponse<AdminUserResponse>
    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Long,
        @Body request: UpdateUserRequest
    ): ApiResponse<AdminUserResponse>
    @POST("api/users")
    suspend fun createUser(
        @Body request: AdminCreateUserRequest
    ): ApiResponse<AdminUserResponse>
    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Path("id") userId: Long
    ): ApiResponse<Unit>
    @GET("api/roles")
    suspend fun getAllRoles(): ApiResponse<List<RoleResponse>>
    @POST("api/roles")
    suspend fun createRole(@Body request: RoleRequest): ApiResponse<RoleResponse>
    @GET("api/roles/{id}")
    suspend fun getRoleById(@Path("id") roleId: Long): ApiResponse<RoleResponse>
    @PUT("api/roles/{id}")
    suspend fun updateRole(
        @Path("id") roleId: Long,
        @Body request: RoleRequest
    ): ApiResponse<RoleResponse>
    @DELETE("api/roles/{id}")
    suspend fun deleteRole(@Path("id") roleId: Long): ApiResponse<Unit>
    @GET("api/permissions")
    suspend fun getAllPermissions(): ApiResponse<List<PermissionResponse>>
    @POST("api/permissions")
    suspend fun createPermission(@Body request: PermissionRequest): ApiResponse<PermissionResponse>
    @GET("api/permissions/{id}")
    suspend fun getPermissionById(@Path("id") permissionId: Long): ApiResponse<PermissionResponse>
    @PUT("api/permissions/{id}")
    suspend fun updatePermission(
        @Path("id") permissionId: Long,
        @Body request: PermissionRequest
    ): ApiResponse<PermissionResponse>
    @DELETE("api/permissions/{id}")
    suspend fun deletePermission(@Path("id") permissionId: Long): ApiResponse<Unit>


    // --- ENDPOINTY INVENTORY (Poprawione) ---

    @GET("api/inventory")
    suspend fun getAllInventoryItems(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "id,asc"
    ): PaginatedResponse<InventoryItemDTO>

    @GET("api/inventory/{id}")
    suspend fun getInventoryItemById(@Path("id") itemId: Long): InventoryItemDTO

    @GET("api/inventory/product/{productId}")
    suspend fun getInventoryByProduct(@Path("productId") productId: Long): List<InventoryItemDTO>

    @GET("api/inventory/location/{locationId}")
    suspend fun getInventoryByLocation(@Path("locationId") locationId: Long): List<InventoryItemDTO>

    @GET("api/inventory/status/{status}")
    suspend fun getInventoryByStatus(@Path("status") status: InventoryStatus): List<InventoryItemDTO>

    @POST("api/inventory")
    suspend fun createInventoryItem(@Body request: CreateInventoryRequest): InventoryItemDTO

    @PUT("api/inventory/{id}")
    suspend fun updateInventoryItem(
        @Path("id") itemId: Long,
        @Body request: UpdateInventoryRequest
    ): InventoryItemDTO

    @DELETE("api/inventory/{id}")
    suspend fun deleteInventoryItem(@Path("id") itemId: Long): Response<Unit>

    @POST("api/inventory/{id}/receive")
    suspend fun receiveStock(
        @Path("id") itemId: Long,
        @Body request: QuantityUpdateRequest
    ): InventoryItemDTO

    @POST("api/inventory/{id}/issue")
    suspend fun issueStock(
        @Path("id") itemId: Long,
        @Body request: QuantityUpdateRequest
    ): InventoryItemDTO

    // --- ENDPOINTY PRODUCTS (ZAKTUALIZOWANE DO DTO) ---

    @GET("api/products")
    suspend fun getAllProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "id,asc",
        @Query("active") active: Boolean? = null
    ): PaginatedResponse<ProductDTO>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") productId: Long): ProductDTO // <-- ZMIANA

    @GET("api/products/sku/{sku}")
    suspend fun getProductBySku(@Path("sku") sku: String): ProductDTO // <-- ZMIANA

    @GET("api/products/category/{categoryId}")
    suspend fun getProductsByCategory(@Path("categoryId") categoryId: Long): List<ProductDTO> // <-- ZMIANA

    @GET("api/products/search")
    suspend fun searchProducts(@Query("query") query: String): List<ProductDTO> // <-- ZMIANA

    @GET("api/products/active")
    suspend fun getActiveProducts(): List<ProductDTO> // <-- ZMIANA

    @POST("api/products")
    suspend fun createProduct(@Body request: CreateProductRequest): ProductDTO // <-- ZMIANA

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Long,
        @Body request: UpdateProductRequest
    ): ProductDTO // <-- ZMIANA

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Long): Response<Unit> // <-- ZMIANA

    @PATCH("api/products/{id}/toggle-active")
    suspend fun toggleProductActive(@Path("id") productId: Long): ProductDTO // <-- ZMIANA
}

// ... (TestService i HealthService bez zmian) ...
interface TestService {
    @GET("/api/test/public")
    suspend fun getPublicEndpoint(): Response<Map<String, Any>>
    @GET("/api/test/protected")
    suspend fun getProtectedEndpoint(): Response<Map<String, Any>>
    @GET("/api/test/admin")
    suspend fun getAdminEndpoint(): Response<Map<String, Any>>
}

interface HealthService {
    @GET("/api/health")
    suspend fun getHealth(): Response<HealthStatus>
    @GET("/api/status")
    suspend fun getStatus(): Response<SystemStatus>
}