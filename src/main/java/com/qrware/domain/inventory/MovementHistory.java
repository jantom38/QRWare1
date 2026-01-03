package com.qrware.domain.inventory;

import com.qrware.domain.common.BaseEntity;
import com.qrware.domain.warehouse.Location;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movement_history", indexes = {
    @Index(name = "idx_movement_inventory_item", columnList = "inventory_item_id"),
    @Index(name = "idx_movement_type", columnList = "movement_type"),
    @Index(name = "idx_movement_date", columnList = "movement_date"),
    @Index(name = "idx_movement_from_location", columnList = "from_location_id"),
    @Index(name = "idx_movement_to_location", columnList = "to_location_id"),
    @Index(name = "idx_movement_reference", columnList = "reference_number")
})
public class MovementHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    @NotNull(message = "Inventory item is required")
    private InventoryItem inventoryItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @Column(name = "movement_date", nullable = false)
    @NotNull(message = "Movement date is required")
    private LocalDateTime movementDate;

    @Column(name = "quantity_before")
    private Integer quantityBefore;

    @Column(name = "quantity_after")
    private Integer quantityAfter;

    @Column(name = "quantity_changed", nullable = false)
    private Integer quantityChanged = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id")
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id")
    private Location toLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_before", length = 20)
    private InventoryStatus statusBefore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_after", length = 20)
    private InventoryStatus statusAfter;

    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "reference_number", length = 50)
    @Size(max = 50, message = "Reference number must not exceed 50 characters")
    private String referenceNumber;

    @Column(name = "reference_type", length = 20)
    @Size(max = 20, message = "Reference type must not exceed 20 characters")
    private String referenceType;

    @Column(name = "reason", length = 500)
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "user_id", length = 50)
    @Size(max = 50, message = "User ID must not exceed 50 characters")
    private String userId;

    @Column(name = "user_name", length = 100)
    @Size(max = 100, message = "User name must not exceed 100 characters")
    private String userName;

    @Column(name = "approved", nullable = false)
    private Boolean approved = false;

    @Column(name = "approved_by", length = 50)
    @Size(max = 50, message = "Approved by must not exceed 50 characters")
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "batch_id", length = 50)
    @Size(max = 50, message = "Batch ID must not exceed 50 characters")
    private String batchId;

    @Column(name = "system_generated", nullable = false)
    private Boolean systemGenerated = false;

    @Column(name = "temperature")
    private Integer temperature;

    @Column(name = "humidity")
    private Integer humidity;

    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(name = "volume", precision = 10, scale = 3)
    private BigDecimal volume;

    public MovementHistory() {
        this.movementDate = LocalDateTime.now();
    }

    public MovementHistory(InventoryItem inventoryItem, MovementType movementType, String reason) {
        this();
        this.inventoryItem = inventoryItem;
        this.movementType = movementType;
        this.reason = reason;
    }

    public boolean isQuantityChange() {
        return quantityChanged != null && quantityChanged != 0;
    }

    public boolean isLocationChange() {
        return fromLocation != null && toLocation != null && !fromLocation.equals(toLocation);
    }

    public boolean isStatusChange() {
        return statusBefore != null && statusAfter != null && !statusBefore.equals(statusAfter);
    }

    public boolean isInbound() {
        return movementType.increasesQuantity();
    }

    public boolean isOutbound() {
        return movementType.decreasesQuantity();
    }

    public boolean requiresApproval() {
        return movementType.requiresApproval();
    }

    public void approve(String approverUserId, String approverUserName) {
        if (this.approved) {
            throw new IllegalStateException("Movement is already approved");
        }
        this.approved = true;
        this.approvedBy = approverUserId;
        this.approvedDate = LocalDateTime.now();
    }

    public boolean isApprovalPending() {
        return requiresApproval() && !approved;
    }

    public String getMovementDescription() {
        StringBuilder description = new StringBuilder();
        description.append(movementType.getDisplayName());
        
        if (isQuantityChange()) {
            description.append(" - Quantity: ");
            if (quantityBefore != null) {
                description.append(quantityBefore);
            } else {
                description.append("0");
            }
            description.append(" → ");
            if (quantityAfter != null) {
                description.append(quantityAfter);
            } else {
                description.append("0");
            }
            description.append(" (").append(quantityChanged > 0 ? "+" : "").append(quantityChanged).append(")");
        }
        
        if (isLocationChange()) {
            description.append(" - Location: ");
            description.append(fromLocation.getCode()).append(" → ").append(toLocation.getCode());
        }
        
        if (isStatusChange()) {
            description.append(" - Status: ");
            description.append(statusBefore.getDisplayName()).append(" → ").append(statusAfter.getDisplayName());
        }
        
        return description.toString();
    }

    public String getFormattedMovementDate() {
        return movementDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
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

    public Location getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(Location fromLocation) {
        this.fromLocation = fromLocation;
    }

    public Location getToLocation() {
        return toLocation;
    }

    public void setToLocation(Location toLocation) {
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

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
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

    @Override
    public String toString() {
        return "MovementHistory{" +
                "movementType=" + movementType +
                ", movementDate=" + movementDate +
                ", quantityChanged=" + quantityChanged +
                ", reason='" + reason + '\'' +
                ", approved=" + approved +
                '}';
    }
}