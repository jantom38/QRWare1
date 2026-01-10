package com.qrware.domain.qr;


public enum QRCodeType {
    
    PRODUCT("Product", "QR code for product identification"),
    INVENTORY_ITEM("Inventory Item", "QR code for specific inventory item"),
    ZONE("Zone", "QR code for warehouse zone"),
    ORDER("Order", "QR code for order identification"),
    USER("User", "QR code for user identification");

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
        return this == PRODUCT || this == INVENTORY_ITEM || this == ZONE;
    }

    public boolean isTrackingEntity() {
        return this == ORDER;
    }

    public boolean isPersonEntity() {
        return this == USER;
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
        return this == USER;
    }

    public boolean canHaveMultipleCodes() {
        return this == INVENTORY_ITEM;
    }

    public boolean shouldTrackUsage() {
        return true;
    }

    public int getDefaultExpirationHours() {
        return -1; // No expiration
    }

    public String getUrlPrefix() {
        return switch (this) {
            case PRODUCT -> "/products/";
            case INVENTORY_ITEM -> "/inventory/";
            case ZONE -> "/zones/";
            case ORDER -> "/orders/";
            case USER -> "/users/";
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}