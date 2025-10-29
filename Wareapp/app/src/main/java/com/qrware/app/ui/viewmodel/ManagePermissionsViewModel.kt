// Ścieżka: app/src/main/java/com/qrware/app/ui/viewmodel/ManagePermissionsViewModel.kt
package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.PermissionRequest
import com.qrware.app.data.model.PermissionResponse
// BŁĄD: Ta klasa nie istnieje w Twoim projekcie (repozytorium zwraca 'Result')
// import com.qrware.app.data.remote.Resource
import com.qrware.app.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManagePermissionsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val permissions: List<PermissionResponse> = emptyList(),
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

            // POPRAWKA: Używamy .onSuccess i .onFailure
            repository.getAllPermissions()
                .onSuccess { permissionsList ->
                    _uiState.update {
                        it.copy(isLoading = false, permissions = permissionsList)
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Nieznany błąd")
                    }
                }
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
            _uiState.update { it.copy(isLoading = true, error = null) } // Zaktualizuj błąd na null

            val result = if (permissionId == null) {
                repository.createPermission(request)
            } else {
                repository.updatePermission(permissionId, request)
            }

            // POPRAWKA: Używamy .onSuccess i .onFailure
            result.onSuccess {
                _uiState.update { it.copy(showDialog = PermissionDialogState.None, isLoading = false) } // Wyłącz ładowanie
                loadPermissions() // Odśwież listę
            }.onFailure { exception ->
                _uiState.update {
                    // Pozostaw okno otwarte, aby użytkownik widział błąd
                    it.copy(isLoading = false, error = exception.message ?: "Błąd zapisu")
                }
            }
        }
    }

    fun deletePermission(permission: PermissionResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Nie musimy resetować błędu tutaj

            // POPRAWKA: Używamy .onSuccess i .onFailure
            repository.deletePermission(permission.id)
                .onSuccess {
                    _uiState.update { it.copy(showDialog = PermissionDialogState.None, isLoading = false) } // Wyłącz ładowanie
                    loadPermissions() // Odśwież listę
                }
                .onFailure { exception ->
                    _uiState.update {
                        // Pozostaw okno otwarte, aby użytkownik widział błąd
                        it.copy(isLoading = false, error = exception.message ?: "Błąd usuwania")
                    }
                }
        }
    }
}

// Fabryka (bez zmian)
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