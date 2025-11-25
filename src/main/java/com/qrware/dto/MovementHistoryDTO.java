package com.qrware.dto;

import com.qrware.domain.inventory.InventoryStatus;

import java.math.BigDecimal;

/**
 * DTO for MovementHistory entity
 */
public class MovementHistoryDTO {
    private Long id;
    private InventoryItemDTO inventoryItem;
    private String movementType;
    private String movementDate;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private Integer quantityChanged;
    private LocationDTO fromLocation;
    private LocationDTO toLocation;
    private InventoryStatus statusBefore;
    private InventoryStatus statusAfter;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private String referenceNumber;
    private String referenceType;
    private String reason;
    private String notes;
    private String userId;
    private String userName;
    private Boolean approved;
    private String approvedBy;
    private String approvedDate;
    private String batchId;
    private Boolean systemGenerated;
    private Integer temperature;
    private Integer humidity;
    private BigDecimal weight;
    private BigDecimal volume;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public MovementHistoryDTO() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InventoryItemDTO getInventoryItem() {
        return inventoryItem;
    }

    public void setInventoryItem(InventoryItemDTO inventoryItem) {
        this.inventoryItem = inventoryItem;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public String getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(String movementDate) {
        this.movementDate = movementDate;
    }

    public Integer getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(Integer quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public Integer getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(Integer quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public Integer getQuantityChanged() {
        return quantityChanged;
    }

    public void setQuantityChanged(Integer quantityChanged) {
        this.quantityChanged = quantityChanged;
    }

    public LocationDTO getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(LocationDTO fromLocation) {
        this.fromLocation = fromLocation;
    }

    public LocationDTO getToLocation() {
        return toLocation;
    }

    public void setToLocation(LocationDTO toLocation) {
        this.toLocation = toLocation;
    }

    public InventoryStatus getStatusBefore() {
        return statusBefore;
    }

    public void setStatusBefore(InventoryStatus statusBefore) {
        this.statusBefore = statusBefore;
    }

    public InventoryStatus getStatusAfter() {
        return statusAfter;
    }

    public void setStatusAfter(InventoryStatus statusAfter) {
        this.statusAfter = statusAfter;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Boolean getSystemGenerated() {
        return systemGenerated;
    }

    public void setSystemGenerated(Boolean systemGenerated) {
        this.systemGenerated = systemGenerated;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "MovementHistoryDTO{" +
                "id=" + id +
                ", movementType='" + movementType + '\'' +
                ", movementDate='" + movementDate + '\'' +
                ", quantityChanged=" + quantityChanged +
                ", reason='" + reason + '\'' +
                ", approved=" + approved +
                '}';
    }
}