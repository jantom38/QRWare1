package com.example.shared.security

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class TokenManager(private val settings: Settings) {

    companion object {
        private const val TOKEN_KEY = "jwt_token"
    }

    /**
     * Pobiera token synchronicznie (potrzebne dla NetworkModule).
     */
    fun getAccessToken(): String? {
        return settings.getStringOrNull(TOKEN_KEY)
    }

    /**
     * Zapisuje token.
     */
    fun saveToken(token: String) {
        settings[TOKEN_KEY] = token
    }

    /**
     * Usuwa token (wylogowanie).
     */
    fun clearToken() {
        settings.remove(TOKEN_KEY)
    }

    // Jeśli potrzebujesz Flow (jak w oryginale), wymaga to biblioteki 'multiplatform-settings-coroutines'
    // fun getTokenFlow(): Flow<String?> = settings.getStringOrNullFlow(TOKEN_KEY)
}