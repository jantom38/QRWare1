package com.qrware.domain.inventory;

public enum InventoryStatus {
    
    AVAILABLE("Available", "Item is available for use"),
    UNAVAILABLE("Unavailable", "Item is unavailable for general use"),
    RESERVED("Reserved", "Item is reserved for orders"),
    ON_HOLD("On Hold", "Item is temporarily unavailable"),
    QUARANTINE("Quarantine", "Item is in quarantine for quality inspection"),
    DAMAGED("Damaged", "Item is damaged and cannot be used"),
    EXPIRED("Expired", "Item has passed its expiry date"),
    RECALLED("Recalled", "Item has been recalled by manufacturer"),
    IN_TRANSIT("In Transit", "Item is being moved between locations"),
    PICKED("Picked", "Item has been picked for an order"),
    SHIPPED("Shipped", "Item has been shipped out"),
    RETURNED("Returned", "Item has been returned by customer"),
    DISPOSED("Disposed", "Item has been disposed of"),
    LOST("Lost", "Item cannot be located"),
    COUNTED("Counted", "Item has been counted during cycle count"),
    ALLOCATED("Allocated", "Item is allocated to a specific purpose"),
    BACKORDERED("Backordered", "Item is backordered from supplier"),
    RECEIVING("Receiving", "Item is in the process of being received"),
    INSPECTING("Inspecting", "Item is being inspected for quality"),
    STAGING("Staging", "Item is staged for shipping or other operations");

    private final String displayName;
    private final String description;

    InventoryStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailableForPicking() {
        return this == AVAILABLE || this == ALLOCATED;
    }

    public boolean isUnavailable() {
        return this == UNAVAILABLE || this == ON_HOLD || this == QUARANTINE || this == DAMAGED || 
               this == EXPIRED || this == RECALLED || this == DISPOSED || this == LOST;
    }

    public boolean isInProcess() {
        return this == IN_TRANSIT || this == PICKED || this == RECEIVING || 
               this == INSPECTING || this == STAGING;
    }

    public boolean isCompleted() {
        return this == SHIPPED || this == DISPOSED;
    }

    public boolean requiresAction() {
        return this == QUARANTINE || this == DAMAGED || this == EXPIRED || 
               this == RECALLED || this == RETURNED || this == LOST;
    }

    public boolean canBeReserved() {
        return this == AVAILABLE;
    }

    public boolean canBeMoved() {
        return this == AVAILABLE || this == RESERVED || this == ALLOCATED || 
               this == ON_HOLD || this == COUNTED || this == UNAVAILABLE;
    }

    public boolean canBeAdjusted() {
        return this != SHIPPED && this != DISPOSED;
    }

    public boolean isTemporary() {
        return this == RESERVED || this == IN_TRANSIT || this == PICKED || 
               this == RECEIVING || this == INSPECTING || this == STAGING;
    }

    @Override
    public String toString() {
        return displayName;
    }
}