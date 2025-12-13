package com.example.shared.ui.viewmodel.UserManagament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.model.PermissionResponse
import com.example.shared.data.model.RoleRequest
import com.example.shared.data.model.RoleResponse
import com.example.shared.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageRolesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val roles: List<RoleResponse> = emptyList(),
    val allRoles: List<RoleResponse> = emptyList(),
    val searchQuery: String = "",
    val allPermissions: List<PermissionResponse> = emptyList(),
    val showDialog: DialogState = DialogState.None
)

sealed class DialogState {
    object None : DialogState()
    object Create : DialogState()
    data class Edit(val role: RoleResponse) : DialogState()
    data class Delete(val role: RoleResponse) : DialogState()
}

class ManageRolesViewModel(private val repository: UserManagementRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageRolesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val rolesResult = repository.getAllRoles()
            val permsResult = repository.getAllPermissions()

            var finalRoles: List<RoleResponse> = emptyList()
            var finalPerms: List<PermissionResponse> = emptyList()
            var errorMessage: String? = null

            rolesResult.onSuccess {
                finalRoles = it
            }.onFailure {
                errorMessage = it.message ?: "Błąd pobierania ról"
            }

            permsResult.onSuccess {
                finalPerms = it
            }.onFailure {
                val permsError = it.message ?: "Błąd pobierania uprawnień"
                errorMessage = if (errorMessage != null) "$errorMessage; $permsError" else permsError
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    roles = filterRoles(finalRoles, it.searchQuery),
                    allRoles = finalRoles,
                    allPermissions = finalPerms,
                    error = errorMessage
                )
            }
        }
    }

    fun requestCreateRole() {
        _uiState.update { it.copy(showDialog = DialogState.Create, error = null) }
    }

    fun requestEditRole(role: RoleResponse) {
        _uiState.update { it.copy(showDialog = DialogState.Edit(role), error = null) }
    }

    fun requestDeleteRole(role: RoleResponse) {
        _uiState.update { it.copy(showDialog = DialogState.Delete(role), error = null) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showDialog = DialogState.None) }
    }

    fun searchRoles(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                roles = filterRoles(currentState.allRoles, query)
            )
        }
    }

    private fun filterRoles(roles: List<RoleResponse>, query: String): List<RoleResponse> {
        if (query.isBlank()) return roles

        val lowerQuery = query.lowercase()
        return roles.filter {
            it.name.lowercase().contains(lowerQuery) ||
                    it.description?.lowercase()?.contains(lowerQuery) == true ||
                    it.permissions.any { permission -> permission.lowercase().contains(lowerQuery) }
        }
    }

    fun saveRole(request: RoleRequest, roleId: Long? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = if (roleId == null) {
                repository.createRole(request)
            } else {
                repository.updateRole(roleId, request)
            }

            result.onSuccess {
                _uiState.update { it.copy(showDialog = DialogState.None, isLoading = false) }
                loadData()
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(isLoading = false, error = exception.message ?: "Błąd zapisu")
                }
            }
        }
    }

    fun deleteRole(role: RoleResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.deleteRole(role.id)
                .onSuccess {
                    _uiState.update { it.copy(showDialog = DialogState.None, isLoading = false) }
                    loadData()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Błąd usuwania")
                    }
                }
        }
    }
}