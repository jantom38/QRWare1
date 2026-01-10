package com.qrware.app.data.repository

import com.qrware.app.data.remote.TestService
import com.google.gson.Gson

class TestRepository(private val testService: TestService) {

    private val gson = Gson()

    private suspend fun fetchAndFormatException(
        apiCall: suspend () -> retrofit2.Response<Map<String, Any>>
    ): Result<String> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                Result.success(gson.toJson(response.body()))
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPublicData(): Result<String> {
        return fetchAndFormatException { testService.getPublicEndpoint() }
    }
    suspend fun getProtectedData(): Result<String> {
        return fetchAndFormatException { testService.getProtectedEndpoint() }
    }

    suspend fun getAdminData(): Result<String> {
        return fetchAndFormatException { testService.getAdminEndpoint() }
    }
}