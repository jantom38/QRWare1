package com.example.shared.ui.viewmodel.ProductsManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.dto.CategoryDTO
import com.example.shared.data.model.*
import com.example.shared.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryDTO?>(null)
    val selectedCategory: StateFlow<CategoryDTO?> = _selectedCategory.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = categoryRepository.getAllCategories()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categories = response.data ?: emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania kategorii"
                )
            }
        }
    }

    fun loadActiveCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = categoryRepository.getActiveCategories()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categories = response.data ?: emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania kategorii"
                )
            }
        }
    }

    fun loadRootCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = categoryRepository.getRootCategories()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categories = response.data ?: emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania kategorii głównych"
                )
            }
        }
    }

    fun loadChildCategories(parentId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = categoryRepository.getChildCategories(parentId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    childCategories = response.data ?: emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas ładowania podkategorii"
                )
            }
        }
    }

    fun searchCategories(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = categoryRepository.searchCategories(query)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categories = response.data ?: emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd podczas wyszukiwania"
                )
            }
        }
    }

    fun createCategory(request: CreateCategoryRequest) {
        viewModelScope.launch {
            try {
                categoryRepository.createCategory(request)
                loadCategories()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Kategoria została utworzona pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas tworzenia kategorii"
                )
            }
        }
    }

    fun updateCategory(categoryId: Long, request: UpdateCategoryRequest) {
        viewModelScope.launch {
            try {
                categoryRepository.updateCategory(categoryId, request)
                loadCategories()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Kategoria została zaktualizowana pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas aktualizacji kategorii"
                )
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(categoryId)
                loadCategories()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Kategoria została usunięta pomyślnie"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas usuwania kategorii"
                )
            }
        }
    }

    fun toggleCategoryActive(categoryId: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.toggleCategoryActive(categoryId)
                loadCategories()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Status kategorii został zmieniony"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Błąd podczas zmiany statusu kategorii"
                )
            }
        }
    }

    fun selectCategory(category: CategoryDTO) {
        _selectedCategory.value = category
    }

    fun clearSelection() {
        _selectedCategory.value = null
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}

data class CategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryDTO> = emptyList(),
    val childCategories: List<CategoryDTO> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)