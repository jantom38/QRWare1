package com.example.shared.data.repository

import com.example.shared.data.model.HealthStatus
import com.example.shared.data.model.SystemStatus
import com.example.shared.data.remote.HealthService

class HealthRepository(private val healthService: HealthService) {

    suspend fun getHealthStatus(): Result<HealthStatus> {
        return try {
            val response = healthService.getHealth()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSystemStatus(): Result<SystemStatus> {
        return try {
            val response = healthService.getStatus()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}