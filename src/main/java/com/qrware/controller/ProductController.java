package com.qrware.controller;

import com.qrware.domain.product.Product;
import com.qrware.domain.product.Category;
import com.qrware.repository.product.ProductRepository;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://10.0.2.2:8080"})
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Pobierz wszystkie produkty z paginacją
    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return ResponseEntity.ok(products);
    }

    // Pobierz produkt po ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        throw new ResourceNotFoundException("Product", "id", id);
    }

    // Pobierz produkt po SKU
    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        Optional<Product> product = productRepository.findBySku(sku);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        throw new ResourceNotFoundException("Product", "sku", sku);
    }

    // Pobierz produkty po kategorii
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    // Wyszukaj produkty po nazwie
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String query) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(query);
        return ResponseEntity.ok(products);
    }

    // Pobierz aktywne produkty
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<Product>> getActiveProducts() {
        List<Product> products = productRepository.findByActiveTrue();
        return ResponseEntity.ok(products);
    }

    // Dodaj nowy produkt
    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        // Sprawdź czy SKU już istnieje
        if (productRepository.existsBySku(request.getSku())) {
            return ResponseEntity.badRequest().build();
        }

        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUnitOfMeasure(request.getUnit());
        product.setWeight(request.getWeight());
        product.setDimensionsLength(request.getLength());
        product.setDimensionsWidth(request.getWidth());
        product.setDimensionsHeight(request.getHeight());
        product.setActive(request.getActive() != null ? request.getActive() : true);

        // Ustaw kategorię jeśli podana
        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(request.getCategoryId());
            if (category.isPresent()) {
                product.setCategory(category.get());
            }
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // Aktualizuj produkt
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, 
                                               @Valid @RequestBody UpdateProductRequest request) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (!existingProduct.isPresent()) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        Product product = existingProduct.get();
        
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getUnit() != null) product.setUnitOfMeasure(request.getUnit());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getLength() != null) product.setDimensionsLength(request.getLength());
        if (request.getWidth() != null) product.setDimensionsWidth(request.getWidth());
        if (request.getHeight() != null) product.setDimensionsHeight(request.getHeight());
        if (request.getActive() != null) product.setActive(request.getActive());

        // Aktualizuj kategorię jeśli podana
        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(request.getCategoryId());
            if (category.isPresent()) {
                product.setCategory(category.get());
            }
        }

        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    // Usuń produkt (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (!existingProduct.isPresent()) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        Product product = existingProduct.get();
        product.setActive(false); // Soft delete
        productRepository.save(product);
        
        return ResponseEntity.noContent().build();
    }

    // Aktywuj/dezaktywuj produkt
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<Product> toggleProductActive(@PathVariable Long id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (!existingProduct.isPresent()) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        Product product = existingProduct.get();
        product.setActive(!product.getActive());
        Product updatedProduct = productRepository.save(product);
        
        return ResponseEntity.ok(updatedProduct);
    }

    // TODO: Dodać endpoint /low-stock gdy zostanie zaimplementowana metoda w repository

    // DTOs
    public static class CreateProductRequest {
        private String sku;
        private String name;
        private String description;
        private BigDecimal price;
        private String unit;
        private BigDecimal weight;
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private Long categoryId;
        private Boolean active;

        // Gettery i settery
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        public BigDecimal getLength() { return length; }
        public void setLength(BigDecimal length) { this.length = length; }
        public BigDecimal getWidth() { return width; }
        public void setWidth(BigDecimal width) { this.width = width; }
        public BigDecimal getHeight() { return height; }
        public void setHeight(BigDecimal height) { this.height = height; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public static class UpdateProductRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private String unit;
        private BigDecimal weight;
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private Long categoryId;
        private Boolean active;

        // Gettery i settery
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        public BigDecimal getLength() { return length; }
        public void setLength(BigDecimal length) { this.length = length; }
        public BigDecimal getWidth() { return width; }
        public void setWidth(BigDecimal width) { this.width = width; }
        public BigDecimal getHeight() { return height; }
        public void setHeight(BigDecimal height) { this.height = height; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
}