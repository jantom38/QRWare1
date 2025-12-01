package com.qrware.domain.order;

public enum OrderStatus {
    CREATED("Created", "Zamówienie utworzone", false, true),
    ASSIGNED("Assigned", "Przypisane do użytkownika", false, true),
    IN_PROGRESS("In Progress", "W trakcie realizacji", true, true),
    ON_HOLD("On Hold", "Wstrzymane", false, true),
    PARTIALLY_COMPLETED("Partially Completed", "Częściowo zrealizowane", true, true),
    COMPLETED("Completed", "Zakończone", false, false),
    CANCELLED("Cancelled", "Anulowane", false, false),
    FAILED("Failed", "Nieudane", false, false);

    private final String displayName;
    private final String description;
    private final boolean requiresAction;
    private final boolean canBeModified;

    OrderStatus(String displayName, String description, boolean requiresAction, boolean canBeModified) {
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
        return this == CREATED || this == ASSIGNED || this == IN_PROGRESS || 
               this == ON_HOLD || this == PARTIALLY_COMPLETED;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED || this == FAILED;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case CREATED:
                return newStatus == ASSIGNED || newStatus == IN_PROGRESS || 
                       newStatus == CANCELLED;
            case ASSIGNED:
                return newStatus == IN_PROGRESS || newStatus == ON_HOLD || 
                       newStatus == CANCELLED;
            case IN_PROGRESS:
                return newStatus == PARTIALLY_COMPLETED || newStatus == COMPLETED || 
                       newStatus == ON_HOLD || newStatus == FAILED || newStatus == CANCELLED;
            case ON_HOLD:
                return newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case PARTIALLY_COMPLETED:
                return newStatus == IN_PROGRESS || newStatus == COMPLETED || 
                       newStatus == FAILED || newStatus == CANCELLED;
            case COMPLETED:
            case CANCELLED:
            case FAILED:
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