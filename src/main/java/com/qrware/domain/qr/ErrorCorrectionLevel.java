package com.qrware.domain.qr;

public enum ErrorCorrectionLevel {
    
    L("Low", "~7% recovery", 0.07),
    
    M("Medium", "~15% recovery", 0.15),
    
    Q("Quartile", "~25% recovery", 0.25),
    
    H("High", "~30% recovery", 0.30);

    private final String displayName;
    private final String description;
    private final double recoveryCapacity;

    ErrorCorrectionLevel(String displayName, String description, double recoveryCapacity) {
        this.displayName = displayName;
        this.description = description;
        this.recoveryCapacity = recoveryCapacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public double getRecoveryCapacity() {
        return recoveryCapacity;
    }

    public int getRecoveryPercentage() {
        return (int) (recoveryCapacity * 100);
    }

    public com.google.zxing.qrcode.decoder.ErrorCorrectionLevel getZXingLevel() {
        return switch (this) {
            case L -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L;
            case M -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M;
            case Q -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q;
            case H -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H;
        };
    }

    public static ErrorCorrectionLevel recommendForUsage(QRCodeType qrCodeType) {
        return switch (qrCodeType) {
            case INVENTORY_ITEM -> Q;
            
            default -> M;
        };
    }

    public static ErrorCorrectionLevel recommendForEnvironment(String environment) {
        return switch (environment.toLowerCase()) {
            case "harsh", "outdoor", "industrial" -> H;
            case "moderate", "warehouse", "factory" -> Q;
            case "clean", "office", "controlled" -> M;
            case "digital", "temporary", "indoor" -> L;
            default -> M;
        };
    }

    public boolean canRecover(double damagePercentage) {
        return damagePercentage <= recoveryCapacity;
    }

    public double getDataCapacityReduction() {
        return switch (this) {
            case L -> 0.0;
            case M -> 0.20;
            case Q -> 0.35;
            case H -> 0.45;
        };
    }

    public boolean isSuitableForLongTerm() {
        return this == Q || this == H;
    }

    public boolean isSuitableForHighFrequency() {
        return this == M || this == Q || this == H;
    }

    public int getRecommendedMinimumSize() {
        return switch (this) {
            case L -> 200;
            case M -> 250;
            case Q -> 300;
            case H -> 350;
        };
    }

    @Override
    public String toString() {
        return displayName + " (" + getRecoveryPercentage() + "% recovery)";
    }
}