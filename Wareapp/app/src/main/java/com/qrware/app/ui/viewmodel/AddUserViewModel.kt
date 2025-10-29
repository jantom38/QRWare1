package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qrware.app.data.model.AdminCreateUserRequest
import com.qrware.app.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stan formularza dodawania użytkownika.
 */
data class AddUserFormState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "", // Używamy "" dla null
    val roles: Set<String> = setOf("USER"), // Domyślna rola
    val active: Boolean = true,
    val emailVerified: Boolean = false
)

/**
 * Stan całego ekranu dodawania.
 */
data class AddUserUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val createSuccess: Boolean = false, // Flaga do nawigacji wstecz
    val formState: AddUserFormState = AddUserFormState()
)

class AddUserViewModel(
    private val repository: UserManagementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUserUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Waliduje dane i wysyła żądanie utworzenia użytkownika.
     */
    fun createUser() {
        if (_uiState.value.isSaving) return
        val form = _uiState.value.formState

        // --- Prosta walidacja ---
        if (form.username.length < 3) {
            _uiState.update { it.copy(error = "Nazwa użytkownika musi mieć co najmniej 3 znaki.") }
            return
        }
        if (form.password.length < 8) {
            _uiState.update { it.copy(error = "Hasło musi mieć co najmniej 8 znaków.") }
            return
        }
        if (form.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(form.email).matches()) {
            _uiState.update { it.copy(error = "Wprowadź poprawny adres email.") }
            return
        }
        if (form.roles.isEmpty()) {
            _uiState.update { it.copy(error = "Użytkownik musi mieć co najmniej jedną rolę.") }
            return
        }
        // --- Koniec walidacji ---

        _uiState.update { it.copy(isSaving = true, error = null) }

        // Utwórz obiekt żądania
        val request = AdminCreateUserRequest(
            username = form.username,
            email = form.email,
            password = form.password,
            firstName = form.firstName,
            lastName = form.lastName,
            phone = form.phone.ifEmpty { null },
            roles = form.roles,
            active = form.active,
            emailVerified = form.emailVerified
        )

        viewModelScope.launch {
            repository.createUser(request)
                .onSuccess {
                    _uiState.update {
                        it.copy(isSaving = false, createSuccess = true)
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Błąd tworzenia: ${exception.message}"
                        )
                    }
                }
        }
    }

    // --- Funkcje obsługi zmian w formularzu ---

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(formState = it.formState.copy(username = username)) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(formState = it.formState.copy(email = email)) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(formState = it.formState.copy(password = password)) }
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
        val rolesSet = rolesText
            .split(",")
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }
            .toSet()
        _uiState.update { it.copy(formState = it.formState.copy(roles = rolesSet)) }
    }

    /**
     * Resetuje flagę sukcesu po nawigacji.
     */
    fun onCreateSuccessConsumed() {
        _uiState.update { it.copy(createSuccess = false) }
    }
}

/**
 * Factory dla AddUserViewModel.
 */
class AddUserViewModelFactory(
    private val repository: UserManagementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddUserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}