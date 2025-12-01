package com.qrware.domain.order;

public enum OrderItemStatus {
    PENDING("Pending", "Oczekująca", false, true),
    IN_PROGRESS("In Progress", "W trakcie", true, true),
    PICKED("Picked", "Pobrana", true, true),
    PARTIALLY_COMPLETED("Partially Completed", "Częściowo ukończona", true, true),
    COMPLETED("Completed", "Ukończona", false, false),
    CANCELLED("Cancelled", "Anulowana", false, false),
    ON_HOLD("On Hold", "Wstrzymana", false, true),
    BACK_ORDERED("Back Ordered", "Odroczona", false, true),
    DAMAGED("Damaged", "Uszkodzona", false, true),
    SHORT_PICKED("Short Picked", "Niepełna realizacja", false, true);

    private final String displayName;
    private final String description;
    private final boolean requiresAction;
    private final boolean canBeModified;

    OrderItemStatus(String displayName, String description, boolean requiresAction, boolean canBeModified) {
        this.displayName = displayName;
        this.description = description;
        this.requiresAction = requiresAction;
        this.canBeModified = canBeModified;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresAction() {
        return requiresAction;
    }

    public boolean canBeModified() {
        return canBeModified;
    }

    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS || this == PICKED || 
               this == PARTIALLY_COMPLETED || this == ON_HOLD || this == BACK_ORDERED;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean isProblematic() {
        return this == DAMAGED || this == SHORT_PICKED || this == BACK_ORDERED;
    }

    public boolean canTransitionTo(OrderItemStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == IN_PROGRESS || newStatus == PICKED || 
                       newStatus == ON_HOLD || newStatus == CANCELLED ||
                       newStatus == BACK_ORDERED;
            case IN_PROGRESS:
                return newStatus == PICKED || newStatus == PARTIALLY_COMPLETED ||
                       newStatus == COMPLETED || newStatus == ON_HOLD ||
                       newStatus == CANCELLED || newStatus == DAMAGED ||
                       newStatus == SHORT_PICKED;
            case PICKED:
                return newStatus == PARTIALLY_COMPLETED || newStatus == COMPLETED ||
                       newStatus == DAMAGED || newStatus == SHORT_PICKED ||
                       newStatus == CANCELLED;
            case PARTIALLY_COMPLETED:
                return newStatus == COMPLETED || newStatus == IN_PROGRESS ||
                       newStatus == CANCELLED || newStatus == SHORT_PICKED;
            case ON_HOLD:
                return newStatus == PENDING || newStatus == IN_PROGRESS ||
                       newStatus == CANCELLED;
            case BACK_ORDERED:
                return newStatus == PENDING || newStatus == CANCELLED;
            case DAMAGED:
                return newStatus == CANCELLED || newStatus == PENDING;
            case SHORT_PICKED:
                return newStatus == COMPLETED || newStatus == CANCELLED ||
                       newStatus == BACK_ORDERED;
            case COMPLETED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}