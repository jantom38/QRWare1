package com.qrware.controller;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.product.Product;
import com.qrware.domain.product.Category;
import com.qrware.dto.LowStockReportDTO;
import com.qrware.repository.product.ProductRepository;
import com.qrware.repository.product.CategoryRepository;
import com.qrware.exception.ResourceNotFoundException;


import com.qrware.controller.ProductController.ProductDTO;
import com.qrware.controller.ProductController.CategoryDTO;
import java.util.stream.Collectors;

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
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            Pageable pageable,
            @RequestParam(required = false) Boolean active
    ) {
        Page<Product> productsPage;

        if (active == null) {
            productsPage = productRepository.findAll(pageable);
        } else {
            productsPage = productRepository.findByActive(active, pageable);
        }

        Page<ProductDTO> productsDTOPage = productsPage.map(this::convertToDTO);
        return ResponseEntity.ok(productsDTOPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(convertToDTO(product.get()));
        }
        throw new ResourceNotFoundException("Product", "id", id);
    }

    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        Optional<Product> product = productRepository.findBySku(sku);
        if (product.isPresent()) {
            return ResponseEntity.ok(convertToDTO(product.get()));
        }
        throw new ResourceNotFoundException("Product", "sku", sku);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String query) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(query);
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductDTO>> getActiveProducts() {
        List<Product> products = productRepository.findByActiveTrue();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<LowStockReportDTO>> getLowStockProducts() {
        List<LowStockReportDTO> lowStockProducts = productRepository.findLowStockProducts();
        return ResponseEntity.ok(lowStockProducts);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            return ResponseEntity.badRequest().build();
        }
        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCost(request.getCost());
        product.setUnitOfMeasure(request.getUnit() != null ? request.getUnit() : "PIECE");
        product.setWeight(request.getWeight());
        product.setDimensionsLength(request.getLength());
        product.setDimensionsWidth(request.getWidth());
        product.setDimensionsHeight(request.getHeight());
        product.setMinimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : 0);
        product.setMaximumStock(request.getMaximumStock());
        product.setReorderPoint(request.getReorderPoint());
        product.setActive(request.getActive() != null ? request.getActive() : true);
        product.setPerishable(request.getPerishable() != null ? request.getPerishable() : false);
        product.setHazardous(request.getHazardous() != null ? request.getHazardous() : false);
        product.setFragile(request.getFragile() != null ? request.getFragile() : false);
        product.setManufacturer(request.getManufacturer());
        product.setSupplier(request.getSupplier());
        product.setStorageConditions(request.getStorageConditions());
        product.setBarcode(request.getBarcode());
        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(request.getCategoryId());
            category.ifPresent(product::setCategory);
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedProduct));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateProductRequest request) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (!existingProduct.isPresent()) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        Product product = existingProduct.get();

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCost() != null) product.setCost(request.getCost());
        if (request.getUnit() != null) product.setUnitOfMeasure(request.getUnit());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getLength() != null) product.setDimensionsLength(request.getLength());
        if (request.getWidth() != null) product.setDimensionsWidth(request.getWidth());
        if (request.getHeight() != null) product.setDimensionsHeight(request.getHeight());
        if (request.getMinimumStock() != null) product.setMinimumStock(request.getMinimumStock());
        if (request.getMaximumStock() != null) product.setMaximumStock(request.getMaximumStock());
        if (request.getReorderPoint() != null) product.setReorderPoint(request.getReorderPoint());
        if (request.getActive() != null) product.setActive(request.getActive());
        if (request.getPerishable() != null) product.setPerishable(request.getPerishable());
        if (request.getHazardous() != null) product.setHazardous(request.getHazardous());
        if (request.getFragile() != null) product.setFragile(request.getFragile());
        if (request.getManufacturer() != null) product.setManufacturer(request.getManufacturer());
        if (request.getSupplier() != null) product.setSupplier(request.getSupplier());
        if (request.getStorageConditions() != null) product.setStorageConditions(request.getStorageConditions());
        if (request.getBarcode() != null) product.setBarcode(request.getBarcode());

        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(request.getCategoryId());
            category.ifPresent(product::setCategory);
        }

        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(convertToDTO(updatedProduct));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (!existingProduct.isPresent()) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        Product product = existingProduct.get();
        product.setActive(false);
        productRepository.save(product);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<ProductDTO> toggleProductActive(@PathVariable Long id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (!existingProduct.isPresent()) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        Product product = existingProduct.get();
        product.setActive(!product.getActive());
        Product updatedProduct = productRepository.save(product);

        return ResponseEntity.ok(convertToDTO(updatedProduct));
    }

    private ProductDTO convertToDTO(Product product) {
        if (product == null) {
            return null;
        }

        CategoryDTO categoryDTO = null;
        if (product.getCategory() != null) {
            try {
                categoryDTO = new CategoryDTO(
                        product.getCategory().getId(),
                        product.getCategory().getName()
                );
            } catch (Exception e) {
                categoryDTO = null;
            }
        }

        return new ProductDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCost(),
                product.getWeight(),
                product.getDimensionsLength(),
                product.getDimensionsWidth(),
                product.getDimensionsHeight(),
                product.getUnitOfMeasure(),
                product.getMinimumStock(),
                product.getMaximumStock(),
                product.getReorderPoint(),
                product.getActive(),
                product.getPerishable(),
                product.getHazardous(),
                product.getFragile(),
                product.getManufacturer(),
                product.getSupplier(),
                product.getStorageConditions(),
                product.getBarcode(),
                categoryDTO
        );
    }

    public record CategoryDTO(
            Long id,
            String name
    ) {
    }

    public record ProductDTO(
            Long id,
            String sku,
            String name,
            String description,
            BigDecimal price,
            BigDecimal cost,
            BigDecimal weight,
            BigDecimal dimensionsLength,
            BigDecimal dimensionsWidth,
            BigDecimal dimensionsHeight,
            String unitOfMeasure,
            Integer minimumStock,
            Integer maximumStock,
            Integer reorderPoint,
            Boolean active,
            Boolean perishable,
            Boolean hazardous,
            Boolean fragile,
            String manufacturer,
            String supplier,
            String storageConditions,
            String barcode,
            CategoryDTO category
    ) {
    }

    public static class CreateProductRequest {
        private String sku;
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal cost;
        private String unit;
        private BigDecimal weight;
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private Integer minimumStock;
        private Integer maximumStock;
        private Integer reorderPoint;
        private Boolean active;
        private Boolean perishable;
        private Boolean hazardous;
        private Boolean fragile;
        private String manufacturer;
        private String supplier;
        private String storageConditions;
        private String barcode;
        private Long categoryId;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getCost() { return cost; }
        public void setCost(BigDecimal cost) { this.cost = cost; }
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
        public Integer getMinimumStock() { return minimumStock; }
        public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
        public Integer getMaximumStock() { return maximumStock; }
        public void setMaximumStock(Integer maximumStock) { this.maximumStock = maximumStock; }
        public Integer getReorderPoint() { return reorderPoint; }
        public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public Boolean getPerishable() { return perishable; }
        public void setPerishable(Boolean perishable) { this.perishable = perishable; }
        public Boolean getHazardous() { return hazardous; }
        public void setHazardous(Boolean hazardous) { this.hazardous = hazardous; }
        public Boolean getFragile() { return fragile; }
        public void setFragile(Boolean fragile) { this.fragile = fragile; }
        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
        public String getSupplier() { return supplier; }
        public void setSupplier(String supplier) { this.supplier = supplier; }
        public String getStorageConditions() { return storageConditions; }
        public void setStorageConditions(String storageConditions) { this.storageConditions = storageConditions; }
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    }

    public static class UpdateProductRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal cost;
        private String unit;
        private BigDecimal weight;
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private Integer minimumStock;
        private Integer maximumStock;
        private Integer reorderPoint;
        private Boolean active;
        private Boolean perishable;
        private Boolean hazardous;
        private Boolean fragile;
        private String manufacturer;
        private String supplier;
        private String storageConditions;
        private String barcode;
        private Long categoryId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getCost() { return cost; }
        public void setCost(BigDecimal cost) { this.cost = cost; }
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
        public Integer getMinimumStock() { return minimumStock; }
        public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
        public Integer getMaximumStock() { return maximumStock; }
        public void setMaximumStock(Integer maximumStock) { this.maximumStock = maximumStock; }
        public Integer getReorderPoint() { return reorderPoint; }
        public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public Boolean getPerishable() { return perishable; }
        public void setPerishable(Boolean perishable) { this.perishable = perishable; }
        public Boolean getHazardous() { return hazardous; }
        public void setHazardous(Boolean hazardous) { this.hazardous = hazardous; }
        public Boolean getFragile() { return fragile; }
        public void setFragile(Boolean fragile) { this.fragile = fragile; }
        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
        public String getSupplier() { return supplier; }
        public void setSupplier(String supplier) { this.supplier = supplier; }
        public String getStorageConditions() { return storageConditions; }
        public void setStorageConditions(String storageConditions) { this.storageConditions = storageConditions; }
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    }
}