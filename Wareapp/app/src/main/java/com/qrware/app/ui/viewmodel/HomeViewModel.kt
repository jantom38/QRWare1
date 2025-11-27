package com.qrware.app.ui.viewmodel

import androidx.lifecycle.*
import com.qrware.app.data.model.UserInfoResponse
import com.qrware.app.data.repository.AuthRepository
// import com.qrware.app.data.repository.TestRepository // Usunięte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository
    // private val testRepository: TestRepository // Usunięte
) : ViewModel() {

    private val _userState = MutableStateFlow<UserInfoResponse?>(null)
    val userState: StateFlow<UserInfoResponse?> = _userState.asStateFlow()

    // Usunięte stany _publicData i _protectedData

    fun fetchData() {
        viewModelScope.launch {
            // Fetch User Info
            authRepository.getCurrentUser()
                .onSuccess { _userState.value = it }
                .onFailure { _userState.value = null } // Handle error appropriately

            // Usunięte wywołania testRepository
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

// Factory dla ViewModel
class HomeViewModelFactory(
    private val authRepository: AuthRepository
    // private val testRepository: TestRepository // Usunięte
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(authRepository) as T // Zaktualizowane
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}