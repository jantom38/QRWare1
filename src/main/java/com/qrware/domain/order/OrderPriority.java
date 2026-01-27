package com.qrware.domain.order;

public enum OrderPriority {
    LOW("Low", "Niski", 1, "#28a745"),
    NORMAL("Normal", "Normalny", 2, "#6c757d"),
    HIGH("High", "Wysoki", 3, "#fd7e14"),
    URGENT("Urgent", "Pilny", 4, "#dc3545"),
    CRITICAL("Critical", "Krytyczny", 5, "#721c24");

    private final String displayName;
    private final String description;
    private final int level;
    private final String colorCode;

    OrderPriority(String displayName, String description, int level, String colorCode) {
        this.displayName = displayName;
        this.description = description;
        this.level = level;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public String getColorCode() {
        return colorCode;
    }

    public boolean isHigherThan(OrderPriority other) {
        return this.level > other.level;
    }

    public boolean isLowerThan(OrderPriority other) {
        return this.level < other.level;
    }

    public boolean isEqualTo(OrderPriority other) {
        return this.level == other.level;
    }

    public boolean requiresImmediateAttention() {
        return this == URGENT || this == CRITICAL;
    }

    public boolean allowsDelay() {
        return this == LOW || this == NORMAL;
    }

    public static OrderPriority fromLevel(int level) {
        for (OrderPriority priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        return NORMAL;
    }

    @Override
    public String toString() {
        return displayName;
    }
}