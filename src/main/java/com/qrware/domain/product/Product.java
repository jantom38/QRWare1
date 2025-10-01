package com.qrware.domain.product;

import com.qrware.domain.common.BaseEntity;
import com.qrware.domain.inventory.InventoryItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product entity representing items in the warehouse
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku"),
    @Index(name = "idx_product_barcode", columnList = "barcode"),
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_active", columnList = "active")
})
public class Product extends BaseEntity {

    @Column(name = "sku", unique = true, nullable = false, length = 50)
    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "barcode", length = 50)
    @Size(max = 50, message = "Barcode must not exceed 50 characters")
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    private Category category;

    @Column(name = "price", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Column(name = "cost", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than 0")
    private BigDecimal cost;

    @Column(name = "weight", precision = 8, scale = 3)
    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    private BigDecimal weight;

    @Column(name = "dimensions_length", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Length must be non-negative")
    private BigDecimal dimensionsLength;

    @Column(name = "dimensions_width", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Width must be non-negative")
    private BigDecimal dimensionsWidth;

    @Column(name = "dimensions_height", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Height must be non-negative")
    private BigDecimal dimensionsHeight;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    @NotBlank(message = "Unit of measure is required")
    @Size(max = 20, message = "Unit of measure must not exceed 20 characters")
    private String unitOfMeasure = "PIECE";

    @Column(name = "minimum_stock", nullable = false)
    @Min(value = 0, message = "Minimum stock must be non-negative")
    private Integer minimumStock = 0;

    @Column(name = "maximum_stock")
    @Min(value = 0, message = "Maximum stock must be non-negative")
    private Integer maximumStock;

    @Column(name = "reorder_point")
    @Min(value = 0, message = "Reorder point must be non-negative")
    private Integer reorderPoint;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "perishable", nullable = false)
    private Boolean perishable = false;

    @Column(name = "hazardous", nullable = false)
    private Boolean hazardous = false;

    @Column(name = "fragile", nullable = false)
    private Boolean fragile = false;

    @Column(name = "manufacturer", length = 100)
    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    private String manufacturer;

    @Column(name = "supplier", length = 100)
    @Size(max = 100, message = "Supplier must not exceed 100 characters")
    private String supplier;

    @Column(name = "storage_conditions", length = 500)
    @Size(max = 500, message = "Storage conditions must not exceed 500 characters")
    private String storageConditions;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    // Constructors
    public Product() {}

    public Product(String sku, String name, Category category, String unitOfMeasure) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
    }

    // Business methods
    public boolean requiresSpecialHandling() {
        return perishable || hazardous || fragile;
    }

    public boolean isLowStock(int currentStock) {
        return reorderPoint != null && currentStock <= reorderPoint;
    }

    public boolean isOverStock(int currentStock) {
        return maximumStock != null && currentStock > maximumStock;
    }

    public BigDecimal calculateVolume() {
        if (dimensionsLength != null && dimensionsWidth != null && dimensionsHeight != null) {
            return dimensionsLength.multiply(dimensionsWidth).multiply(dimensionsHeight);
        }
        return null;
    }

    public BigDecimal calculateMargin() {
        if (price != null && cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
            return price.subtract(cost).divide(cost, 4, BigDecimal.ROUND_HALF_UP);
        }
        return null;
    }

    // Getters and Setters
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getDimensionsLength() {
        return dimensionsLength;
    }

    public void setDimensionsLength(BigDecimal dimensionsLength) {
        this.dimensionsLength = dimensionsLength;
    }

    public BigDecimal getDimensionsWidth() {
        return dimensionsWidth;
    }

    public void setDimensionsWidth(BigDecimal dimensionsWidth) {
        this.dimensionsWidth = dimensionsWidth;
    }

    public BigDecimal getDimensionsHeight() {
        return dimensionsHeight;
    }

    public void setDimensionsHeight(BigDecimal dimensionsHeight) {
        this.dimensionsHeight = dimensionsHeight;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public Integer getMaximumStock() {
        return maximumStock;
    }

    public void setMaximumStock(Integer maximumStock) {
        this.maximumStock = maximumStock;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getPerishable() {
        return perishable;
    }

    public void setPerishable(Boolean perishable) {
        this.perishable = perishable;
    }

    public Boolean getHazardous() {
        return hazardous;
    }

    public void setHazardous(Boolean hazardous) {
        this.hazardous = hazardous;
    }

    public Boolean getFragile() {
        return fragile;
    }

    public void setFragile(Boolean fragile) {
        this.fragile = fragile;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    @Override
    public String toString() {
        return "Product{" +
                "sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category=" + (category != null ? category.getName() : null) +
                ", active=" + active +
                '}';
    }
}