package com.example.shared.data.repository

import com.example.shared.data.dto.CreateZoneRequest
import com.example.shared.data.dto.UpdateZoneRequest
import com.example.shared.data.dto.ZoneDTO
import com.example.shared.data.model.PaginatedResponse
import com.example.shared.data.remote.ApiService

class ZoneRepository(private val apiService: ApiService) {

    suspend fun getZones(
        page: Int,
        size: Int,
        active: Boolean? = null
    ): PaginatedResponse<ZoneDTO> {
        return apiService.getZones(page = page, size = size, active = active)
    }

    suspend fun getZoneById(id: Long): ZoneDTO {
        return apiService.getZoneById(id)
    }

    suspend fun createZone(request: CreateZoneRequest): ZoneDTO {
        return apiService.createZone(request)
    }

    suspend fun updateZone(id: Long, request: UpdateZoneRequest): ZoneDTO {
        return apiService.updateZone(id, request)
    }

    suspend fun deleteZone(id: Long) {
        apiService.deleteZone(id)
    }

    suspend fun toggleZoneActive(id: Long): ZoneDTO {
        return apiService.toggleZoneActive(id)
    }
}