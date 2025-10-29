// Ścieżka: app/src/main/java/com/qrware/app/data/repository/UserManagementRepository.kt
package com.qrware.app.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.qrware.app.data.model.AdminCreateUserRequest
import com.qrware.app.data.model.AdminUserResponse
import com.qrware.app.data.model.PaginatedResponse
import com.qrware.app.data.model.PermissionRequest
import com.qrware.app.data.model.PermissionResponse
import com.qrware.app.data.model.RoleRequest
import com.qrware.app.data.model.RoleResponse
import com.qrware.app.data.model.UpdateUserRequest
import com.qrware.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class UserManagementRepository(private val apiService: ApiService) {

    // --- Metody Użytkowników (istniejące) ---

    suspend fun getAllUsers(page: Int, size: Int): Result<PaginatedResponse<AdminUserResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.getAllUsers(page = page, size = size)
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd: ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci: ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Nieoczekiwany błąd: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Pobiera szczegóły użytkownika po ID.
     */
    suspend fun getUserById(userId: Long): Result<AdminUserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.getUserById(userId)
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd (getUserById): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (getUserById): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd pobierania użytkownika: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Aktualizuje dane użytkownika.
     */
    suspend fun updateUser(userId: Long, request: UpdateUserRequest): Result<AdminUserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.updateUser(userId, request)
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd (updateUser): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (updateUser): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd aktualizacji użytkownika: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    suspend fun createUser(request: AdminCreateUserRequest): Result<AdminUserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.createUser(request)
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd (createUser): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (createUser): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd tworzenia użytkownika: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Usuwa użytkownika.
     */
    suspend fun deleteUser(userId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.deleteUser(userId)
                if (apiResponse.success) {
                    Result.success(Unit)
                } else {
                    Log.w(TAG, "API zwróciło błąd (deleteUser): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (deleteUser): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd usuwania użytkownika: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    // --- NOWE METODY: Zarządzanie Rolami ---

    suspend fun getAllRoles(): Result<List<RoleResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.getAllRoles()
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd (getAllRoles): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (getAllRoles): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Nieoczekiwany błąd (getAllRoles): ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun createRole(request: RoleRequest): Result<RoleResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.createRole(request)
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd (createRole): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (createRole): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd tworzenia roli: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun updateRole(roleId: Long, request: RoleRequest): Result<RoleResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.updateRole(roleId, request)
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd (updateRole): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (updateRole): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd aktualizacji roli: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteRole(roleId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.deleteRole(roleId)
                if (apiResponse.success) {
                    Result.success(Unit)
                } else {
                    Log.w(TAG, "API zwróciło błąd (deleteRole): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (deleteRole): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd usuwania roli: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    // --- NOWE METODY: Zarządzanie Uprawnieniami ---

    suspend fun getAllPermissions(): Result<List<PermissionResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.getAllPermissions()
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd (getAllPermissions): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (getAllPermissions): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Nieoczekiwany błąd (getAllPermissions): ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun createPermission(request: PermissionRequest): Result<PermissionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.createPermission(request)
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd (createPermission): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (createPermission): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd tworzenia uprawnienia: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun updatePermission(permissionId: Long, request: PermissionRequest): Result<PermissionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.updatePermission(permissionId, request)
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Log.w(TAG, "API zwróciło błąd (updatePermission): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (updatePermission): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd aktualizacji uprawnienia: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deletePermission(permissionId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.deletePermission(permissionId)
                if (apiResponse.success) {
                    Result.success(Unit)
                } else {
                    Log.w(TAG, "API zwróciło błąd (deletePermission): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
            } catch (e: IOException) {
                Log.e(TAG, "Błąd sieci (deletePermission): ${e.message}", e)
                Result.failure(Exception("Błąd sieci. Sprawdź połączenie."))
            } catch (e: Exception) {
                Log.e(TAG, "Błąd usuwania uprawnienia: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}