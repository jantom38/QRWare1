package com.qrware.domain.qr;

/**
 * Enumeration defining different types of QR codes used in the system
 */
public enum QRCodeType {
    
    LOCATION("Location", "QR code for warehouse location"),
    PRODUCT("Product", "QR code for product identification"),
    INVENTORY_ITEM("Inventory Item", "QR code for specific inventory item"),
    PALLET("Pallet", "QR code for pallet tracking"),
    CONTAINER("Container", "QR code for container tracking"),
    SHIPMENT("Shipment", "QR code for shipment tracking"),
    ORDER("Order", "QR code for order tracking"),
    USER("User", "QR code for user identification"),
    EQUIPMENT("Equipment", "QR code for equipment tracking"),
    DOCUMENT("Document", "QR code for document reference"),
    TASK("Task", "QR code for task tracking"),
    ZONE("Zone", "QR code for warehouse zone"),
    RACK("Rack", "QR code for storage rack"),
    SHELF("Shelf", "QR code for storage shelf"),
    BIN("Bin", "QR code for storage bin"),
    VEHICLE("Vehicle", "QR code for vehicle tracking"),
    SUPPLIER("Supplier", "QR code for supplier information"),
    CUSTOMER("Customer", "QR code for customer information"),
    BATCH("Batch", "QR code for batch tracking"),
    LOT("Lot", "QR code for lot tracking"),
    SERIAL("Serial", "QR code for serial number tracking"),
    ASSET("Asset", "QR code for asset tracking"),
    MAINTENANCE("Maintenance", "QR code for maintenance tracking"),
    QUALITY_CHECK("Quality Check", "QR code for quality inspection"),
    SAFETY("Safety", "QR code for safety information"),
    EMERGENCY("Emergency", "QR code for emergency procedures"),
    TEMP_ACCESS("Temporary Access", "Temporary QR code for access"),
    API_ACCESS("API Access", "QR code for API authentication"),
    MOBILE_APP("Mobile App", "QR code for mobile app features"),
    REPORT("Report", "QR code for report access"),
    CUSTOM("Custom", "Custom QR code type");

    private final String displayName;
    private final String description;

    QRCodeType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPhysicalEntity() {
        return this == LOCATION || this == PRODUCT || this == INVENTORY_ITEM || 
               this == PALLET || this == CONTAINER || this == EQUIPMENT ||
               this == ZONE || this == RACK || this == SHELF || this == BIN ||
               this == VEHICLE || this == ASSET;
    }

    public boolean isTrackingEntity() {
        return this == SHIPMENT || this == ORDER || this == BATCH || this == LOT ||
               this == SERIAL || this == TASK || this == MAINTENANCE;
    }

    public boolean isPersonEntity() {
        return this == USER || this == SUPPLIER || this == CUSTOMER;
    }

    public boolean isDocumentEntity() {
        return this == DOCUMENT || this == REPORT;
    }

    public boolean isAccessEntity() {
        return this == TEMP_ACCESS || this == API_ACCESS || this == MOBILE_APP;
    }

    public boolean isQualityEntity() {
        return this == QUALITY_CHECK || this == SAFETY;
    }

    public boolean isTemporary() {
        return this == TEMP_ACCESS || this == EMERGENCY;
    }

    public boolean requiresExpiration() {
        return this == TEMP_ACCESS || this == API_ACCESS;
    }

    public boolean isHighSecurity() {
        return this == USER || this == API_ACCESS || this == EMERGENCY || this == SAFETY;
    }

    public boolean canHaveMultipleCodes() {
        return this == INVENTORY_ITEM || this == BATCH || this == LOT || this == SERIAL ||
               this == DOCUMENT || this == TASK || this == MAINTENANCE;
    }

    public boolean shouldTrackUsage() {
        return this == TEMP_ACCESS || this == API_ACCESS || this == MOBILE_APP ||
               this == EMERGENCY || this == QUALITY_CHECK;
    }

    public int getDefaultExpirationHours() {
        return switch (this) {
            case TEMP_ACCESS -> 24;
            case API_ACCESS -> 168; // 7 days
            case EMERGENCY -> 1;
            default -> -1; // No expiration
        };
    }

    public String getUrlPrefix() {
        return switch (this) {
            case LOCATION -> "/locations/";
            case PRODUCT -> "/products/";
            case INVENTORY_ITEM -> "/inventory/";
            case PALLET -> "/pallets/";
            case CONTAINER -> "/containers/";
            case SHIPMENT -> "/shipments/";
            case ORDER -> "/orders/";
            case USER -> "/users/";
            case EQUIPMENT -> "/equipment/";
            case DOCUMENT -> "/documents/";
            case TASK -> "/tasks/";
            case ZONE -> "/zones/";
            case RACK -> "/racks/";
            case SHELF -> "/shelves/";
            case BIN -> "/bins/";
            case VEHICLE -> "/vehicles/";
            case SUPPLIER -> "/suppliers/";
            case CUSTOMER -> "/customers/";
            case BATCH -> "/batches/";
            case LOT -> "/lots/";
            case SERIAL -> "/serials/";
            case ASSET -> "/assets/";
            case MAINTENANCE -> "/maintenance/";
            case QUALITY_CHECK -> "/quality/";
            case SAFETY -> "/safety/";
            case EMERGENCY -> "/emergency/";
            case TEMP_ACCESS -> "/temp-access/";
            case API_ACCESS -> "/api-access/";
            case MOBILE_APP -> "/mobile/";
            case REPORT -> "/reports/";
            case CUSTOM -> "/custom/";
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}