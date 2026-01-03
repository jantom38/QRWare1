package com.qrware.domain.inventory;

public enum MovementType {
    
    RECEIPT("Receipt", "Item received into inventory"),
    ISSUE("Issue", "Item issued from inventory"),
    TRANSFER("Transfer", "Item transferred between locations"),
    MOVE("Move", "Item moved to different location"),
    ADJUSTMENT("Adjustment", "Inventory quantity adjustment"),
    CYCLE_COUNT("Cycle Count", "Cycle count adjustment"),
    PHYSICAL_COUNT("Physical Count", "Physical inventory count adjustment"),
    RESERVE("Reserve", "Item reserved for order"),
    UNRESERVE("Unreserve", "Item reservation removed"),
    PICK("Pick", "Item picked for order"),
    PACK("Pack", "Item packed for shipment"),
    SHIP("Ship", "Item shipped out"),
    RETURN("Return", "Item returned to inventory"),
    PUTAWAY("Putaway", "Item put away to storage location"),
    REPLENISHMENT("Replenishment", "Item replenished from bulk location"),
    ALLOCATION("Allocation", "Item allocated to specific purpose"),
    DEALLOCATION("Deallocation", "Item allocation removed"),
    QUARANTINE("Quarantine", "Item moved to quarantine"),
    RELEASE("Release", "Item released from quarantine"),
    HOLD("Hold", "Item put on hold"),
    UNHOLD("Unhold", "Item removed from hold"),
    DAMAGE("Damage", "Item marked as damaged"),
    DISPOSAL("Disposal", "Item disposed of"),
    LOSS("Loss", "Item marked as lost"),
    FOUND("Found", "Lost item found"),
    EXPIRY("Expiry", "Item marked as expired"),
    RECALL("Recall", "Item recalled"),
    STAGING("Staging", "Item moved to staging area"),
    CROSSDOCK("Crossdock", "Item moved for crossdocking"),
    CONSOLIDATION("Consolidation", "Items consolidated"),
    SPLIT("Split", "Item quantity split"),
    MERGE("Merge", "Items merged together"),
    CONVERSION("Conversion", "Unit conversion"),
    PRODUCTION("Production", "Item produced/manufactured"),
    CONSUMPTION("Consumption", "Item consumed in production"),
    SCRAP("Scrap", "Item scrapped"),
    REWORK("Rework", "Item sent for rework"),
    SAMPLE("Sample", "Item taken as sample"),
    LOAN("Loan", "Item loaned out"),
    LOAN_RETURN("Loan Return", "Loaned item returned"),
    
    ORDER_RECEIPT("Order Receipt", "Item received via order"),
    ORDER_ISSUE("Order Issue", "Item issued via order"),
    ORDER_PICK("Order Pick", "Item picked for order fulfillment"),
    ORDER_PACK("Order Pack", "Item packed for order shipment"),
    ORDER_CANCEL("Order Cancel", "Movement cancelled due to order cancellation"),
    ORDER_RETURN("Order Return", "Item returned from order"),
    ORDER_ADJUSTMENT("Order Adjustment", "Quantity adjusted due to order discrepancy");

    private final String displayName;
    private final String description;

    MovementType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInbound() {
        return this == RECEIPT || this == RETURN || this == FOUND || 
               this == RELEASE || this == UNHOLD || this == PRODUCTION ||
               this == LOAN_RETURN || this == ORDER_RECEIPT || this == ORDER_RETURN;
    }

    public boolean isOutbound() {
        return this == ISSUE || this == SHIP || this == DISPOSAL || 
               this == LOSS || this == SCRAP || this == CONSUMPTION ||
               this == SAMPLE || this == LOAN || this == ORDER_ISSUE || 
               this == ORDER_PICK || this == ORDER_PACK;
    }

    public boolean isMovement() {
        return this == TRANSFER || this == MOVE || this == PUTAWAY || 
               this == REPLENISHMENT || this == STAGING || this == CROSSDOCK;
    }

    public boolean isStatusChange() {
        return this == RESERVE || this == UNRESERVE || this == QUARANTINE || 
               this == HOLD || this == DAMAGE || this == EXPIRY || this == RECALL;
    }

    public boolean isAdjustment() {
        return this == ADJUSTMENT || this == CYCLE_COUNT || this == PHYSICAL_COUNT;
    }

    public boolean isOrderRelated() {
        return this == RESERVE || this == PICK || this == PACK || this == SHIP ||
               this == ALLOCATION || this == DEALLOCATION || this == ORDER_RECEIPT ||
               this == ORDER_ISSUE || this == ORDER_PICK || this == ORDER_PACK ||
               this == ORDER_CANCEL || this == ORDER_RETURN || this == ORDER_ADJUSTMENT;
    }

    public boolean isQualityRelated() {
        return this == QUARANTINE || this == RELEASE || this == DAMAGE || 
               this == EXPIRY || this == RECALL || this == REWORK || this == SAMPLE;
    }

    public boolean isProductionRelated() {
        return this == PRODUCTION || this == CONSUMPTION || this == SCRAP || 
               this == REWORK || this == CONVERSION;
    }

    public boolean increasesQuantity() {
        return this == RECEIPT || this == RETURN || this == FOUND || 
               this == PRODUCTION || this == LOAN_RETURN || this == ORDER_RECEIPT || 
               this == ORDER_RETURN;
    }

    public boolean decreasesQuantity() {
        return this == ISSUE || this == SHIP || this == DISPOSAL || 
               this == LOSS || this == SCRAP || this == CONSUMPTION ||
               this == SAMPLE || this == LOAN || this == ORDER_ISSUE || 
               this == ORDER_PICK || this == ORDER_PACK;
    }

    public boolean requiresApproval() {
        return this == DISPOSAL || this == SCRAP || this == ADJUSTMENT ||
               this == LOSS || this == DAMAGE || this == ORDER_CANCEL ||
               this == ORDER_ADJUSTMENT;
    }

    public boolean isOrderSpecific() {
        return this == ORDER_RECEIPT || this == ORDER_ISSUE || this == ORDER_PICK ||
               this == ORDER_PACK || this == ORDER_CANCEL || this == ORDER_RETURN ||
               this == ORDER_ADJUSTMENT;
    }

    @Override
    public String toString() {
        return displayName;
    }
}