package com.qrware.domain.warehouse;

public enum ZoneType {
    
    STORAGE("Storage", "General storage zone"),
    RECEIVING("Receiving", "Incoming goods receiving zone"),
    SHIPPING("Shipping", "Outgoing goods shipping zone"),
    PICKING("Picking", "Order picking zone"),
    PACKING("Packing", "Order packing zone"),
    STAGING("Staging", "Staging area for operations"),
    CROSSDOCK("Cross Dock", "Cross docking operations zone"),
    QUARANTINE("Quarantine", "Quality inspection and quarantine zone"),
    COLD_STORAGE("Cold Storage", "Refrigerated storage zone"),
    FREEZER("Freezer", "Frozen storage zone"),
    HAZMAT("Hazmat", "Hazardous materials storage zone"),
    HIGH_SECURITY("High Security", "High security storage zone"),
    BULK("Bulk", "Bulk storage zone"),
    FAST_MOVING("Fast Moving", "Fast moving items zone"),
    SLOW_MOVING("Slow Moving", "Slow moving items zone"),
    RETURNS("Returns", "Returns processing zone"),
    DAMAGED("Damaged", "Damaged goods zone"),
    MAINTENANCE("Maintenance", "Equipment maintenance zone"),
    OFFICE("Office", "Administrative office zone"),
    PRODUCTION("Production", "Production/assembly zone"),
    QUALITY_CONTROL("Quality Control", "Quality control testing zone"),
    OVERFLOW("Overflow", "Overflow storage zone"),
    SEASONAL("Seasonal", "Seasonal items storage zone"),
    HIGH_VALUE("High Value", "High value items storage zone"),
    AUTOMATED("Automated", "Automated storage and retrieval zone");

    private final String displayName;
    private final String description;

    ZoneType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isStorageZone() {
        return this == STORAGE || this == COLD_STORAGE || this == FREEZER || 
               this == BULK || this == FAST_MOVING || this == SLOW_MOVING ||
               this == OVERFLOW || this == SEASONAL || this == HIGH_VALUE ||
               this == AUTOMATED;
    }

    public boolean isOperationalZone() {
        return this == RECEIVING || this == SHIPPING || this == PICKING || 
               this == PACKING || this == STAGING || this == CROSSDOCK ||
               this == PRODUCTION;
    }

    public boolean isSpecialHandlingZone() {
        return this == QUARANTINE || this == COLD_STORAGE || this == FREEZER ||
               this == HAZMAT || this == HIGH_SECURITY || this == RETURNS ||
               this == DAMAGED || this == HIGH_VALUE;
    }

    public boolean isTemperatureControlled() {
        return this == COLD_STORAGE || this == FREEZER;
    }

    public boolean requiresSecurityAccess() {
        return this == HIGH_SECURITY || this == HAZMAT || this == HIGH_VALUE ||
               this == QUARANTINE;
    }

    public boolean isAutomated() {
        return this == AUTOMATED;
    }

    public boolean isProcessingZone() {
        return this == QUARANTINE || this == RETURNS || this == DAMAGED ||
               this == QUALITY_CONTROL || this == PRODUCTION;
    }

    public int getDefaultSecurityLevel() {
        return switch (this) {
            case HIGH_SECURITY, HIGH_VALUE -> 4;
            case HAZMAT, QUARANTINE -> 3;
            case OFFICE, QUALITY_CONTROL -> 2;
            default -> 1;
        };
    }

    public int getDefaultPickingPriority() {
        return switch (this) {
            case FAST_MOVING, CROSSDOCK -> 1;
            case PICKING, SHIPPING -> 2;
            case STORAGE -> 3;
            case SLOW_MOVING, BULK -> 4;
            default -> 5;
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}