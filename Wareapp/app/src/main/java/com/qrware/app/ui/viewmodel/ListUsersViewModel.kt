package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.AdminUserResponse
import com.qrware.app.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Definicja klasy stanu UI
data class ListUsersUiState(
    val isLoading: Boolean = false,
    val users: List<AdminUserResponse> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val canLoadMore: Boolean = false,
    // Pola do obsługi dialogu usuwania
    val showDeleteDialog: Boolean = false,
    val userToDelete: AdminUserResponse? = null
)

// 2. ViewModel
class ListUsersViewModel(
    private val repository: UserManagementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUsersUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        // Załaduj pierwszą stronę użytkowników przy starcie ViewModelu
        loadUsers(page = 0)
    }

    fun loadUsers(page: Int) {
        // ZMIANA: Usunięto błędny warunek 'if (_uiState.value.isLoading) return'
        // Pozwala to na wykonanie wywołania z bloku 'init'

        viewModelScope.launch {
            // Ustaw stan ładowania (szczególnie ważne przy paginacji)
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getAllUsers(page = page, size = 20)
                .onSuccess { response ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            // Jeśli page == 0, zastąp listę, inaczej dodaj do listy
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

    /**
     * Funkcja do ładowania kolejnej strony (dla infinite scroll).
     */
    fun loadNextPage() {
        val currentState = _uiState.value
        // Ten warunek jest poprawny i zapobiega podwójnemu ładowaniu
        if (!currentState.isLoading && currentState.canLoadMore) {
            loadUsers(page = currentState.currentPage + 1)
        }
    }

    /**
     * Odświeża listę (np. po powrocie z ekranu edycji/dodawania).
     */
    fun refreshList() {
        _uiState.update { it.copy(users = emptyList(), currentPage = 0, totalPages = 0) }
        loadUsers(page = 0)
    }

    // --- Funkcje obsługi usuwania (bez zmian) ---

    /**
     * Wyświetla dialog potwierdzenia usunięcia.
     */
    fun requestDeleteUser(user: AdminUserResponse) {
        _uiState.update { it.copy(showDeleteDialog = true, userToDelete = user) }
    }

    /**
     * Zamyka dialog potwierdzenia usunięcia.
     */
    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, userToDelete = null) }
    }

    /**
     * Potwierdza i wykonuje usunięcie użytkownika.
     */
    fun confirmDeleteUser() {
        val userToDelete = _uiState.value.userToDelete ?: return
        // Zamknij dialog
        dismissDeleteDialog()

        viewModelScope.launch {
            repository.deleteUser(userToDelete.id)
                .onSuccess {
                    // Usuń użytkownika z listy w UI (optymistyczna aktualizacja)
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

// 3. Factory dla ViewModelu (bez zmian)
class ListUsersViewModelFactory(
    private val repository: UserManagementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListUsersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListUsersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}