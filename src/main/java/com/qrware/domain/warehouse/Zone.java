package com.qrware.domain.warehouse;

import com.qrware.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

import java.util.ArrayList;
import java.util.List;

/**
 * Zone entity representing logical areas within the warehouse
 */
@Entity
@Table(name = "zones", indexes = {
    @Index(name = "idx_zone_name", columnList = "name"),
    @Index(name = "idx_zone_code", columnList = "code"),
    @Index(name = "idx_zone_type", columnList = "type"),
    @Index(name = "idx_zone_active", columnList = "active")
})
public class Zone extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Zone name is required")
    @Size(max = 100, message = "Zone name must not exceed 100 characters")
    private String name;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Zone code is required")
    @Size(max = 20, message = "Zone code must not exceed 20 characters")
    private String code;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ZoneType type = ZoneType.STORAGE;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

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

    @Column(name = "security_level", nullable = false)
    @Min(value = 1, message = "Security level must be at least 1")
    private Integer securityLevel = 1;

    @Column(name = "hazardous_materials", nullable = false)
    private Boolean hazardousMaterials = false;

    @Column(name = "fragile_items", nullable = false)
    private Boolean fragileItems = false;

    @Column(name = "picking_priority")
    @Min(value = 1, message = "Picking priority must be at least 1")
    private Integer pickingPriority = 5;

    @Column(name = "manager", length = 100)
    @Size(max = 100, message = "Manager name must not exceed 100 characters")
    private String manager;

    @Column(name = "contact_info", length = 200)
    @Size(max = 200, message = "Contact info must not exceed 200 characters")
    private String contactInfo;

    @Column(name = "color", length = 7)
    @Size(max = 7, message = "Color must not exceed 7 characters")
    private String color;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Location> locations = new ArrayList<>();

    // Constructors
    public Zone() {}

    public Zone(String name, String code, ZoneType type) {
        this.name = name;
        this.code = code;
        this.type = type;
    }

    // Business methods
    public boolean hasEnvironmentalControls() {
        return temperatureControlled || humidityControlled;
    }

    public boolean isSpecialHandlingZone() {
        return hazardousMaterials || fragileItems || hasEnvironmentalControls();
    }

    public boolean isHighSecurityZone() {
        return securityLevel >= 3;
    }

    public boolean canAcceptHazardousMaterials() {
        return hazardousMaterials;
    }

    public boolean canAcceptFragileItems() {
        return fragileItems;
    }

    public int getLocationCount() {
        return locations.size();
    }

    public long getActiveLocationCount() {
        return locations.stream()
            .filter(location -> location.getActive())
            .count();
    }

    public long getOccupiedLocationCount() {
        return locations.stream()
            .filter(location -> !location.isEmpty())
            .count();
    }

    public double getOccupancyRate() {
        long activeLocations = getActiveLocationCount();
        if (activeLocations == 0) {
            return 0.0;
        }
        return (double) getOccupiedLocationCount() / activeLocations;
    }

    public List<Location> getAvailableLocations() {
        return locations.stream()
            .filter(location -> location.getActive() && location.getReceivable() && !location.isFull())
            .toList();
    }

    public List<Location> getPickableLocations() {
        return locations.stream()
            .filter(location -> location.getActive() && location.getPickable() && !location.isEmpty())
            .toList();
    }

    public void addLocation(Location location) {
        locations.add(location);
        location.setZone(this);
    }

    public void removeLocation(Location location) {
        locations.remove(location);
        location.setZone(null);
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

    public boolean canBeDeleted() {
        return locations.isEmpty();
    }

    // Getters and Setters
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

    public ZoneType getType() {
        return type;
    }

    public void setType(ZoneType type) {
        this.type = type;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public Integer getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(Integer securityLevel) {
        this.securityLevel = securityLevel;
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

    public Integer getPickingPriority() {
        return pickingPriority;
    }

    public void setPickingPriority(Integer pickingPriority) {
        this.pickingPriority = pickingPriority;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", type=" + type +
                ", active=" + active +
                ", locationCount=" + getLocationCount() +
                '}';
    }
}