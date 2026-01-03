package com.qrware.domain.warehouse;

import com.qrware.domain.common.BaseEntity;
import com.qrware.domain.inventory.InventoryItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations", indexes = {
    @Index(name = "idx_location_code", columnList = "code"),
    @Index(name = "idx_location_zone", columnList = "zone_id"),
    @Index(name = "idx_location_type", columnList = "type"),
    @Index(name = "idx_location_active", columnList = "active"),
    @Index(name = "idx_location_coordinates", columnList = "aisle, rack, shelf, bin")
})
public class Location extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Location code is required")
    @Size(max = 50, message = "Location code must not exceed 50 characters")
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Location name is required")
    @Size(max = 100, message = "Location name must not exceed 100 characters")
    private String name;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private LocationType type = LocationType.SHELF;

    @Column(name = "aisle", length = 10)
    @Size(max = 10, message = "Aisle must not exceed 10 characters")
    private String aisle;

    @Column(name = "rack", length = 10)
    @Size(max = 10, message = "Rack must not exceed 10 characters")
    private String rack;

    @Column(name = "shelf", length = 10)
    @Size(max = 10, message = "Shelf must not exceed 10 characters")
    private String shelf;

    @Column(name = "bin", length = 10)
    @Size(max = 10, message = "Bin must not exceed 10 characters")
    private String bin;

    @Column(name = "capacity_volume", precision = 10, scale = 3)
    @DecimalMin(value = "0.0", message = "Capacity volume must be non-negative")
    private BigDecimal capacityVolume;

    @Column(name = "capacity_weight", precision = 10, scale = 3)
    @DecimalMin(value = "0.0", message = "Capacity weight must be non-negative")
    private BigDecimal capacityWeight;

    @Column(name = "capacity_items")
    @Min(value = 0, message = "Capacity items must be non-negative")
    private Integer capacityItems;

    @Column(name = "temperature_controlled", nullable = false)
    private Boolean temperatureControlled = false;

    @Column(name = "temperature_min")
    private Integer temperatureMin;

    @Column(name = "temperature_max")
    private Integer temperatureMax;

    @Column(name = "humidity_controlled", nullable = false)
    private Boolean humidityControlled = false;

    @Column(name = "humidity_min")
    private Integer humidityMin;

    @Column(name = "humidity_max")
    private Integer humidityMax;

    @Column(name = "hazardous_materials", nullable = false)
    private Boolean hazardousMaterials = false;

    @Column(name = "fragile_items", nullable = false)
    private Boolean fragileItems = false;

    @Column(name = "security_level", nullable = false)
    @Min(value = 1, message = "Security level must be at least 1")
    private Integer securityLevel = 1;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "pickable", nullable = false)
    private Boolean pickable = true;

    @Column(name = "receivable", nullable = false)
    private Boolean receivable = true;

    @Column(name = "qr_code", unique = true, length = 100)
    private String qrCode;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "x_coordinate", precision = 10, scale = 2)
    private BigDecimal xCoordinate;

    @Column(name = "y_coordinate", precision = 10, scale = 2)
    private BigDecimal yCoordinate;

    @Column(name = "z_coordinate", precision = 10, scale = 2)
    private BigDecimal zCoordinate;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    public Location() {}

    public Location(String code, String name, Zone zone, LocationType type) {
        this.code = code;
        this.name = name;
        this.zone = zone;
        this.type = type;
    }

    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (aisle != null) address.append("A").append(aisle);
        if (rack != null) address.append("-R").append(rack);
        if (shelf != null) address.append("-S").append(shelf);
        if (bin != null) address.append("-B").append(bin);
        return address.toString();
    }

    public boolean hasEnvironmentalControls() {
        return temperatureControlled || humidityControlled;
    }

    public boolean isSpecialHandlingLocation() {
        return hazardousMaterials || fragileItems || hasEnvironmentalControls();
    }

    public boolean canAcceptItem(BigDecimal itemVolume, BigDecimal itemWeight) {
        if (capacityVolume != null && itemVolume != null) {
            BigDecimal currentVolume = getCurrentVolume();
            if (currentVolume.add(itemVolume).compareTo(capacityVolume) > 0) {
                return false;
            }
        }
        
        if (capacityWeight != null && itemWeight != null) {
            BigDecimal currentWeight = getCurrentWeight();
            if (currentWeight.add(itemWeight).compareTo(capacityWeight) > 0) {
                return false;
            }
        }
        
        if (capacityItems != null) {
            int currentItems = inventoryItems.size();
            if (currentItems >= capacityItems) {
                return false;
            }
        }
        
        return true;
    }

    public BigDecimal getCurrentVolume() {
        return inventoryItems.stream()
            .map(item -> item.calculateTotalVolume())
            .filter(volume -> volume != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getCurrentWeight() {
        return inventoryItems.stream()
            .map(item -> item.calculateTotalWeight())
            .filter(weight -> weight != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getCurrentItemCount() {
        return inventoryItems.size();
    }

    public BigDecimal getVolumeUtilization() {
        if (capacityVolume == null || capacityVolume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getCurrentVolume().divide(capacityVolume, 4, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getWeightUtilization() {
        if (capacityWeight == null || capacityWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getCurrentWeight().divide(capacityWeight, 4, BigDecimal.ROUND_HALF_UP);
    }

    public boolean isEmpty() {
        return inventoryItems.isEmpty();
    }

    public boolean isFull() {
        if (capacityItems != null && getCurrentItemCount() >= capacityItems) {
            return true;
        }
        if (capacityVolume != null && getCurrentVolume().compareTo(capacityVolume) >= 0) {
            return true;
        }
        if (capacityWeight != null && getCurrentWeight().compareTo(capacityWeight) >= 0) {
            return true;
        }
        return false;
    }

    public boolean isTemperatureInRange(Integer temperature) {
        if (!temperatureControlled || temperature == null) {
            return true;
        }
        boolean minOk = temperatureMin == null || temperature >= temperatureMin;
        boolean maxOk = temperatureMax == null || temperature <= temperatureMax;
        return minOk && maxOk;
    }

    public boolean isHumidityInRange(Integer humidity) {
        if (!humidityControlled || humidity == null) {
            return true;
        }
        boolean minOk = humidityMin == null || humidity >= humidityMin;
        boolean maxOk = humidityMax == null || humidity <= humidityMax;
        return minOk && maxOk;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public String getAisle() {
        return aisle;
    }

    public void setAisle(String aisle) {
        this.aisle = aisle;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public BigDecimal getCapacityVolume() {
        return capacityVolume;
    }

    public void setCapacityVolume(BigDecimal capacityVolume) {
        this.capacityVolume = capacityVolume;
    }

    public BigDecimal getCapacityWeight() {
        return capacityWeight;
    }

    public void setCapacityWeight(BigDecimal capacityWeight) {
        this.capacityWeight = capacityWeight;
    }

    public Integer getCapacityItems() {
        return capacityItems;
    }

    public void setCapacityItems(Integer capacityItems) {
        this.capacityItems = capacityItems;
    }

    public Boolean getTemperatureControlled() {
        return temperatureControlled;
    }

    public void setTemperatureControlled(Boolean temperatureControlled) {
        this.temperatureControlled = temperatureControlled;
    }

    public Integer getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(Integer temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public Integer getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(Integer temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public Boolean getHumidityControlled() {
        return humidityControlled;
    }

    public void setHumidityControlled(Boolean humidityControlled) {
        this.humidityControlled = humidityControlled;
    }

    public Integer getHumidityMin() {
        return humidityMin;
    }

    public void setHumidityMin(Integer humidityMin) {
        this.humidityMin = humidityMin;
    }

    public Integer getHumidityMax() {
        return humidityMax;
    }

    public void setHumidityMax(Integer humidityMax) {
        this.humidityMax = humidityMax;
    }

    public Boolean getHazardousMaterials() {
        return hazardousMaterials;
    }

    public void setHazardousMaterials(Boolean hazardousMaterials) {
        this.hazardousMaterials = hazardousMaterials;
    }

    public Boolean getFragileItems() {
        return fragileItems;
    }

    public void setFragileItems(Boolean fragileItems) {
        this.fragileItems = fragileItems;
    }

    public Integer getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(Integer securityLevel) {
        this.securityLevel = securityLevel;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getPickable() {
        return pickable;
    }

    public void setPickable(Boolean pickable) {
        this.pickable = pickable;
    }

    public Boolean getReceivable() {
        return receivable;
    }

    public void setReceivable(Boolean receivable) {
        this.receivable = receivable;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(BigDecimal xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public BigDecimal getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(BigDecimal yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public BigDecimal getzCoordinate() {
        return zCoordinate;
    }

    public void setzCoordinate(BigDecimal zCoordinate) {
        this.zCoordinate = zCoordinate;
    }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    @Override
    public String toString() {
        return "Location{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", address='" + getFullAddress() + '\'' +
                ", active=" + active +
                '}';
    }
}