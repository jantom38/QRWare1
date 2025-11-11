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
    suspend fun getProductById(@Path("id") productId: Long): ProductDTO

    @GET("api/products/sku/{sku}")
    suspend fun getProductBySku(@Path("sku") sku: String): ProductDTO


    @GET("api/products/category/{categoryId}")
    suspend fun getProductsByCategory(@Path("categoryId") categoryId: Long): List<ProductDTO>

    @GET("api/products/search")
    suspend fun searchProducts(@Query("query") query: String): List<ProductDTO>

    @GET("api/products/active")
    suspend fun getActiveProducts(): List<ProductDTO>

    @POST("api/products")
    suspend fun createProduct(@Body request: CreateProductRequest): ProductDTO

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Long,
        @Body request: UpdateProductRequest
    ): ProductDTO

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Long): Response<Unit>

    @PATCH("api/products/{id}/toggle-active")
    suspend fun toggleProductActive(@Path("id") productId: Long): ProductDTO

    // --- ENDPOINTY CATEGORIES ---

    @GET("api/categories")
    suspend fun getAllCategories(): ApiResponse<List<CategoryDTO>>

    @GET("api/categories/active")
    suspend fun getActiveCategories(): ApiResponse<List<CategoryDTO>>

    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") categoryId: Long): ApiResponse<CategoryDTO>

    @GET("api/categories/code/{code}")
    suspend fun getCategoryByCode(@Path("code") code: String): ApiResponse<CategoryDTO>

    @GET("api/categories/search")
    suspend fun searchCategories(@Query("query") query: String): ApiResponse<List<CategoryDTO>>

    @GET("api/categories/root")
    suspend fun getRootCategories(): ApiResponse<List<CategoryDTO>>

    @GET("api/categories/{id}/children")
    suspend fun getChildCategories(@Path("id") categoryId: Long): ApiResponse<List<CategoryDTO>>

    @POST("api/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): ApiResponse<CategoryDTO>

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") categoryId: Long,
        @Body request: UpdateCategoryRequest
    ): ApiResponse<CategoryDTO>

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") categoryId: Long): ApiResponse<Unit>

    @PATCH("api/categories/{id}/toggle-active")
    suspend fun toggleCategoryActive(@Path("id") categoryId: Long): ApiResponse<CategoryDTO>

//--- OBSŁUGA QR KODÓW (POPRAWIONE) ---

    @GET("api/qr-codes")
    suspend fun getAllQRCodes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "id,desc"
    ): PaginatedResponse<QRCodeData> // <-- POPRAWKA

    @GET("api/qr-codes/{id}")
    suspend fun getQRCodeById(@Path("id") qrCodeId: Long): QRCodeData // <-- POPRAWKA

    @GET("api/qr-codes/code/{code}")
    suspend fun scanQRCode(@Path("code") code: String): QRCodeData // <-- POPRAWKA

    @GET("api/qr-codes/entity/{entityType}/{entityId}")
    suspend fun getQRCodeByEntity(
        @Path("entityType") entityType: String,
        @Path("entityId") entityId: Long
    ): QRCodeData // <-- POPRAWKA

    @GET("api/qr-codes/active")
    suspend fun getActiveQRCodes(): List<QRCodeData> // <-- POPRAWKA

    @GET("api/qr-codes/type/{type}")
    suspend fun getQRCodesByType(@Path("type") type: QRCodeType): List<QRCodeData> // <-- POPRAWKA

    @POST("api/qr-codes/generate")
    suspend fun generateQRCode(@Body request: GenerateQRRequest): QRCodeData // <-- POPRAWKA

    @PUT("api/qr-codes/{id}")
    suspend fun updateQRCode(
        @Path("id") qrCodeId: Long,
        @Body request: UpdateQRRequest
    ): QRCodeData // <-- POPRAWKA

    @DELETE("api/qr-codes/{id}")
    suspend fun deleteQRCode(@Path("id") qrCodeId: Long): Response<Unit> // <-- POPRAWKA

    @PATCH("api/qr-codes/{id}/toggle-active")
    suspend fun toggleQRCodeActive(@Path("id") qrCodeId: Long): QRCodeData // <-- POPRAWKA

    @GET("api/qr-codes/stats")
    suspend fun getQRStats(): QRStatsResponse // <-- POPRAWKA


//Location obsługa

    @GET("api/locations")
    suspend fun getAllLocations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "id,asc",
        @Query("active") active: Boolean? = null
    ): PaginatedResponse<LocationDTO> // Wymaga istnienia PaginatedResponse

    @GET("api/locations/{id}")
    suspend fun getLocationById(@Path("id") locationId: Long): LocationDTO

    @POST("api/locations")
    suspend fun createLocation(@Body request: CreateLocationRequest): LocationDTO

    @PUT("api/locations/{id}")
    suspend fun updateLocation(
        @Path("id") locationId: Long,
        @Body request: UpdateLocationRequest
    ): LocationDTO

    @DELETE("api/locations/{id}")
    suspend fun deleteLocation(@Path("id") locationId: Long): Response<Unit>

    @PATCH("api/locations/{id}/toggle-active")
    suspend fun toggleLocationActive(@Path("id") locationId: Long): LocationDTO

    @GET("api/zones")
    suspend fun getZones(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 1000, // Pobierzmy dużo na raz
        @Query("active") active: Boolean? = true // Chcemy tylko aktywne
    ): PaginatedResponse<ZoneDTO>
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