package com.qrware.domain.warehouse;

public enum LocationType {
    
    SHELF("Shelf", "Standard shelf storage"),
    RACK("Rack", "Rack storage system"),
    FLOOR("Floor", "Floor storage area"),
    PALLET("Pallet", "Pallet storage location"),
    BIN("Bin", "Small bin storage"),
    CAGE("Cage", "Security cage storage"),
    COLD_STORAGE("Cold Storage", "Refrigerated storage area"),
    FREEZER("Freezer", "Frozen storage area"),
    HAZMAT("Hazmat", "Hazardous materials storage"),
    RECEIVING("Receiving", "Receiving dock area"),
    SHIPPING("Shipping", "Shipping dock area"),
    STAGING("Staging", "Staging area for operations"),
    QUARANTINE("Quarantine", "Quarantine area for inspection"),
    DAMAGED("Damaged", "Area for damaged goods"),
    RETURNS("Returns", "Returns processing area"),
    PICKING("Picking", "Order picking area"),
    PACKING("Packing", "Order packing area"),
    CROSSDOCK("Cross Dock", "Cross docking area"),
    BULK("Bulk", "Bulk storage area"),
    OVERFLOW("Overflow", "Overflow storage area"),
    MAINTENANCE("Maintenance", "Maintenance area"),
    OFFICE("Office", "Office space"),
    VIRTUAL("Virtual", "Virtual location for tracking");

    private final String displayName;
    private final String description;

    LocationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isStorageType() {
        return this == SHELF || this == RACK || this == FLOOR || this == PALLET || 
               this == BIN || this == CAGE || this == BULK || this == OVERFLOW;
    }

    public boolean isSpecialHandling() {
        return this == COLD_STORAGE || this == FREEZER || this == HAZMAT || 
               this == QUARANTINE || this == DAMAGED;
    }

    public boolean isOperational() {
        return this == RECEIVING || this == SHIPPING || this == STAGING || 
               this == PICKING || this == PACKING || this == CROSSDOCK;
    }

    public boolean isTemperatureControlled() {
        return this == COLD_STORAGE || this == FREEZER;
    }

    public boolean requiresSecurityAccess() {
        return this == CAGE || this == HAZMAT || this == QUARANTINE;
    }

    public boolean isPhysicalLocation() {
        return this != VIRTUAL;
    }

    @Override
    public String toString() {
        return displayName;
    }
}