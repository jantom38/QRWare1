package com.qrware.dto;

import java.math.BigDecimal;

public class LocationDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private ZoneDTO zone;
    private String aisle;
    private String rack;
    private String shelf;
    private String position;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;
    private String barcode;
    private BigDecimal xCoordinate;
    private BigDecimal yCoordinate;
    private BigDecimal zCoordinate;

    // Constructors
    public LocationDTO() {}

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ZoneDTO getZone() { return zone; }
    public void setZone(ZoneDTO zone) { this.zone = zone; }
    
    public String getAisle() { return aisle; }
    public void setAisle(String aisle) { this.aisle = aisle; }
    
    public String getRack() { return rack; }
    public void setRack(String rack) { this.rack = rack; }
    
    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public BigDecimal getMaxWeight() { return maxWeight; }
    public void setMaxWeight(BigDecimal maxWeight) { this.maxWeight = maxWeight; }
    
    public BigDecimal getMaxVolume() { return maxVolume; }
    public void setMaxVolume(BigDecimal maxVolume) { this.maxVolume = maxVolume; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public BigDecimal getXCoordinate() { return xCoordinate; }
    public void setXCoordinate(BigDecimal xCoordinate) { this.xCoordinate = xCoordinate; }
    
    public BigDecimal getYCoordinate() { return yCoordinate; }
    public void setYCoordinate(BigDecimal yCoordinate) { this.yCoordinate = yCoordinate; }
    
    public BigDecimal getZCoordinate() { return zCoordinate; }
    public void setZCoordinate(BigDecimal zCoordinate) { this.zCoordinate = zCoordinate; }
}