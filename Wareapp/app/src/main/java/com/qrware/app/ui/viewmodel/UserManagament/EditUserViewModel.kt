package com.qrware.app.ui.viewmodel.UserManagament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.AdminUserResponse
import com.qrware.app.data.model.UpdateUserRequest
import com.qrware.app.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stan formularza edycji użytkownika.
 */
data class EditUserFormState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "", // Używamy "" dla null
    val active: Boolean = true,
    val emailVerified: Boolean = false,
    val roles: Set<String> = emptySet()
)

/**
 * Stan całego ekranu edycji.
 */
data class EditUserUiState(
    val isLoading: Boolean = true, // Zaczynamy od ładowania
    val isSaving: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false, // Flaga do nawigacji wstecz
    val user: AdminUserResponse? = null, // Przechowuje oryginalne dane
    val formState: EditUserFormState = EditUserFormState()
)

class EditUserViewModel(
    private val repository: UserManagementRepository,
    private val userId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUserUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserDetails()
    }

    /**
     * Pobiera aktualne dane użytkownika i wypełnia formularz.
     */
    private fun loadUserDetails() {
        viewModelScope.launch {
            repository.getUserById(userId)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            // Wypełnij stan formularza danymi z serwera
                            formState = EditUserFormState(
                                email = user.email,
                                firstName = user.firstName,
                                lastName = user.lastName,
                                phone = user.phone ?: "",
                                active = user.active,
                                emailVerified = user.emailVerified,
                                roles = user.roles.toSet()
                            )
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
     * Wysyła zaktualizowane dane do serwera.
     */
    fun saveUser() {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, error = null) }

        val currentForm = _uiState.value.formState

        // Utwórz obiekt żądania
        val request = UpdateUserRequest(
            email = currentForm.email,
            firstName = currentForm.firstName,
            lastName = currentForm.lastName,
            phone = currentForm.phone.ifEmpty { null }, // Zamień "" z powrotem na null
            active = currentForm.active,
            emailVerified = currentForm.emailVerified,
            roles = currentForm.roles
        )

        viewModelScope.launch {
            repository.updateUser(userId, request)
                .onSuccess {
                    _uiState.update {
                        it.copy(isSaving = false, updateSuccess = true)
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Błąd aktualizacji: ${exception.message}"
                        )
                    }
                }
        }
    }

    // --- Funkcje obsługi zmian w formularzu ---

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(formState = it.formState.copy(email = email)) }
    }

    fun onFirstNameChange(name: String) {
        _uiState.update { it.copy(formState = it.formState.copy(firstName = name)) }
    }

    fun onLastNameChange(name: String) {
        _uiState.update { it.copy(formState = it.formState.copy(lastName = name)) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(formState = it.formState.copy(phone = phone)) }
    }

    fun onActiveChange(isActive: Boolean) {
        _uiState.update { it.copy(formState = it.formState.copy(active = isActive)) }
    }

    fun onEmailVerifiedChange(isVerified: Boolean) {
        _uiState.update { it.copy(formState = it.formState.copy(emailVerified = isVerified)) }
    }

    fun onRolesChange(rolesText: String) {
        // Prosta implementacja: role oddzielone przecinkami
        val rolesSet = rolesText
            .split(",")
            .map { it.trim().uppercase() } // Normalizuj dane
            .filter { it.isNotEmpty() }
            .toSet()
        _uiState.update { it.copy(formState = it.formState.copy(roles = rolesSet)) }
    }

    /**
     * Resetuje flagę sukcesu po nawigacji.
     */
    fun onUpdateSuccessConsumed() {
        _uiState.update { it.copy(updateSuccess = false) }
    }
}

/**
 * Factory dla EditUserViewModel, ponieważ potrzebuje userId w konstruktorze.
 */
class EditUserViewModelFactory(
    private val repository: UserManagementRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditUserViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}