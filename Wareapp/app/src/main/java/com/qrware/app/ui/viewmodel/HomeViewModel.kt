package com.qrware.app.ui.viewmodel

import androidx.lifecycle.*
import com.qrware.app.data.model.UserInfoResponse
import com.qrware.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserInfoResponse?>(null)
    val userState: StateFlow<UserInfoResponse?> = _userState.asStateFlow()

    fun fetchData() {
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

class HomeViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}