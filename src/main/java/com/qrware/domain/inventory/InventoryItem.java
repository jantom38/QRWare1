package com.qrware.domain.inventory;

import com.qrware.domain.common.BaseEntity;
import com.qrware.domain.product.Product;
import com.qrware.domain.warehouse.Location;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_items", indexes = {
    @Index(name = "idx_inventory_product", columnList = "product_id"),
    @Index(name = "idx_inventory_location", columnList = "location_id"),
    @Index(name = "idx_inventory_status", columnList = "status"),
    @Index(name = "idx_inventory_qr_code", columnList = "qr_code"),
    @Index(name = "idx_inventory_lot_number", columnList = "lot_number"),
    @Index(name = "idx_inventory_expiry_date", columnList = "expiry_date")
})
public class InventoryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @NotNull(message = "Location is required")
    private Location location;

    @Column(name = "quantity", nullable = false)
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    @Min(value = 0, message = "Reserved quantity must be non-negative")
    private Integer reservedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    @Min(value = 0, message = "Available quantity must be non-negative")
    private Integer availableQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    @Column(name = "qr_code", unique = true, nullable = false, length = 100)
    @NotBlank(message = "QR code is required")
    @Size(max = 100, message = "QR code must not exceed 100 characters")
    private String qrCode;

    @Column(name = "lot_number", length = 50)
    @Size(max = 50, message = "Lot number must not exceed 50 characters")
    private String lotNumber;

    @Column(name = "batch_number", length = 50)
    @Size(max = 50, message = "Batch number must not exceed 50 characters")
    private String batchNumber;

    @Column(name = "serial_number", length = 50)
    @Size(max = 50, message = "Serial number must not exceed 50 characters")
    private String serialNumber;

    @Column(name = "received_date", nullable = false)
    @NotNull(message = "Received date is required")
    private LocalDate receivedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "last_counted_date")
    private LocalDateTime lastCountedDate;

    @Column(name = "last_moved_date")
    private LocalDateTime lastMovedDate;

    @Column(name = "unit_cost", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Unit cost must be non-negative")
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Total cost must be non-negative")
    private BigDecimal totalCost;

    @Column(name = "supplier_reference", length = 100)
    @Size(max = 100, message = "Supplier reference must not exceed 100 characters")
    private String supplierReference;

    @Column(name = "purchase_order_number", length = 50)
    @Size(max = 50, message = "Purchase order number must not exceed 50 characters")
    private String purchaseOrderNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "temperature")
    private Integer temperature;

    @Column(name = "humidity")
    private Integer humidity;

    @Column(name = "condition_rating")
    @Min(value = 1, message = "Condition rating must be at least 1")
    @Max(value = 10, message = "Condition rating must be at most 10")
    private Integer conditionRating = 10;

    @Column(name = "quarantine", nullable = false)
    private Boolean quarantine = false;

    @Column(name = "quarantine_reason", length = 500)
    @Size(max = 500, message = "Quarantine reason must not exceed 500 characters")
    private String quarantineReason;

    @Column(name = "hold", nullable = false)
    private Boolean hold = false;

    @Column(name = "hold_reason", length = 500)
    @Size(max = 500, message = "Hold reason must not exceed 500 characters")
    private String holdReason;

    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovementHistory> movementHistory = new ArrayList<>();

    public InventoryItem() {}

    public InventoryItem(Product product, Location location, Integer quantity, String qrCode) {
        this.product = product;
        this.location = location;
        this.quantity = quantity;
        this.availableQuantity = quantity;
        this.qrCode = qrCode;
        this.receivedDate = LocalDate.now();
    }

    @PrePersist
    @PreUpdate
    private void onPersistOrUpdate() {
        calculateAvailableQuantity();
        calculateTotalCost();
    }

    private void calculateAvailableQuantity() {
        if (quantity != null && reservedQuantity != null) {
            this.availableQuantity = quantity - reservedQuantity;
        }
    }

    private void calculateTotalCost() {
        if (quantity != null && unitCost != null) {
            this.totalCost = unitCost.multiply(new BigDecimal(quantity));
        }
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isAvailable() {
        return status == InventoryStatus.AVAILABLE && !quarantine && !hold && availableQuantity > 0;
    }

    public boolean canReserve(int requestedQuantity) {
        return isAvailable() && availableQuantity >= requestedQuantity;
    }

    public void reserve(int quantityToReserve) {
        if (!canReserve(quantityToReserve)) {
            throw new IllegalStateException("Cannot reserve " + quantityToReserve + " items. Available: " + availableQuantity);
        }
        this.reservedQuantity += quantityToReserve;
    }

    public void unreserve(int quantityToUnreserve) {
        if (reservedQuantity < quantityToUnreserve) {
            throw new IllegalStateException("Cannot unreserve " + quantityToUnreserve + " items. Reserved: " + reservedQuantity);
        }
        this.reservedQuantity -= quantityToUnreserve;
    }

    public void fulfillReservationAndDecreaseStock(int quantityToFulfill) {
        if (this.reservedQuantity < quantityToFulfill) {
            throw new IllegalStateException("Cannot fulfill " + quantityToFulfill + " items. Only " + this.reservedQuantity + " are reserved.");
        }
        if (this.quantity < quantityToFulfill) {
            throw new IllegalStateException("Cannot fulfill " + quantityToFulfill + " items. Only " + this.quantity + " exist in total (data integrity issue).");
        }
        this.quantity -= quantityToFulfill;
        this.reservedQuantity -= quantityToFulfill;
    }

    public void adjustQuantity(int newQuantity, String reason) {
        int oldQuantity = this.quantity;
        this.quantity = newQuantity;
        
        MovementHistory movement = new MovementHistory();
        movement.setInventoryItem(this);
        movement.setMovementType(MovementType.ADJUSTMENT);
        movement.setQuantityBefore(oldQuantity);
        movement.setQuantityAfter(newQuantity);
        movement.setQuantityChanged(newQuantity - oldQuantity);
        movement.setReason(reason);
        movement.setMovementDate(LocalDateTime.now());
        
        this.movementHistory.add(movement);
    }

    public void move(Location newLocation, String reason) {
        Location oldLocation = this.location;
        this.location = newLocation;
        this.lastMovedDate = LocalDateTime.now();
        
        MovementHistory movement = new MovementHistory();
        movement.setInventoryItem(this);
        movement.setMovementType(MovementType.MOVE);
        movement.setFromLocation(oldLocation);
        movement.setToLocation(newLocation);
        movement.setQuantityChanged(0);
        movement.setReason(reason);
        movement.setMovementDate(LocalDateTime.now());
        
        this.movementHistory.add(movement);
    }

    public BigDecimal calculateTotalVolume() {
        if (product != null && quantity != null) {
            BigDecimal itemVolume = product.calculateVolume();
            if (itemVolume != null) {
                return itemVolume.multiply(new BigDecimal(quantity));
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalWeight() {
        if (product != null && product.getWeight() != null && quantity != null) {
            return product.getWeight().multiply(new BigDecimal(quantity));
        }
        return BigDecimal.ZERO;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
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
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public List<MovementHistory> getMovementHistory() { return movementHistory; }
    public void setMovementHistory(List<MovementHistory> movementHistory) { this.movementHistory = movementHistory; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
    public LocalDateTime getLastCountedDate() { return lastCountedDate; }
    public void setLastCountedDate(LocalDateTime lastCountedDate) { this.lastCountedDate = lastCountedDate; }
    public LocalDateTime getLastMovedDate() { return lastMovedDate; }
    public void setLastMovedDate(LocalDateTime lastMovedDate) { this.lastMovedDate = lastMovedDate; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public String getSupplierReference() { return supplierReference; }
    public void setSupplierReference(String supplierReference) { this.supplierReference = supplierReference; }
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