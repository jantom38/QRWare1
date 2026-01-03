package com.qrware.domain.product;

import com.qrware.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_category_code", columnList = "code"),
    @Index(name = "idx_category_parent", columnList = "parent_id"),
    @Index(name = "idx_category_active", columnList = "active")
})
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Category code is required")
    @Size(max = 20, message = "Category code must not exceed 20 characters")
    private String code;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "icon", length = 50)
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;

    @Column(name = "color", length = 7)
    @Size(max = 7, message = "Color must not exceed 7 characters")
    private String color;

    @Column(name = "requires_special_handling", nullable = false)
    private Boolean requiresSpecialHandling = false;

    @Column(name = "storage_temperature_min")
    private Integer storageTemperatureMin;

    @Column(name = "storage_temperature_max")
    private Integer storageTemperatureMax;

    @Column(name = "storage_humidity_min")
    private Integer storageHumidityMin;

    @Column(name = "storage_humidity_max")
    private Integer storageHumidityMax;

    public Category() {}

    public Category(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Category(String name, String code, Category parent) {
        this.name = name;
        this.code = code;
        this.parent = parent;
    }

    public boolean isRootCategory() {
        return parent == null;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean hasProducts() {
        return !products.isEmpty();
    }

    public int getLevel() {
        int level = 0;
        Category current = this.parent;
        while (current != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    public List<Category> getAncestors() {
        List<Category> ancestors = new ArrayList<>();
        Category current = this.parent;
        while (current != null) {
            ancestors.add(0, current);
            current = current.getParent();
        }
        return ancestors;
    }

    public List<Category> getAllDescendants() {
        List<Category> descendants = new ArrayList<>();
        for (Category child : children) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }

    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Category child) {
        children.remove(child);
        child.setParent(null);
    }

    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setCategory(null);
    }

    public boolean canBeDeleted() {
        return !hasChildren() && !hasProducts();
    }

    public boolean hasStorageRequirements() {
        return storageTemperatureMin != null || storageTemperatureMax != null ||
               storageHumidityMin != null || storageHumidityMax != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getRequiresSpecialHandling() {
        return requiresSpecialHandling;
    }

    public void setRequiresSpecialHandling(Boolean requiresSpecialHandling) {
        this.requiresSpecialHandling = requiresSpecialHandling;
    }

    public Integer getStorageTemperatureMin() {
        return storageTemperatureMin;
    }

    public void setStorageTemperatureMin(Integer storageTemperatureMin) {
        this.storageTemperatureMin = storageTemperatureMin;
    }

    public Integer getStorageTemperatureMax() {
        return storageTemperatureMax;
    }

    public void setStorageTemperatureMax(Integer storageTemperatureMax) {
        this.storageTemperatureMax = storageTemperatureMax;
    }

    public Integer getStorageHumidityMin() {
        return storageHumidityMin;
    }

    public void setStorageHumidityMin(Integer storageHumidityMin) {
        this.storageHumidityMin = storageHumidityMin;
    }

    public Integer getStorageHumidityMax() {
        return storageHumidityMax;
    }

    public void setStorageHumidityMax(Integer storageHumidityMax) {
        this.storageHumidityMax = storageHumidityMax;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", level=" + getLevel() +
                ", active=" + active +
                '}';
    }
}