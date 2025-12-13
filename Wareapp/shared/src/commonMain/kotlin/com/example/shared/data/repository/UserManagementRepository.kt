package com.example.shared.data.repository

import com.example.shared.data.model.*
import com.example.shared.data.remote.ApiService

class UserManagementRepository(private val apiService: ApiService) {

    private val TAG = "UserManagementRepo"

    suspend fun getAllUsers(page: Int, size: Int): Result<PaginatedResponse<AdminUserResponse>> {
        return try {
            val apiResponse = apiService.getAllUsers(page = page, size = size)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (getAllUsers): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Nieoczekiwany błąd (getAllUsers): ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String, page: Int, size: Int): Result<PaginatedResponse<AdminUserResponse>> {
        return try {
            val apiResponse = apiService.searchUsers(query = query, page = page, size = size)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd przy wyszukiwaniu: ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Nieoczekiwany błąd przy wyszukiwaniu: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: Long): Result<AdminUserResponse> {
        return try {
            val apiResponse = apiService.getUserById(userId)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (getUserById): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd pobierania użytkownika: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateUser(userId: Long, request: UpdateUserRequest): Result<AdminUserResponse> {
        return try {
            val apiResponse = apiService.updateUser(userId, request)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (updateUser): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd aktualizacji użytkownika: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createUser(request: AdminCreateUserRequest): Result<AdminUserResponse> {
        return try {
            val apiResponse = apiService.createUser(request)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (createUser): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd tworzenia użytkownika: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: Long): Result<Unit> {
        return try {
            val apiResponse = apiService.deleteUser(userId)
            if (apiResponse.success) {
                Result.success(Unit)
            } else {
                println("$TAG: API zwróciło błąd (deleteUser): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd usuwania użytkownika: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAllRoles(): Result<List<RoleResponse>> {
        return try {
            val apiResponse = apiService.getAllRoles()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (getAllRoles): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Nieoczekiwany błąd (getAllRoles): ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createRole(request: RoleRequest): Result<RoleResponse> {
        return try {
            val apiResponse = apiService.createRole(request)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (createRole): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd tworzenia roli: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateRole(roleId: Long, request: RoleRequest): Result<RoleResponse> {
        return try {
            val apiResponse = apiService.updateRole(roleId, request)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (updateRole): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd aktualizacji roli: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteRole(roleId: Long): Result<Unit> {
        return try {
            val apiResponse = apiService.deleteRole(roleId)
            if (apiResponse.success) {
                Result.success(Unit)
            } else {
                println("$TAG: API zwróciło błąd (deleteRole): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd usuwania roli: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAllPermissions(): Result<List<PermissionResponse>> {
        return try {
            val apiResponse = apiService.getAllPermissions()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (getAllPermissions): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Nieoczekiwany błąd (getAllPermissions): ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createPermission(request: PermissionRequest): Result<PermissionResponse> {
        return try {
            val apiResponse = apiService.createPermission(request)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (createPermission): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd tworzenia uprawnienia: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updatePermission(permissionId: Long, request: PermissionRequest): Result<PermissionResponse> {
        return try {
            val apiResponse = apiService.updatePermission(permissionId, request)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                println("$TAG: API zwróciło błąd (updatePermission): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd aktualizacji uprawnienia: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deletePermission(permissionId: Long): Result<Unit> {
        return try {
            val apiResponse = apiService.deletePermission(permissionId)
            if (apiResponse.success) {
                Result.success(Unit)
            } else {
                println("$TAG: API zwróciło błąd (deletePermission): ${apiResponse.message}")
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            println("$TAG: Błąd usuwania uprawnienia: ${e.message}")
            Result.failure(e)
        }
    }
}