package com.qrware.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private String barcode;
    private CategoryDTO category;
    private BigDecimal price;
    private BigDecimal cost;
    private BigDecimal weight;
    private BigDecimal dimensionsLength;
    private BigDecimal dimensionsWidth;
    private BigDecimal dimensionsHeight;
    private String unitOfMeasure;
    private Integer minimumStock;
    private Integer maximumStock;
    private Integer reorderPoint;
    private Boolean active;
    private Boolean perishable;
    private Boolean hazardous;
    private Boolean fragile;
    private String manufacturer;
    private String brand;
    private String model;
    private String version;

    public ProductDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public CategoryDTO getCategory() { return category; }
    public void setCategory(CategoryDTO category) { this.category = category; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    
    public BigDecimal getDimensionsLength() { return dimensionsLength; }
    public void setDimensionsLength(BigDecimal dimensionsLength) { this.dimensionsLength = dimensionsLength; }
    
    public BigDecimal getDimensionsWidth() { return dimensionsWidth; }
    public void setDimensionsWidth(BigDecimal dimensionsWidth) { this.dimensionsWidth = dimensionsWidth; }
    
    public BigDecimal getDimensionsHeight() { return dimensionsHeight; }
    public void setDimensionsHeight(BigDecimal dimensionsHeight) { this.dimensionsHeight = dimensionsHeight; }
    
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    
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
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}