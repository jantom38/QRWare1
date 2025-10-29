package com.qrware.app.data.repository

import com.qrware.app.data.model.HealthStatus
import com.qrware.app.data.model.SystemStatus
import com.qrware.app.data.remote.HealthService

class HealthRepository(private val healthService: HealthService) {

    suspend fun getHealthStatus(): Result<HealthStatus> {
        return try {
            val response = healthService.getHealth()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get health status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSystemStatus(): Result<SystemStatus> {
        return try {
            val response = healthService.getStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get system status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}