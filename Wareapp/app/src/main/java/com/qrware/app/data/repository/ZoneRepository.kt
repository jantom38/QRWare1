package com.qrware.app.data.repository

import com.qrware.app.data.dto.ZoneDTO
import com.qrware.app.data.model.PaginatedResponse
import com.qrware.app.data.remote.ApiService
import com.qrware.app.data.dto.CreateZoneRequest
import com.qrware.app.data.dto.UpdateZoneRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ZoneRepository(private val apiService: ApiService) {

    suspend fun getZones(
        page: Int,
        size: Int,
        active: Boolean? = null
    ): PaginatedResponse<ZoneDTO> {
        return withContext(Dispatchers.IO) {
            apiService.getZones(page = page, size = size, active = active)
        }
    }

    suspend fun getZoneById(id: Long): ZoneDTO {
        return withContext(Dispatchers.IO) {
            apiService.getZoneById(id)
        }
    }

    suspend fun createZone(request: CreateZoneRequest): ZoneDTO {
        return withContext(Dispatchers.IO) {
            apiService.createZone(request)
        }
    }

    suspend fun updateZone(id: Long, request: UpdateZoneRequest): ZoneDTO {
        return withContext(Dispatchers.IO) {
            apiService.updateZone(id, request)
        }
    }

    suspend fun deleteZone(id: Long) {
        withContext(Dispatchers.IO) {
            val response = apiService.deleteZone(id)
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
        }
    }

    suspend fun toggleZoneActive(id: Long): ZoneDTO {
        return withContext(Dispatchers.IO) {
            apiService.toggleZoneActive(id)
        }
    }
}