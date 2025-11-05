package com.qrware.app.data.repository

import com.qrware.app.data.dto.CategoryDTO
import com.qrware.app.data.model.*
import com.qrware.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllCategories() = 
        apiService.getAllCategories()

    suspend fun getActiveCategories() = 
        apiService.getActiveCategories()

    suspend fun getCategoryById(categoryId: Long) = 
        apiService.getCategoryById(categoryId)

    suspend fun getCategoryByCode(code: String) = 
        apiService.getCategoryByCode(code)

    suspend fun searchCategories(query: String) = 
        apiService.searchCategories(query)

    suspend fun getRootCategories() = 
        apiService.getRootCategories()

    suspend fun getChildCategories(categoryId: Long) = 
        apiService.getChildCategories(categoryId)

    suspend fun createCategory(request: CreateCategoryRequest) = 
        apiService.createCategory(request)

    suspend fun updateCategory(categoryId: Long, request: UpdateCategoryRequest) = 
        apiService.updateCategory(categoryId, request)

    suspend fun deleteCategory(categoryId: Long) = 
        apiService.deleteCategory(categoryId)

    suspend fun toggleCategoryActive(categoryId: Long) = 
        apiService.toggleCategoryActive(categoryId)
}