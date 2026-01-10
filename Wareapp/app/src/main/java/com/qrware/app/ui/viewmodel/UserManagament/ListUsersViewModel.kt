package com.qrware.app.ui.viewmodel.UserManagament

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
    val successMessage: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val canLoadMore: Boolean = false,
    val searchQuery: String = "",
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
     * Wyszukuje użytkowników na podstawie zapytania.
     */
    fun searchUsers(query: String) {
        _uiState.update { it.copy(searchQuery = query, users = emptyList(), currentPage = 0, totalPages = 0) }
        loadUsers(page = 0)
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

    fun requestPasswordReset(email: String) {
        // Tutaj powinna być logika wysyłania żądania resetu hasła.
        // Ponieważ w UserManagementRepository nie ma metody requestPasswordReset,
        // a jest ona w AuthRepository, musimy albo dodać ją do UserManagementRepository,
        // albo wstrzyknąć AuthRepository do tego ViewModelu.
        // Zakładając, że UserManagementRepository jest głównym repozytorium dla tego ekranu,
        // dodam metodę do UserManagementRepository (która może delegować do ApiService).
        // Ale ApiService ma requestPasswordReset w AuthService.
        // W tym przypadku, dla uproszczenia, zakładam, że metoda requestPasswordReset w AuthRepository
        // jest tą właściwą, ale ListUsersViewModel korzysta z UserManagementRepository.
        // Najlepiej byłoby dodać metodę do UserManagementRepository, która wywołuje odpowiedni endpoint.
        // Jednak endpoint /api/auth/forgot-password jest publiczny i przyjmuje email.
        // Możemy go wywołać.

        // UWAGA: W poprzednim kroku dodałem requestPasswordReset do AuthRepository.
        // Ale ListUsersViewModel używa UserManagementRepository.
        // Aby nie zmieniać konstruktora ViewModelu i fabryki (co wymagałoby zmian w AppContainer),
        // dodam metodę do UserManagementRepository, która będzie wywoływać ten sam endpoint co AuthRepository,
        // lub po prostu użyję ApiService jeśli tam jest ten endpoint (a jest w AuthService).
        
        // Ponieważ nie mam dostępu do AuthRepository tutaj, a nie chcę komplikować DI,
        // dodam metodę do UserManagementRepository.
        
        viewModelScope.launch {
             repository.requestPasswordReset(email)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Wysłano żądanie resetu hasła na email: $email") }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(error = "Błąd resetowania hasła: ${exception.message}") }
                }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
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