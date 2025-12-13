package com.example.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.model.UserInfoResponse
import com.example.shared.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() { // ZMIANA: Dziedziczenie po ViewModel

    // Usunięto ręcznie tworzony scope - używamy viewModelScope

    private val _userState = MutableStateFlow<UserInfoResponse?>(null)
    val userState: StateFlow<UserInfoResponse?> = _userState.asStateFlow()

    fun fetchData() {
        // Teraz viewModelScope jest dostępny dzięki dziedziczeniu
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { _userState.value = it }
                .onFailure { _userState.value = null }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _userState.value = null
                }
        }
    }
}