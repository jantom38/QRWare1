package com.qrware.domain.qr;


public enum QRCodeType {
    
    PRODUCT("Product", "QR code for product identification"),
    INVENTORY_ITEM("Inventory Item", "QR code for specific inventory item");

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
        return true;
    }

    public boolean isTrackingEntity() {
        return false;
    }

    public boolean isPersonEntity() {
        return false;
    }

    public boolean isDocumentEntity() {
        return false;
    }

    public boolean isAccessEntity() {
        return false;
    }

    public boolean isQualityEntity() {
        return false;
    }

    public boolean isTemporary() {
        return false;
    }

    public boolean requiresExpiration() {
        return false;
    }

    public boolean isHighSecurity() {
        return false;
    }

    public boolean canHaveMultipleCodes() {
        return this == INVENTORY_ITEM;
    }

    public boolean shouldTrackUsage() {
        return false;
    }

    public int getDefaultExpirationHours() {
        return -1; // No expiration
    }

    public String getUrlPrefix() {
        return switch (this) {
            case PRODUCT -> "/products/";
            case INVENTORY_ITEM -> "/inventory/";
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}