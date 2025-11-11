package com.qrware.app.data.model

/**
 * Enumeration defining different types of warehouse locations
 * Wersja kliencka (Kotlin) na podstawie enum z serwera.
 */
enum class LocationType(
    val displayName: String,
    val description: String
) {
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

    // Właściwości pomocnicze (bardziej idiomatyczne w Kotlin niż metody)
    val isStorageType: Boolean
        get() = this in setOf(SHELF, RACK, FLOOR, PALLET, BIN, CAGE, BULK, OVERFLOW)

    val isSpecialHandling: Boolean
        get() = this in setOf(COLD_STORAGE, FREEZER, HAZMAT, QUARANTINE, DAMAGED)

    val isOperational: Boolean
        get() = this in setOf(RECEIVING, SHIPPING, STAGING, PICKING, PACKING, CROSSDOCK)

    val isTemperatureControlled: Boolean
        get() = this in setOf(COLD_STORAGE, FREEZER)

    val requiresSecurityAccess: Boolean
        get() = this in setOf(CAGE, HAZMAT, QUARANTINE)

    val isPhysicalLocation: Boolean
        get() = this != VIRTUAL

    override fun toString(): String {
        return displayName
    }
}