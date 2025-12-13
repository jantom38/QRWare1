package com.example.shared.data.model


enum class ZoneType(
    val displayName: String,
    val description: String
) {
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
    AUTOMATED("Automated", "Automated storage and retrieval zone"); // <-- TEN ŚREDNIK JEST KLUCZOWY

    // Właściwości pomocnicze w stylu z LocationType.kt
    val isStorageZone: Boolean
        get() = this in setOf(STORAGE, COLD_STORAGE, FREEZER, BULK, FAST_MOVING, SLOW_MOVING, OVERFLOW, SEASONAL, HIGH_VALUE, AUTOMATED)

    val isOperationalZone: Boolean
        get() = this in setOf(RECEIVING, SHIPPING, PICKING, PACKING, STAGING, CROSSDOCK, PRODUCTION)

    val isSpecialHandlingZone: Boolean
        get() = this in setOf(QUARANTINE, COLD_STORAGE, FREEZER, HAZMAT, HIGH_SECURITY, RETURNS, DAMAGED, HIGH_VALUE)

    val isTemperatureControlled: Boolean
        get() = this in setOf(COLD_STORAGE, FREEZER)

    val requiresSecurityAccess: Boolean
        get() = this in setOf(HIGH_SECURITY, HAZMAT, HIGH_VALUE, QUARANTINE)

    val isAutomated: Boolean
        get() = this == AUTOMATED

    val isProcessingZone: Boolean
        get() = this in setOf(QUARANTINE, RETURNS, DAMAGED, QUALITY_CONTROL, PRODUCTION)

    val defaultSecurityLevel: Int
        get() = when (this) {
            HIGH_SECURITY, HIGH_VALUE -> 4
            HAZMAT, QUARANTINE -> 3
            OFFICE, QUALITY_CONTROL -> 2
            else -> 1
        }

    val defaultPickingPriority: Int
        get() = when (this) {
            FAST_MOVING, CROSSDOCK -> 1
            PICKING, SHIPPING -> 2
            STORAGE -> 3
            SLOW_MOVING, BULK -> 4
            else -> 5
        }

    override fun toString(): String {
        return displayName
    }
}