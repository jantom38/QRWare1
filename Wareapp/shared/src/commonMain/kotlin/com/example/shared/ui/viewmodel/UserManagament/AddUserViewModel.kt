package com.example.shared.ui.viewmodel.UserManagament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.model.AdminCreateUserRequest
import com.example.shared.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddUserFormState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val roles: Set<String> = setOf("USER"),
    val active: Boolean = true,
    val emailVerified: Boolean = false
)

data class AddUserUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val createSuccess: Boolean = false,
    val formState: AddUserFormState = AddUserFormState()
)

class AddUserViewModel(
    private val repository: UserManagementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUserUiState())
    val uiState = _uiState.asStateFlow()

    fun createUser() {
        if (_uiState.value.isSaving) return
        val form = _uiState.value.formState

        if (form.username.length < 3) {
            _uiState.update { it.copy(error = "Nazwa użytkownika musi mieć co najmniej 3 znaki.") }
            return
        }
        if (form.password.length < 8) {
            _uiState.update { it.copy(error = "Hasło musi mieć co najmniej 8 znaków.") }
            return
        }
        if (form.email.isBlank() || !form.email.contains("@")) {
            _uiState.update { it.copy(error = "Wprowadź poprawny adres email.") }
            return
        }
        if (form.roles.isEmpty()) {
            _uiState.update { it.copy(error = "Użytkownik musi mieć co najmniej jedną rolę.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

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

    fun onCreateSuccessConsumed() {
        _uiState.update { it.copy(createSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}