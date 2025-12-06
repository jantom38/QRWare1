package com.qrware.dto;

import com.qrware.domain.order.OrderItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderItemDTO {
    private Long id;
    private Integer lineNumber;
    
    // Order information
    private Long orderId;
    private String orderNumber;
    
    // Product information
    private Long productId;
    private String productName;
    private String productSku;
    private String productDescription;
    
    // Inventory information
    private Long inventoryItemId;
    private String inventoryItemCode;
    
    // Location information
    private Long sourceLocationId;
    private String sourceLocationName;
    private String sourceLocationCode;
    private Long destinationLocationId;
    private String destinationLocationName;
    private String destinationLocationCode;
    
    // Quantities
    private Integer requestedQuantity;
    private Integer completedQuantity;
    private Integer remainingQuantity;
    
    // Pricing
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    
    // Status and tracking
    private OrderItemStatus status;
    private String notes;
    private String batchNumber;
    private String serialNumber;
    private String qrCodeData;
    
    // Dates
    private LocalDateTime expiryDate;
    private LocalDateTime pickedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional information
    private String completionNotes;
    
    // Calculated fields
    private Double completionPercentage;
    private Boolean isCompleted;
    private Boolean isPartiallyCompleted;
    private Boolean canBeCompleted;
    private Boolean requiresQRScan;
    private Boolean isQRScanned;
    private Boolean requiresExactInventory;
    private String actualSourceQrCode;
    private String fulfillmentNotes;

    // Constructors
    public OrderItemDTO() {}

    public OrderItemDTO(Long id, Integer lineNumber, String productName, 
                       Integer requestedQuantity, OrderItemStatus status) {
        this.id = id;
        this.lineNumber = lineNumber;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public Long getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public String getInventoryItemCode() { return inventoryItemCode; }
    public void setInventoryItemCode(String inventoryItemCode) { this.inventoryItemCode = inventoryItemCode; }

    public Long getSourceLocationId() { return sourceLocationId; }
    public void setSourceLocationId(Long sourceLocationId) { this.sourceLocationId = sourceLocationId; }

    public String getSourceLocationName() { return sourceLocationName; }
    public void setSourceLocationName(String sourceLocationName) { this.sourceLocationName = sourceLocationName; }

    public String getSourceLocationCode() { return sourceLocationCode; }
    public void setSourceLocationCode(String sourceLocationCode) { this.sourceLocationCode = sourceLocationCode; }

    public Long getDestinationLocationId() { return destinationLocationId; }
    public void setDestinationLocationId(Long destinationLocationId) { this.destinationLocationId = destinationLocationId; }

    public String getDestinationLocationName() { return destinationLocationName; }
    public void setDestinationLocationName(String destinationLocationName) { this.destinationLocationName = destinationLocationName; }

    public String getDestinationLocationCode() { return destinationLocationCode; }
    public void setDestinationLocationCode(String destinationLocationCode) { this.destinationLocationCode = destinationLocationCode; }

    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public Integer getCompletedQuantity() { return completedQuantity; }
    public void setCompletedQuantity(Integer completedQuantity) { this.completedQuantity = completedQuantity; }

    public Integer getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(Integer remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public OrderItemStatus getStatus() { return status; }
    public void setStatus(OrderItemStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public LocalDateTime getPickedAt() { return pickedAt; }
    public void setPickedAt(LocalDateTime pickedAt) { this.pickedAt = pickedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }

    public Double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Boolean getIsPartiallyCompleted() { return isPartiallyCompleted; }
    public void setIsPartiallyCompleted(Boolean isPartiallyCompleted) { this.isPartiallyCompleted = isPartiallyCompleted; }

    public Boolean getCanBeCompleted() { return canBeCompleted; }
    public void setCanBeCompleted(Boolean canBeCompleted) { this.canBeCompleted = canBeCompleted; }

    public Boolean getRequiresQRScan() { return requiresQRScan; }
    public void setRequiresQRScan(Boolean requiresQRScan) { this.requiresQRScan = requiresQRScan; }

    public Boolean getIsQRScanned() { return isQRScanned; }
    public void setIsQRScanned(Boolean isQRScanned) { this.isQRScanned = isQRScanned; }

    public Boolean getRequiresExactInventory() { return requiresExactInventory; }
    public void setRequiresExactInventory(Boolean requiresExactInventory) { this.requiresExactInventory = requiresExactInventory; }

    public String getActualSourceQrCode() { return actualSourceQrCode; }
    public void setActualSourceQrCode(String actualSourceQrCode) { this.actualSourceQrCode = actualSourceQrCode; }

    public String getFulfillmentNotes() { return fulfillmentNotes; }
    public void setFulfillmentNotes(String fulfillmentNotes) { this.fulfillmentNotes = fulfillmentNotes; }

    @Override
    public String toString() {
        return "OrderItemDTO{" +
                "id=" + id +
                ", lineNumber=" + lineNumber +
                ", productName='" + productName + '\'' +
                ", requestedQuantity=" + requestedQuantity +
                ", completedQuantity=" + completedQuantity +
                ", status=" + status +
                '}';
    }
}