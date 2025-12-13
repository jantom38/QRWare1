package com.example.shared.data.repository

import com.example.shared.data.dto.CategoryDTO

import com.example.shared.data.model.ApiResponse
import com.example.shared.data.model.CreateCategoryRequest
import com.example.shared.data.model.UpdateCategoryRequest
import com.example.shared.data.remote.ApiService

class CategoryRepository(
    private val apiService: ApiService
) {
    suspend fun getAllCategories(): ApiResponse<List<CategoryDTO>> =
        apiService.getAllCategories()

    suspend fun getActiveCategories(): ApiResponse<List<CategoryDTO>> =
        apiService.getActiveCategories()

    suspend fun getCategoryById(categoryId: Long): ApiResponse<CategoryDTO> =
        apiService.getCategoryById(categoryId)

    suspend fun getCategoryByCode(code: String): ApiResponse<CategoryDTO> =
        apiService.getCategoryByCode(code)

    suspend fun searchCategories(query: String): ApiResponse<List<CategoryDTO>> =
        apiService.searchCategories(query)

    suspend fun getRootCategories(): ApiResponse<List<CategoryDTO>> =
        apiService.getRootCategories()

    suspend fun getChildCategories(categoryId: Long): ApiResponse<List<CategoryDTO>> =
        apiService.getChildCategories(categoryId)

    suspend fun createCategory(request: CreateCategoryRequest): ApiResponse<CategoryDTO> =
        apiService.createCategory(request)

    suspend fun updateCategory(categoryId: Long, request: UpdateCategoryRequest): ApiResponse<CategoryDTO> =
        apiService.updateCategory(categoryId, request)

    suspend fun deleteCategory(categoryId: Long): ApiResponse<Unit> =
        apiService.deleteCategory(categoryId)

    suspend fun toggleCategoryActive(categoryId: Long): ApiResponse<CategoryDTO> =
        apiService.toggleCategoryActive(categoryId)
}