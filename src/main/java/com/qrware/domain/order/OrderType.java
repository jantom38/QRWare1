package com.qrware.domain.order;

public enum OrderType {
    INBOUND("Inbound", "Przyjęcie towaru do magazynu", "RECEIPT"),
    OUTBOUND("Outbound", "Wydanie towaru z magazynu", "ISSUE"),
    TRANSFER("Transfer", "Przeniesienie między lokalizacjami", "TRANSFER"),
    PICK("Pick", "Kompletacja zamówienia", "PICK"),
    PUTAWAY("Putaway", "Odkładanie towaru na miejsce", "PUTAWAY"),
    CYCLE_COUNT("Cycle Count", "Inwentaryzacja ciągła", "CYCLE_COUNT"),
    REPLENISHMENT("Replenishment", "Uzupełnienie zapasów", "REPLENISHMENT"),
    RETURN("Return", "Zwrot towaru", "RETURN"),
    ADJUSTMENT("Adjustment", "Korekta stanu magazynowego", "ADJUSTMENT"),
    MAINTENANCE("Maintenance", "Konserwacja/przegląd", "MAINTENANCE"),
    QUALITY_CHECK("Quality Check", "Kontrola jakości", "QUALITY_CHECK");

    private final String displayName;
    private final String description;
    private final String defaultMovementType;

    OrderType(String displayName, String description, String defaultMovementType) {
        this.displayName = displayName;
        this.description = description;
        this.defaultMovementType = defaultMovementType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultMovementType() {
        return defaultMovementType;
    }

    public boolean requiresSourceLocation() {
        return this == OUTBOUND || this == TRANSFER || this == PICK || 
               this == REPLENISHMENT || this == RETURN;
    }

    public boolean requiresDestinationLocation() {
        return this == INBOUND || this == TRANSFER || this == PUTAWAY || 
               this == REPLENISHMENT || this == RETURN;
    }

    public boolean isInventoryChanging() {
        return this == INBOUND || this == OUTBOUND || this == ADJUSTMENT || 
               this == RETURN;
    }

    public boolean requiresQRScan() {
        return this != MAINTENANCE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}