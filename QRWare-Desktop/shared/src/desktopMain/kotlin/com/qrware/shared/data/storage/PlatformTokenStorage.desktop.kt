package com.qrware.shared.data.storage

import com.qrware.shared.data.model.TokenData
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual class PlatformTokenStorage : TokenStorage {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val storageDir = getStorageDirectory()
    private val tokenFile = File(storageDir, "tokens.enc")
    private val keyFile = File(storageDir, "key.enc")
    
    init {
        // Ensure storage directory exists
        storageDir.mkdirs()
    }
    
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveTokens(tokenData: TokenData) {
        try {
            val jsonString = json.encodeToString(tokenData)
            val encryptedData = encryptData(jsonString)
            
            Files.write(
                tokenFile.toPath(),
                encryptedData,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        } catch (e: Exception) {
            throw StorageException("Failed to save tokens", e)
        }
    }
    
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun getTokens(): TokenData? {
        return try {
            if (!tokenFile.exists()) return null
            
            val encryptedData = Files.readAllBytes(tokenFile.toPath())
            val decryptedJson = decryptData(encryptedData)
            json.decodeFromString<TokenData>(decryptedJson)
        } catch (e: Exception) {
            // If decryption fails, clear corrupted data
            clearTokens()
            null
        }
    }
    
    override suspend fun clearTokens() {
        try {
            tokenFile.delete()
            keyFile.delete()
        } catch (e: Exception) {
            // Ignore deletion errors
        }
    }
    
    override suspend fun hasValidTokens(): Boolean {
        val tokens = getTokens()
        return tokens != null && !tokens.isExpired()
    }
    
    override suspend fun getAccessToken(): String? {
        val tokens = getTokens()
        return if (tokens != null && !tokens.isExpired()) {
            tokens.accessToken
        } else {
            null
        }
    }
    
    override suspend fun getRefreshToken(): String? {
        return getTokens()?.refreshToken
    }
    
    @OptIn(ExperimentalEncodingApi::class)
    private fun encryptData(data: String): ByteArray {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        
        // Generate random IV
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        // Combine IV + encrypted data
        return iv + encryptedData
    }
    
    @OptIn(ExperimentalEncodingApi::class)
    private fun decryptData(encryptedData: ByteArray): String {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        
        // Extract IV (first 16 bytes) and encrypted data
        val iv = encryptedData.sliceArray(0..15)
        val encrypted = encryptedData.sliceArray(16 until encryptedData.size)
        
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        
        val decryptedBytes = cipher.doFinal(encrypted)
        return String(decryptedBytes)
    }
    
    @OptIn(ExperimentalEncodingApi::class)
    private fun getOrCreateSecretKey(): SecretKey {
        if (keyFile.exists()) {
            try {
                val keyBytes = Files.readAllBytes(keyFile.toPath())
                val decodedKey = Base64.decode(keyBytes)
                return SecretKeySpec(decodedKey, "AES")
            } catch (e: Exception) {
                // If key is corrupted, create new one
            }
        }
        
        // Generate new key
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()
        
        // Save key to file
        val encodedKey = Base64.encode(secretKey.encoded)
        Files.write(
            keyFile.toPath(),
            encodedKey,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        
        return secretKey
    }
    
    private fun getStorageDirectory(): File {
        val userHome = System.getProperty("user.home")
        val appDataDir = when {
            System.getProperty("os.name").lowercase().contains("windows") -> {
                File(System.getenv("APPDATA") ?: "$userHome\\AppData\\Roaming", "QRWare")
            }
            System.getProperty("os.name").lowercase().contains("mac") -> {
                File("$userHome/Library/Application Support/QRWare")
            }
            else -> {
                File("$userHome/.qrware")
            }
        }
        return appDataDir
    }
}

class StorageException(message: String, cause: Throwable? = null) : Exception(message, cause)