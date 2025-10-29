package com.qrware.app.data.remote

import com.qrware.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

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

    // --- ENDPOINTY UŻYTKOWNIKÓW (istniejące) ---
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


    // --- NOWE ENDPOINTY RÓL ---

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

    // --- NOWE ENDPOINTY UPRAWNIEŃ ---

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
}
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

