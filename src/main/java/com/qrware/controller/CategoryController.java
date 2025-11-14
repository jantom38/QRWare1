package com.qrware.controller;

import com.qrware.domain.product.Category;
import com.qrware.dto.CategoryDTO;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.ApiResponse;
import com.qrware.repository.product.CategoryRepository;
import com.qrware.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = {"http://localhost:3000", "http://10.0.2.2:8080"})
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DTOMapper dtoMapper;

    // Pobierz wszystkie kategorie
    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        // ZMIANA: Opakowanie w ApiResponse.success()
        return ResponseEntity.ok(ApiResponse.success(categoryDTOs));
    }

    // Pobierz aktywne kategorie
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getActiveCategories() {
        List<Category> categories = categoryRepository.findByActiveTrue();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        // ZMIANA: Opakowanie w ApiResponse.success()
        return ResponseEntity.ok(ApiResponse.success(categoryDTOs));
    }

    // Pobierz kategorię po ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            // ZMIANA: Opakowanie w ApiResponse.success()
            return ResponseEntity.ok(ApiResponse.success(dtoMapper.toDTO(category.get())));
        }
        throw new ResourceNotFoundException("Category", "id", id);
    }

    // Pobierz kategorię po kodzie
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryByCode(@PathVariable String code) {
        Optional<Category> category = categoryRepository.findByCode(code);
        if (category.isPresent()) {
            // ZMIANA: Opakowanie w ApiResponse.success()
            return ResponseEntity.ok(ApiResponse.success(dtoMapper.toDTO(category.get())));
        }
        throw new ResourceNotFoundException("Category", "code", code);
    }

    // Wyszukaj kategorie po nazwie
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> searchCategories(@RequestParam String query) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(query);
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        // ZMIANA: Opakowanie w ApiResponse.success()
        return ResponseEntity.ok(ApiResponse.success(categoryDTOs));
    }

    // Pobierz kategorie główne (bez rodzica)
    @GetMapping("/root")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getRootCategories() {
        List<Category> categories = categoryRepository.findByParentIsNull();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        // ZMIANA: Opakowanie w ApiResponse.success()
        return ResponseEntity.ok(ApiResponse.success(categoryDTOs));
    }

    // Pobierz podkategorie
    @GetMapping("/{id}/children")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getChildCategories(@PathVariable Long id) {
        List<Category> categories = categoryRepository.findByParentId(id);
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        // ZMIANA: Opakowanie w ApiResponse.success()
        return ResponseEntity.ok(ApiResponse.success(categoryDTOs));
    }

    // Dodaj nową kategorię
    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        // Sprawdź czy kod już istnieje
        if (categoryRepository.existsByCode(request.getCode())) {
            // ZMIANA: Zwróć błąd w formacie ApiResponse
            return ResponseEntity.badRequest().body(ApiResponse.error("Kategoria o tym kodzie już istnieje"));
        }

        Category category = new Category();
        category.setCode(request.getCode());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setActive(request.getActive() != null ? request.getActive() : true);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setRequiresSpecialHandling(request.getRequiresSpecialHandling() != null ? request.getRequiresSpecialHandling() : false);
        category.setStorageTemperatureMin(request.getStorageTemperatureMin());
        category.setStorageTemperatureMax(request.getStorageTemperatureMax());
        category.setStorageHumidityMin(request.getStorageHumidityMin());
        category.setStorageHumidityMax(request.getStorageHumidityMax());

        // Ustaw rodzica jeśli podany
        if (request.getParentId() != null) {
            Optional<Category> parent = categoryRepository.findById(request.getParentId());
            if (parent.isPresent()) {
                category.setParent(parent.get());
            }
        }

        Category savedCategory = categoryRepository.save(category);
        // ZMIANA: Opakowanie w ApiResponse.success()
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dtoMapper.toDTO(savedCategory)));
    }

    // Aktualizuj kategorię
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(@PathVariable Long id,
                                                                   @Valid @RequestBody UpdateCategoryRequest request) {
        Optional<Category> existingCategory = categoryRepository.findById(id);
        if (!existingCategory.isPresent()) {
            throw new ResourceNotFoundException("Category", "id", id);
        }

        Category category = existingCategory.get();

        if (request.getName() != null) category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getActive() != null) category.setActive(request.getActive());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getColor() != null) category.setColor(request.getColor());
        if (request.getRequiresSpecialHandling() != null) category.setRequiresSpecialHandling(request.getRequiresSpecialHandling());
        if (request.getStorageTemperatureMin() != null) category.setStorageTemperatureMin(request.getStorageTemperatureMin());
        if (request.getStorageTemperatureMax() != null) category.setStorageTemperatureMax(request.getStorageTemperatureMax());
        if (request.getStorageHumidityMin() != null) category.setStorageHumidityMin(request.getStorageHumidityMin());
        if (request.getStorageHumidityMax() != null) category.setStorageHumidityMax(request.getStorageHumidityMax());

        // Aktualizuj rodzica jeśli podany
        if (request.getParentId() != null) {
            Optional<Category> parent = categoryRepository.findById(request.getParentId());
            if (parent.isPresent()) {
                category.setParent(parent.get());
            }
        } else if (request.getParentId() == null && request.isRemoveParent()) {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        // ZMIANA: Opakowanie w ApiResponse.success()
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toDTO(updatedCategory)));
    }

    // Usuń kategorię (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    // ZMIANA: Zwraca ApiResponse<Unit> (reprezentowane przez 'null' lub 'Void')
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        Optional<Category> existingCategory = categoryRepository.findById(id);
        if (!existingCategory.isPresent()) {
            throw new ResourceNotFoundException("Category", "id", id);
        }

        Category category = existingCategory.get();
        category.setActive(false); // Soft delete
        categoryRepository.save(category);

        // ZMIANA: Zwróć sukces z pustymi danymi (musi zwrócić obiekt JSON)
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Aktywuj/dezaktywuj kategorię
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    // ZMIANA: Zwraca opakowaną odpowiedź
    public ResponseEntity<ApiResponse<CategoryDTO>> toggleCategoryActive(@PathVariable Long id) {
        Optional<Category> existingCategory = categoryRepository.findById(id);
        if (!existingCategory.isPresent()) {
            throw new ResourceNotFoundException("Category", "id", id);
        }

        Category category = existingCategory.get();
        category.setActive(!category.getActive());
        Category updatedCategory = categoryRepository.save(category);

        // ZMIANA: Opakowanie w ApiResponse.success()
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toDTO(updatedCategory)));
    }

    // DTOs (pozostają bez zmian)
    public static class CreateCategoryRequest {
        private String code;
        private String name;
        private String description;
        private Long parentId;
        private Boolean active;
        private Integer sortOrder;
        private String icon;
        private String color;
        private Boolean requiresSpecialHandling;
        private Integer storageTemperatureMin;
        private Integer storageTemperatureMax;
        private Integer storageHumidityMin;
        private Integer storageHumidityMax;

        // Gettery i settery
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public Boolean getRequiresSpecialHandling() { return requiresSpecialHandling; }
        public void setRequiresSpecialHandling(Boolean requiresSpecialHandling) { this.requiresSpecialHandling = requiresSpecialHandling; }
        public Integer getStorageTemperatureMin() { return storageTemperatureMin; }
        public void setStorageTemperatureMin(Integer storageTemperatureMin) { this.storageTemperatureMin = storageTemperatureMin; }
        public Integer getStorageTemperatureMax() { return storageTemperatureMax; }
        public void setStorageTemperatureMax(Integer storageTemperatureMax) { this.storageTemperatureMax = storageTemperatureMax; }
        public Integer getStorageHumidityMin() { return storageHumidityMin; }
        public void setStorageHumidityMin(Integer storageHumidityMin) { this.storageHumidityMin = storageHumidityMin; }
        public Integer getStorageHumidityMax() { return storageHumidityMax; }
        public void setStorageHumidityMax(Integer storageHumidityMax) { this.storageHumidityMax = storageHumidityMax; }
    }

    public static class UpdateCategoryRequest {
        private String name;
        private String description;
        private Long parentId;
        private Boolean active;
        private Integer sortOrder;
        private boolean removeParent;
        private String icon;
        private String color;
        private Boolean requiresSpecialHandling;
        private Integer storageTemperatureMin;
        private Integer storageTemperatureMax;
        private Integer storageHumidityMin;
        private Integer storageHumidityMax;

        // Gettery i settery
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public boolean isRemoveParent() { return removeParent; }
        public void setRemoveParent(boolean removeParent) { this.removeParent = removeParent; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public Boolean getRequiresSpecialHandling() { return requiresSpecialHandling; }
        public void setRequiresSpecialHandling(Boolean requiresSpecialHandling) { this.requiresSpecialHandling = requiresSpecialHandling; }
        public Integer getStorageTemperatureMin() { return storageTemperatureMin; }
        public void setStorageTemperatureMin(Integer storageTemperatureMin) { this.storageTemperatureMin = storageTemperatureMin; }
        public Integer getStorageTemperatureMax() { return storageTemperatureMax; }
        public void setStorageTemperatureMax(Integer storageTemperatureMax) { this.storageTemperatureMax = storageTemperatureMax; }
        public Integer getStorageHumidityMin() { return storageHumidityMin; }
        public void setStorageHumidityMin(Integer storageHumidityMin) { this.storageHumidityMin = storageHumidityMin; }
        public Integer getStorageHumidityMax() { return storageHumidityMax; }
        public void setStorageHumidityMax(Integer storageHumidityMax) { this.storageHumidityMax = storageHumidityMax; }
    }
}