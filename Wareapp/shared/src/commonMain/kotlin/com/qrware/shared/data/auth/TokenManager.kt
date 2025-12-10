package com.qrware.shared.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple Token Manager dla przechowywania aktualnego tokena
 * W przyszłości można rozszerzyć o secure storage
 */
class TokenManager {
    
    private var _currentToken: String? = null
    private val _tokenState = MutableStateFlow<String?>(null)
    val tokenState: StateFlow<String?> = _tokenState.asStateFlow()
    
    /**
     * Zapisz token po udanym login
     */
    fun saveToken(accessToken: String) {
        _currentToken = accessToken
        _tokenState.value = accessToken
    }
    
    /**
     * Pobierz aktualny token
     */
    suspend fun getToken(): String? {
        return _currentToken
    }
    
    /**
     * Wyczyść token przy logout
     */
    fun clearToken() {
        _currentToken = null
        _tokenState.value = null
    }
    
    /**
     * Sprawdź czy mamy token
     */
    fun hasToken(): Boolean {
        return !_currentToken.isNullOrBlank()
    }
}