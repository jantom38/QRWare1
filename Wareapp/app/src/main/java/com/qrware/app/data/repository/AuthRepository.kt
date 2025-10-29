package com.qrware.app.data.repository

import com.qrware.app.data.model.*
import com.qrware.app.data.remote.AuthService

class AuthRepository(private val authService: AuthService) {

    suspend fun login(request: LoginRequest): Result<AuthenticationResponse> {
        return try {
            val response = authService.login(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Login failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun register(request: RegisterRequest): Result<AuthenticationResponse> {
        return try {
            val response = authService.register(request)
            if (response.isSuccessful && response.body()?.data != null) {
                // Rejestracja się powiodła, zwracamy dane odpowiedzi
                Result.success(response.body()!!.data!!)
            } else {
                // Błąd serwera lub walidacji (np. użytkownik już istnieje)
                Result.failure(Exception("Rejestracja nieudana: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            // Błąd sieciowy
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserInfoResponse> {
        return try {
            val response = authService.getCurrentUser()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to get user info: ${response.code()}"))
            }
        } catch(e: Exception) {
            Result.failure(e)
        }
    }
}