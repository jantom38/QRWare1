package com.qrware.shared.data.storage

import com.qrware.shared.data.model.TokenData

/**
 * Interface for secure token storage across platforms
 */
interface TokenStorage {
    suspend fun saveTokens(tokenData: TokenData)
    suspend fun getTokens(): TokenData?
    suspend fun clearTokens()
    suspend fun hasValidTokens(): Boolean
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
}

/**
 * Default implementation using platform-specific storage
 */
expect class PlatformTokenStorage() : TokenStorage