package com.qrware.dto;

import com.qrware.domain.warehouse.LocationType;
import java.math.BigDecimal;

public class LocationDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private ZoneDTO zone;
    private Long zoneId; // Added zoneId field
    private LocationType type;
    private String aisle;
    private String rack;
    private String shelf;
    private String bin;
    private BigDecimal capacityVolume;
    private BigDecimal capacityWeight;
    private Integer capacityItems;
    private Integer currentItems; // Added currentItems field
    private Boolean temperatureControlled;
    private Integer temperatureMin;
    private Integer temperatureMax;
    private Boolean humidityControlled;
    private Integer humidityMin;
    private Integer humidityMax;
    private Boolean hazardousMaterials;
    private Boolean fragileItems;
    private Integer securityLevel;
    private Boolean active;
    private Boolean pickable;
    private Boolean receivable;
    private String qrCode;
    private String barcode;
    private BigDecimal xCoordinate;
    private BigDecimal yCoordinate;
    private BigDecimal zCoordinate;

    public LocationDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ZoneDTO getZone() { return zone; }
    public void setZone(ZoneDTO zone) { 
        this.zone = zone; 
        if (zone != null) {
            this.zoneId = zone.id();
        }
    }
    
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    
    public LocationType getType() { return type; }
    public void setType(LocationType type) { this.type = type; }
    
    public String getAisle() { return aisle; }
    public void setAisle(String aisle) { this.aisle = aisle; }
    
    public String getRack() { return rack; }
    public void setRack(String rack) { this.rack = rack; }
    
    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }
    
    public String getBin() { return bin; }
    public void setBin(String bin) { this.bin = bin; }
    
    public BigDecimal getCapacityVolume() { return capacityVolume; }
    public void setCapacityVolume(BigDecimal capacityVolume) { this.capacityVolume = capacityVolume; }
    
    public BigDecimal getCapacityWeight() { return capacityWeight; }
    public void setCapacityWeight(BigDecimal capacityWeight) { this.capacityWeight = capacityWeight; }
    
    public Integer getCapacityItems() { return capacityItems; }
    public void setCapacityItems(Integer capacityItems) { this.capacityItems = capacityItems; }
    
    public Integer getCurrentItems() { return currentItems; }
    public void setCurrentItems(Integer currentItems) { this.currentItems = currentItems; }
    
    public Boolean getTemperatureControlled() { return temperatureControlled; }
    public void setTemperatureControlled(Boolean temperatureControlled) { this.temperatureControlled = temperatureControlled; }
    
    public Integer getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(Integer temperatureMin) { this.temperatureMin = temperatureMin; }
    
    public Integer getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(Integer temperatureMax) { this.temperatureMax = temperatureMax; }
    
    public Boolean getHumidityControlled() { return humidityControlled; }
    public void setHumidityControlled(Boolean humidityControlled) { this.humidityControlled = humidityControlled; }
    
    public Integer getHumidityMin() { return humidityMin; }
    public void setHumidityMin(Integer humidityMin) { this.humidityMin = humidityMin; }
    
    public Integer getHumidityMax() { return humidityMax; }
    public void setHumidityMax(Integer humidityMax) { this.humidityMax = humidityMax; }
    
    public Boolean getHazardousMaterials() { return hazardousMaterials; }
    public void setHazardousMaterials(Boolean hazardousMaterials) { this.hazardousMaterials = hazardousMaterials; }
    
    public Boolean getFragileItems() { return fragileItems; }
    public void setFragileItems(Boolean fragileItems) { this.fragileItems = fragileItems; }
    
    public Integer getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(Integer securityLevel) { this.securityLevel = securityLevel; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Boolean getPickable() { return pickable; }
    public void setPickable(Boolean pickable) { this.pickable = pickable; }
    
    public Boolean getReceivable() { return receivable; }
    public void setReceivable(Boolean receivable) { this.receivable = receivable; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public BigDecimal getXCoordinate() { return xCoordinate; }
    public void setXCoordinate(BigDecimal xCoordinate) { this.xCoordinate = xCoordinate; }
    
    public BigDecimal getYCoordinate() { return yCoordinate; }
    public void setYCoordinate(BigDecimal yCoordinate) { this.yCoordinate = yCoordinate; }
    
    public BigDecimal getZCoordinate() { return zCoordinate; }
    public void setZCoordinate(BigDecimal zCoordinate) { this.zCoordinate = zCoordinate; }
}