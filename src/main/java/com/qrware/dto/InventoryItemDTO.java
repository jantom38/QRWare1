package com.qrware.dto;

import com.qrware.domain.inventory.InventoryStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryItemDTO {
    private Long id;
    private ProductDTO product;
    private LocationDTO location;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private InventoryStatus status;
    private String qrCode;
    private String lotNumber;
    private String batchNumber;
    private String serialNumber;
    private LocalDate receivedDate;
    private LocalDate expiryDate;
    private LocalDate manufactureDate;
    private java.time.LocalDateTime lastCountedDate;
    private java.time.LocalDateTime lastMovedDate;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private String supplierReference;
    private String manufacturer;
    private String purchaseOrderNumber;
    private String notes;
    private Integer temperature;
    private Integer humidity;
    private Integer conditionRating;
    private Boolean quarantine;
    private String quarantineReason;
    private Boolean hold;
    private String holdReason;

    public InventoryItemDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ProductDTO getProduct() { return product; }
    public void setProduct(ProductDTO product) { this.product = product; }
    
    public LocationDTO getLocation() { return location; }
    public void setLocation(LocationDTO location) { this.location = location; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    
    public InventoryStatus getStatus() { return status; }
    public void setStatus(InventoryStatus status) { this.status = status; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
    
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public LocalDate getManufactureDate() { return manufactureDate; }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
    
    public java.time.LocalDateTime getLastCountedDate() { return lastCountedDate; }
    public void setLastCountedDate(java.time.LocalDateTime lastCountedDate) { this.lastCountedDate = lastCountedDate; }
    
    public java.time.LocalDateTime getLastMovedDate() { return lastMovedDate; }
    public void setLastMovedDate(java.time.LocalDateTime lastMovedDate) { this.lastMovedDate = lastMovedDate; }
    
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    
    public String getSupplierReference() { return supplierReference; }
    public void setSupplierReference(String supplierReference) { this.supplierReference = supplierReference; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getPurchaseOrderNumber() { return purchaseOrderNumber; }
    public void setPurchaseOrderNumber(String purchaseOrderNumber) { this.purchaseOrderNumber = purchaseOrderNumber; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Integer getTemperature() { return temperature; }
    public void setTemperature(Integer temperature) { this.temperature = temperature; }
    
    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }
    
    public Integer getConditionRating() { return conditionRating; }
    public void setConditionRating(Integer conditionRating) { this.conditionRating = conditionRating; }
    
    public Boolean getQuarantine() { return quarantine; }
    public void setQuarantine(Boolean quarantine) { this.quarantine = quarantine; }
    
    public String getQuarantineReason() { return quarantineReason; }
    public void setQuarantineReason(String quarantineReason) { this.quarantineReason = quarantineReason; }
    
    public Boolean getHold() { return hold; }
    public void setHold(Boolean hold) { this.hold = hold; }
    
    public String getHoldReason() { return holdReason; }
    public void setHoldReason(String holdReason) { this.holdReason = holdReason; }
}