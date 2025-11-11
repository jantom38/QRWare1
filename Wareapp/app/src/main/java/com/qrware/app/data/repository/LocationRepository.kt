package com.qrware.app.data.repository

import com.qrware.app.data.dto.CreateLocationRequest
import com.qrware.app.data.dto.LocationDTO
import com.qrware.app.data.dto.UpdateLocationRequest
import com.qrware.app.data.dto.ZoneDTO
import com.qrware.app.data.model.PaginatedResponse
import com.qrware.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import retrofit2.HttpException

class LocationRepository(private val apiService: ApiService) {

    suspend fun getActiveZones(
        page: Int,
        size: Int
    ): PaginatedResponse<ZoneDTO> {
        return withContext(Dispatchers.IO) {
                 apiService.getZones(page = page, size= size)
             }

    }

    suspend fun getLocations(
        page: Int,
        size: Int,
        active: Boolean?
    ): PaginatedResponse<LocationDTO> {
        return withContext(Dispatchers.IO) {
            apiService.getAllLocations(page = page, size = size, active = active)
        }
    }

    suspend fun getLocationById(id: Long): LocationDTO {
        return withContext(Dispatchers.IO) {
            apiService.getLocationById(id)
        }
    }

    suspend fun createLocation(request: CreateLocationRequest): LocationDTO {
        return withContext(Dispatchers.IO) {
            apiService.createLocation(request)
        }
    }

    suspend fun updateLocation(id: Long, request: UpdateLocationRequest): LocationDTO {
        return withContext(Dispatchers.IO) {
            apiService.updateLocation(id, request)
        }
    }

    suspend fun deleteLocation(id: Long) {
        withContext(Dispatchers.IO) {
            val response = apiService.deleteLocation(id)
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
        }
    }

    suspend fun toggleLocationActive(id: Long): LocationDTO {
        return withContext(Dispatchers.IO) {
            apiService.toggleLocationActive(id)
        }
    }
}