package com.qrware.dto;

public class CategoryDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Boolean active;
    private Integer sortOrder;
    private String icon;
    private String color;
    private Boolean requiresSpecialHandling;
    private Integer storageTemperatureMin;
    private Integer storageTemperatureMax;
    private Integer storageHumidityMin;
    private Integer storageHumidityMax;
    private CategoryDTO parent;
    private Integer level;
    private String fullPath;

    // Constructors
    public CategoryDTO() {}

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
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
    
    public CategoryDTO getParent() { return parent; }
    public void setParent(CategoryDTO parent) { this.parent = parent; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public String getFullPath() { return fullPath; }
    public void setFullPath(String fullPath) { this.fullPath = fullPath; }
}