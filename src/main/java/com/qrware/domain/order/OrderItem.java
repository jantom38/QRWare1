package com.qrware.domain.order;

import com.qrware.domain.common.BaseEntity;
import com.qrware.domain.product.Product;
import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.warehouse.Location;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_product", columnList = "product_id"),
    @Index(name = "idx_order_item_inventory", columnList = "inventory_item_id"),
    @Index(name = "idx_order_item_status", columnList = "status"),
    @Index(name = "idx_order_item_line_number", columnList = "order_id,lineNumber")
})
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull
    private Order order;

    @Column(name = "line_number", nullable = false)
    @NotNull
    @Min(1)
    private Integer lineNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id")
    private Location sourceLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_location_id")
    private Location destinationLocation;

    @Column(name = "requested_quantity", nullable = false)
    @NotNull
    @Min(0)
    private Integer requestedQuantity;

    @Column(name = "completed_quantity", nullable = false)
    @Min(0)
    private Integer completedQuantity = 0;

    @Column(name = "unit_price", precision = 15, scale = 4)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    private OrderItemStatus status = OrderItemStatus.PENDING;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

    @Size(max = 200)
    @Column(name = "batch_number", length = 200)
    private String batchNumber;

    @Size(max = 200)
    @Column(name = "serial_number", length = 200)
    private String serialNumber;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "picked_at")
    private LocalDateTime pickedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Size(max = 500)
    @Column(name = "completion_notes", length = 500)
    private String completionNotes;

    @Size(max = 200)
    @Column(name = "qr_code_data", length = 200)
    private String qrCodeData;

    @Column(name = "requires_exact_inventory", nullable = false)
    private Boolean requiresExactInventory = true;

    @Size(max = 200)
    @Column(name = "actual_source_qr", length = 200)
    private String actualSourceQrCode;

    @Size(max = 500)
    @Column(name = "fulfillment_notes", length = 500)
    private String fulfillmentNotes;

    public OrderItem() {}

    public OrderItem(Order order, Integer lineNumber, Product product, Integer requestedQuantity) {
        this.order = order;
        this.lineNumber = lineNumber;
        this.product = product;
        this.requestedQuantity = requestedQuantity;
        this.status = OrderItemStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == OrderItemStatus.COMPLETED;
    }

    public boolean isPartiallyCompleted() {
        return completedQuantity > 0 && completedQuantity < requestedQuantity;
    }

    public boolean canBeCompleted() {
        return status == OrderItemStatus.PENDING || status == OrderItemStatus.IN_PROGRESS;
    }

    public void complete(Integer quantity, String notes) {
        if (!canBeCompleted()) {
            throw new IllegalStateException("Order item cannot be completed in current status: " + status);
        }
        
        if (quantity > requestedQuantity) {
            throw new IllegalArgumentException("Completed quantity cannot exceed requested quantity");
        }

        this.completedQuantity = quantity;
        this.completionNotes = notes;
        this.completedAt = LocalDateTime.now();
        
        if (quantity.equals(requestedQuantity)) {
            this.status = OrderItemStatus.COMPLETED;
        } else {
            this.status = OrderItemStatus.PARTIALLY_COMPLETED;
        }
    }

    public void pick() {
        if (status != OrderItemStatus.PENDING) {
            throw new IllegalStateException("Order item cannot be picked in current status: " + status);
        }
        this.status = OrderItemStatus.IN_PROGRESS;
        this.pickedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (status == OrderItemStatus.COMPLETED || status == OrderItemStatus.CANCELLED) {
            throw new IllegalStateException("Order item cannot be cancelled in current status: " + status);
        }
        this.status = OrderItemStatus.CANCELLED;
        this.completionNotes = reason;
    }

    public Integer getRemainingQuantity() {
        return requestedQuantity - completedQuantity;
    }

    public BigDecimal getTotalValue() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(completedQuantity));
    }

    public double getCompletionPercentage() {
        if (requestedQuantity == 0) {
            return 0.0;
        }
        return (double) completedQuantity / requestedQuantity * 100.0;
    }

    public boolean requiresQRScan() {
        return order.getType().requiresQRScan();
    }

    public void setQRCodeData(String qrData) {
        this.qrCodeData = qrData;
    }

    public boolean isQRScanned() {
        return qrCodeData != null && !qrCodeData.trim().isEmpty();
    }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public InventoryItem getInventoryItem() { return inventoryItem; }
    public void setInventoryItem(InventoryItem inventoryItem) { this.inventoryItem = inventoryItem; }

    public Location getSourceLocation() { return sourceLocation; }
    public void setSourceLocation(Location sourceLocation) { this.sourceLocation = sourceLocation; }

    public Location getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(Location destinationLocation) { this.destinationLocation = destinationLocation; }

    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public Integer getCompletedQuantity() { return completedQuantity; }
    public void setCompletedQuantity(Integer completedQuantity) { this.completedQuantity = completedQuantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public OrderItemStatus getStatus() { return status; }
    public void setStatus(OrderItemStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public LocalDateTime getPickedAt() { return pickedAt; }
    public void setPickedAt(LocalDateTime pickedAt) { this.pickedAt = pickedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }

    public Boolean getRequiresExactInventory() { return requiresExactInventory; }
    public void setRequiresExactInventory(Boolean requiresExactInventory) { this.requiresExactInventory = requiresExactInventory; }

    public String getActualSourceQrCode() { return actualSourceQrCode; }
    public void setActualSourceQrCode(String actualSourceQrCode) { this.actualSourceQrCode = actualSourceQrCode; }

    public String getFulfillmentNotes() { return fulfillmentNotes; }
    public void setFulfillmentNotes(String fulfillmentNotes) { this.fulfillmentNotes = fulfillmentNotes; }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + getId() +
                ", lineNumber=" + lineNumber +
                ", product=" + (product != null ? product.getName() : "null") +
                ", requestedQuantity=" + requestedQuantity +
                ", completedQuantity=" + completedQuantity +
                ", status=" + status +
                '}';
    }
}