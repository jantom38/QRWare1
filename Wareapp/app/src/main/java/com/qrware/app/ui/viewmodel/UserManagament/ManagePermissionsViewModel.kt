package com.qrware.app.ui.viewmodel.UserManagament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.PermissionRequest
import com.qrware.app.data.model.PermissionResponse
import com.qrware.app.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManagePermissionsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val permissions: List<PermissionResponse> = emptyList(),
    val allPermissions: List<PermissionResponse> = emptyList(),
    val searchQuery: String = "",
    val showDialog: PermissionDialogState = PermissionDialogState.None
)

sealed class PermissionDialogState {
    object None : PermissionDialogState()
    object Create : PermissionDialogState()
    data class Edit(val permission: PermissionResponse) : PermissionDialogState()
    data class Delete(val permission: PermissionResponse) : PermissionDialogState()
}

class ManagePermissionsViewModel(private val repository: UserManagementRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagePermissionsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPermissions()
    }

    fun loadPermissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getAllPermissions()
                .onSuccess { permissionsList ->
                    _uiState.update {
                        it.copy(
                            isLoading = false, 
                            allPermissions = permissionsList,
                            permissions = filterPermissions(permissionsList, it.searchQuery)
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Nieznany błąd")
                    }
                }
        }
    }

    fun searchPermissions(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                permissions = filterPermissions(currentState.allPermissions, query)
            )
        }
    }

    private fun filterPermissions(permissions: List<PermissionResponse>, query: String): List<PermissionResponse> {
        if (query.isBlank()) return permissions
        
        val lowerQuery = query.lowercase()
        return permissions.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.description?.lowercase()?.contains(lowerQuery) == true ||
            it.resource.lowercase().contains(lowerQuery) ||
            it.action.lowercase().contains(lowerQuery)
        }
    }

    fun requestCreatePermission() {
        _uiState.update { it.copy(showDialog = PermissionDialogState.Create, error = null) }
    }

    fun requestEditPermission(permission: PermissionResponse) {
        _uiState.update { it.copy(showDialog = PermissionDialogState.Edit(permission), error = null) }
    }

    fun requestDeletePermission(permission: PermissionResponse) {
        _uiState.update { it.copy(showDialog = PermissionDialogState.Delete(permission), error = null) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showDialog = PermissionDialogState.None) }
    }

    fun savePermission(request: PermissionRequest, permissionId: Long? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = if (permissionId == null) {
                repository.createPermission(request)
            } else {
                repository.updatePermission(permissionId, request)
            }

            result.onSuccess {
                _uiState.update { it.copy(showDialog = PermissionDialogState.None, isLoading = false) }
                loadPermissions()
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(isLoading = false, error = exception.message ?: "Błąd zapisu")
                }
            }
        }
    }

    fun deletePermission(permission: PermissionResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.deletePermission(permission.id)
                .onSuccess {
                    _uiState.update { it.copy(showDialog = PermissionDialogState.None, isLoading = false) }
                    loadPermissions()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Błąd usuwania")
                    }
                }
        }
    }
}

class ManagePermissionsViewModelFactory(
    private val repository: UserManagementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManagePermissionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManagePermissionsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
