package com.example.shared.data.remote

import com.example.shared.data.dto.*
import com.example.shared.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*

class AuthService(private val client: HttpClient) {
    suspend fun login(request: LoginRequest): ApiResponse<AuthenticationResponse> {
        return client.post("/api/auth/login") {
            setBody(request)
        }.body()
    }

    suspend fun getCurrentUser(): ApiResponse<UserInfoResponse> {
        return client.get("/api/auth/me").body()
    }

    suspend fun logout(): ApiResponse<Unit> {
        return client.post("/api/auth/logout").body()
    }

    suspend fun register(request: RegisterRequest): ApiResponse<AuthenticationResponse> {
        return client.post("/api/auth/register") {
            setBody(request)
        }.body()
    }
}

class ApiService(private val client: HttpClient) {

    // --- UŻYTKOWNICY ---
    suspend fun getAllUsers(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc"
    ): ApiResponse<PaginatedResponse<AdminUserResponse>> {
        return client.get("api/users") {
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
        }.body()
    }

    suspend fun getUserById(userId: Long): ApiResponse<AdminUserResponse> {
        return client.get("api/users/$userId").body()
    }

    suspend fun searchUsers(
        query: String,
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc"
    ): ApiResponse<PaginatedResponse<AdminUserResponse>> {
        return client.get("api/users/search") {
            parameter("query", query)
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
        }.body()
    }

    suspend fun updateUser(userId: Long, request: UpdateUserRequest): ApiResponse<AdminUserResponse> {
        return client.put("api/users/$userId") {
            setBody(request)
        }.body()
    }

    suspend fun createUser(request: AdminCreateUserRequest): ApiResponse<AdminUserResponse> {
        return client.post("api/users") {
            setBody(request)
        }.body()
    }

    suspend fun deleteUser(userId: Long): ApiResponse<Unit> {
        return client.delete("api/users/$userId").body()
    }

    // --- ROLE I UPRAWNIENIA ---
    suspend fun getAllRoles(): ApiResponse<List<RoleResponse>> {
        return client.get("api/roles").body()
    }

    suspend fun createRole(request: RoleRequest): ApiResponse<RoleResponse> {
        return client.post("api/roles") {
            setBody(request)
        }.body()
    }

    suspend fun getRoleById(roleId: Long): ApiResponse<RoleResponse> {
        return client.get("api/roles/$roleId").body()
    }

    suspend fun updateRole(roleId: Long, request: RoleRequest): ApiResponse<RoleResponse> {
        return client.put("api/roles/$roleId") {
            setBody(request)
        }.body()
    }

    suspend fun deleteRole(roleId: Long): ApiResponse<Unit> {
        return client.delete("api/roles/$roleId").body()
    }

    suspend fun getAllPermissions(): ApiResponse<List<PermissionResponse>> {
        return client.get("api/permissions").body()
    }

    suspend fun createPermission(request: PermissionRequest): ApiResponse<PermissionResponse> {
        return client.post("api/permissions") {
            setBody(request)
        }.body()
    }

    suspend fun getPermissionById(permissionId: Long): ApiResponse<PermissionResponse> {
        return client.get("api/permissions/$permissionId").body()
    }

    suspend fun updatePermission(permissionId: Long, request: PermissionRequest): ApiResponse<PermissionResponse> {
        return client.put("api/permissions/$permissionId") {
            setBody(request)
        }.body()
    }

    suspend fun deletePermission(permissionId: Long): ApiResponse<Unit> {
        return client.delete("api/permissions/$permissionId").body()
    }

    // --- INVENTORY ---
    suspend fun getAllInventoryItems(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc"
    ): PaginatedResponse<InventoryItemDTO> {
        return client.get("api/inventory") {
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
        }.body()
    }

    suspend fun searchInventory(query: String): List<InventoryItemDTO> {
        return client.get("api/inventory/search") {
            parameter("query", query)
        }.body()
    }

    suspend fun getInventoryItemById(itemId: Long): InventoryItemDTO {
        return client.get("api/inventory/$itemId").body()
    }

    suspend fun getInventoryByProduct(productId: Long): List<InventoryItemDTO> {
        return client.get("api/inventory/product/$productId").body()
    }

    suspend fun getInventoryByLocation(locationId: Long): List<InventoryItemDTO> {
        return client.get("api/inventory/location/$locationId").body()
    }

    suspend fun getInventoryByStatus(status: InventoryStatus): List<InventoryItemDTO> {
        return client.get("api/inventory/status/$status").body()
    }

    suspend fun createInventoryItem(request: CreateInventoryRequest): InventoryItemDTO {
        return client.post("api/inventory") {
            setBody(request)
        }.body()
    }

    suspend fun updateInventoryItem(itemId: Long, request: UpdateInventoryRequest): InventoryItemDTO {
        return client.put("api/inventory/$itemId") {
            setBody(request)
        }.body()
    }

    suspend fun deleteInventoryItem(itemId: Long) {
        client.delete("api/inventory/$itemId")
    }

    suspend fun receiveStock(itemId: Long, request: QuantityUpdateRequest): InventoryItemDTO {
        return client.post("api/inventory/$itemId/receive") {
            setBody(request)
        }.body()
    }

    suspend fun issueStock(itemId: Long, request: QuantityUpdateRequest): InventoryItemDTO {
        return client.post("api/inventory/$itemId/issue") {
            setBody(request)
        }.body()
    }

    suspend fun getInventoryByQRCode(qrCode: String): InventoryItemDTO {
        return client.get("api/inventory/qr/$qrCode").body()
    }

    // --- PRODUCTS ---
    suspend fun getAllProducts(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc",
        active: Boolean? = null
    ): PaginatedResponse<ProductDTO> {
        return client.get("api/products") {
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
            if (active != null) parameter("active", active)
        }.body()
    }

    suspend fun getProductById(productId: Long): ProductDTO {
        return client.get("api/products/$productId").body()
    }

    suspend fun getProductBySku(sku: String): ProductDTO {
        return client.get("api/products/sku/$sku").body()
    }

    suspend fun getProductsByCategory(categoryId: Long): List<ProductDTO> {
        return client.get("api/products/category/$categoryId").body()
    }

    suspend fun searchProducts(query: String): List<ProductDTO> {
        return client.get("api/products/search") {
            parameter("query", query)
        }.body()
    }

    suspend fun getActiveProducts(): List<ProductDTO> {
        return client.get("api/products/active").body()
    }

    suspend fun createProduct(request: CreateProductRequest): ProductDTO {
        return client.post("api/products") {
            setBody(request)
        }.body()
    }

    suspend fun updateProduct(productId: Long, request: UpdateProductRequest): ProductDTO {
        return client.put("api/products/$productId") {
            setBody(request)
        }.body()
    }

    suspend fun deleteProduct(productId: Long) {
        client.delete("api/products/$productId")
    }

    suspend fun toggleProductActive(productId: Long): ProductDTO {
        return client.patch("api/products/$productId/toggle-active").body()
    }

    // --- CATEGORIES ---
    suspend fun getAllCategories(): ApiResponse<List<CategoryDTO>> {
        return client.get("api/categories").body()
    }

    suspend fun getActiveCategories(): ApiResponse<List<CategoryDTO>> {
        return client.get("api/categories/active").body()
    }

    suspend fun getCategoryById(categoryId: Long): ApiResponse<CategoryDTO> {
        return client.get("api/categories/$categoryId").body()
    }

    suspend fun getCategoryByCode(code: String): ApiResponse<CategoryDTO> {
        return client.get("api/categories/code/$code").body()
    }

    suspend fun searchCategories(query: String): ApiResponse<List<CategoryDTO>> {
        return client.get("api/categories/search") {
            parameter("query", query)
        }.body()
    }

    suspend fun getRootCategories(): ApiResponse<List<CategoryDTO>> {
        return client.get("api/categories/root").body()
    }

    suspend fun getChildCategories(categoryId: Long): ApiResponse<List<CategoryDTO>> {
        return client.get("api/categories/$categoryId/children").body()
    }

    suspend fun createCategory(request: CreateCategoryRequest): ApiResponse<CategoryDTO> {
        return client.post("api/categories") {
            setBody(request)
        }.body()
    }

    suspend fun updateCategory(categoryId: Long, request: UpdateCategoryRequest): ApiResponse<CategoryDTO> {
        return client.put("api/categories/$categoryId") {
            setBody(request)
        }.body()
    }

    suspend fun deleteCategory(categoryId: Long): ApiResponse<Unit> {
        return client.delete("api/categories/$categoryId").body()
    }

    suspend fun toggleCategoryActive(categoryId: Long): ApiResponse<CategoryDTO> {
        return client.patch("api/categories/$categoryId/toggle-active").body()
    }

    // --- QR CODES ---
    suspend fun getAllQRCodes(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,desc"
    ): PaginatedResponse<QRCodeData> {
        return client.get("api/qr-codes") {
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
        }.body()
    }

    suspend fun getQRCodeById(qrCodeId: Long): QRCodeData {
        return client.get("api/qr-codes/$qrCodeId").body()
    }

    suspend fun scanQRCode(code: String): QRCodeData {
        return client.get("api/qr-codes/code/$code").body()
    }

    suspend fun getQRCodeByEntity(entityType: String, entityId: Long): QRCodeData {
        return client.get("api/qr-codes/entity/$entityType/$entityId").body()
    }

    suspend fun getActiveQRCodes(): List<QRCodeData> {
        return client.get("api/qr-codes/active").body()
    }

    suspend fun getQRCodesByType(type: QRCodeType): List<QRCodeData> {
        return client.get("api/qr-codes/type/$type").body()
    }

    suspend fun generateQRCode(request: GenerateQRRequest): QRCodeData {
        return client.post("api/qr-codes/generate") {
            setBody(request)
        }.body()
    }

    suspend fun updateQRCode(qrCodeId: Long, request: UpdateQRRequest): QRCodeData {
        return client.put("api/qr-codes/$qrCodeId") {
            setBody(request)
        }.body()
    }

    suspend fun deleteQRCode(qrCodeId: Long) {
        client.delete("api/qr-codes/$qrCodeId")
    }

    suspend fun toggleQRCodeActive(qrCodeId: Long): QRCodeData {
        return client.patch("api/qr-codes/$qrCodeId/toggle-active").body()
    }

    suspend fun getQRStats(): QRStatsResponse {
        return client.get("api/qr-codes/stats").body()
    }

    // --- LOCATIONS ---
    suspend fun getAllLocations(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,asc",
        active: Boolean? = null
    ): PaginatedResponse<LocationDTO> {
        return client.get("api/locations") {
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
            if (active != null) parameter("active", active)
        }.body()
    }

    suspend fun getLocationById(locationId: Long): LocationDTO {
        return client.get("api/locations/$locationId").body()
    }

    suspend fun searchLocations(
        query: String,
        page: Int = 0,
        size: Int = 20,
        active: Boolean? = null
    ): PaginatedResponse<LocationDTO> {
        return client.get("api/locations/search") {
            parameter("query", query)
            parameter("page", page)
            parameter("size", size)
            if (active != null) parameter("active", active)
        }.body()
    }

    suspend fun createLocation(request: CreateLocationRequest): LocationDTO {
        return client.post("api/locations") {
            setBody(request)
        }.body()
    }

    suspend fun updateLocation(locationId: Long, request: UpdateLocationRequest): LocationDTO {
        return client.put("api/locations/$locationId") {
            setBody(request)
        }.body()
    }

    suspend fun deleteLocation(locationId: Long) {
        client.delete("api/locations/$locationId")
    }

    suspend fun toggleLocationActive(locationId: Long): LocationDTO {
        return client.patch("api/locations/$locationId/toggle-active").body()
    }

    // --- ZONES ---
    suspend fun getZones(
        page: Int = 0,
        size: Int = 1000,
        active: Boolean? = null
    ): PaginatedResponse<ZoneDTO> {
        return client.get("api/zones") {
            parameter("page", page)
            parameter("size", size)
            if (active != null) parameter("active", active)
        }.body()
    }

    suspend fun getZoneById(id: Long): ZoneDTO {
        return client.get("api/zones/$id").body()
    }

    suspend fun createZone(request: CreateZoneRequest): ZoneDTO {
        return client.post("api/zones") {
            setBody(request)
        }.body()
    }

    suspend fun updateZone(id: Long, request: UpdateZoneRequest): ZoneDTO {
        return client.put("api/zones/$id") {
            setBody(request)
        }.body()
    }

    suspend fun deleteZone(id: Long) {
        client.delete("api/zones/$id")
    }

    suspend fun toggleZoneActive(id: Long): ZoneDTO {
        return client.patch("api/zones/$id/toggle-active").body()
    }
}

class TestService(private val client: HttpClient) {
    suspend fun getPublicEndpoint(): Map<String, Any> {
        return client.get("/api/test/public").body()
    }

    suspend fun getProtectedEndpoint(): Map<String, Any> {
        return client.get("/api/test/protected").body()
    }

    suspend fun getAdminEndpoint(): Map<String, Any> {
        return client.get("/api/test/admin").body()
    }
}

class HealthService(private val client: HttpClient) {
    suspend fun getHealth(): HealthStatus {
        return client.get("/api/health").body()
    }

    suspend fun getStatus(): SystemStatus {
        return client.get("/api/status").body()
    }
}