package com.example.shared.ui.viewmodel.UserManagament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.model.AdminUserResponse
import com.example.shared.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListUsersUiState(
    val isLoading: Boolean = false,
    val users: List<AdminUserResponse> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val canLoadMore: Boolean = false,
    val searchQuery: String = "",
    val showDeleteDialog: Boolean = false,
    val userToDelete: AdminUserResponse? = null
)

class ListUsersViewModel(
    private val repository: UserManagementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUsersUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadUsers(page = 0)
    }

    fun loadUsers(page: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentQuery = _uiState.value.searchQuery
            val result = if (currentQuery.isBlank()) {
                repository.getAllUsers(page = page, size = 20)
            } else {
                repository.searchUsers(query = currentQuery, page = page, size = 20)
            }

            result.onSuccess { response ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        users = if (page == 0) response.content else currentState.users + response.content,
                        currentPage = response.number,
                        totalPages = response.totalPages,
                        canLoadMore = !response.last
                    )
                }
            }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Błąd pobierania danych: ${exception.message}"
                        )
                    }
                }
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (!currentState.isLoading && currentState.canLoadMore) {
            loadUsers(page = currentState.currentPage + 1)
        }
    }

    fun searchUsers(query: String) {
        _uiState.update { it.copy(searchQuery = query, users = emptyList(), currentPage = 0, totalPages = 0) }
        loadUsers(page = 0)
    }

    fun refreshList() {
        _uiState.update { it.copy(users = emptyList(), currentPage = 0, totalPages = 0) }
        loadUsers(page = 0)
    }

    fun requestDeleteUser(user: AdminUserResponse) {
        _uiState.update { it.copy(showDeleteDialog = true, userToDelete = user) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, userToDelete = null) }
    }

    fun confirmDeleteUser() {
        val userToDelete = _uiState.value.userToDelete ?: return
        dismissDeleteDialog()

        viewModelScope.launch {
            repository.deleteUser(userToDelete.id)
                .onSuccess {
                    _uiState.update { currentState ->
                        currentState.copy(
                            users = currentState.users.filterNot { it.id == userToDelete.id },
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = "Błąd usuwania: ${exception.message}")
                    }
                }
        }
    }
}