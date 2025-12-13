package com.example.shared.data.repository

import com.example.shared.data.dto.*
import com.example.shared.data.model.PaginatedResponse
import com.example.shared.data.remote.ApiService

class LocationRepository(private val apiService: ApiService) {

    suspend fun getActiveZones(page: Int, size: Int): PaginatedResponse<ZoneDTO> {
        return apiService.getZones(page = page, size = size, active = true)
    }

    suspend fun getLocations(page: Int, size: Int, active: Boolean?): PaginatedResponse<LocationDTO> {
        return apiService.getAllLocations(page = page, size = size, active = active)
    }

    suspend fun searchLocations(query: String, page: Int, size: Int, active: Boolean?): PaginatedResponse<LocationDTO> {
        return apiService.searchLocations(query = query, page = page, size = size, active = active)
    }

    suspend fun getLocationById(id: Long): LocationDTO {
        return apiService.getLocationById(id)
    }

    suspend fun createLocation(request: CreateLocationRequest): LocationDTO {
        return apiService.createLocation(request)
    }

    suspend fun updateLocation(id: Long, request: UpdateLocationRequest): LocationDTO {
        return apiService.updateLocation(id, request)
    }

    suspend fun deleteLocation(id: Long) {
        apiService.deleteLocation(id)
    }

    suspend fun toggleLocationActive(id: Long): LocationDTO {
        return apiService.toggleLocationActive(id)
    }
}