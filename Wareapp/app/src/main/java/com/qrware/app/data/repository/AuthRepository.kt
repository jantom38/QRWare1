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
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Rejestracja nieudana: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
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

    suspend fun logout(): Result<Unit> {
        return try {
            val response = authService.logout()

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Wylogowanie nieudane: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}