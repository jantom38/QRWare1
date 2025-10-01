package com.qrware.domain.qr;

/**
 * Enumeration defining QR code error correction levels
 * 
 * Error correction allows QR codes to be readable even when damaged or partially obscured.
 * Higher levels provide better error recovery but reduce data capacity.
 */
public enum ErrorCorrectionLevel {
    
    /**
     * Low error correction (~7% recovery)
     * Suitable for clean environments with minimal damage risk
     */
    L("Low", "~7% recovery", 0.07),
    
    /**
     * Medium error correction (~15% recovery)
     * Recommended for most warehouse applications
     */
    M("Medium", "~15% recovery", 0.15),
    
    /**
     * Quartile error correction (~25% recovery)
     * Good for environments with moderate wear and tear
     */
    Q("Quartile", "~25% recovery", 0.25),
    
    /**
     * High error correction (~30% recovery)
     * Best for harsh environments or long-term use
     */
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

    /**
     * Get the ZXing library equivalent
     */
    public com.google.zxing.qrcode.decoder.ErrorCorrectionLevel getZXingLevel() {
        return switch (this) {
            case L -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L;
            case M -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M;
            case Q -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q;
            case H -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H;
        };
    }

    /**
     * Recommend error correction level based on usage scenario
     */
    public static ErrorCorrectionLevel recommendForUsage(QRCodeType qrCodeType) {
        return switch (qrCodeType) {
            // High-wear environments
            case LOCATION, ZONE, RACK, SHELF, BIN -> H;
            
            // Medium-wear environments
            case INVENTORY_ITEM, PALLET, CONTAINER, EQUIPMENT -> Q;
            
            // Temporary or digital usage
            case TEMP_ACCESS, API_ACCESS, MOBILE_APP, DOCUMENT -> L;
            
            // Security-critical
            case USER, EMERGENCY, SAFETY -> H;
            
            // Default for most cases
            default -> M;
        };
    }

    /**
     * Recommend error correction level based on environment conditions
     */
    public static ErrorCorrectionLevel recommendForEnvironment(String environment) {
        return switch (environment.toLowerCase()) {
            case "harsh", "outdoor", "industrial" -> H;
            case "moderate", "warehouse", "factory" -> Q;
            case "clean", "office", "controlled" -> M;
            case "digital", "temporary", "indoor" -> L;
            default -> M;
        };
    }

    /**
     * Check if this level provides sufficient recovery for the given damage percentage
     */
    public boolean canRecover(double damagePercentage) {
        return damagePercentage <= recoveryCapacity;
    }

    /**
     * Get the maximum data capacity reduction compared to L level
     */
    public double getDataCapacityReduction() {
        return switch (this) {
            case L -> 0.0;    // No reduction (baseline)
            case M -> 0.20;   // ~20% reduction
            case Q -> 0.35;   // ~35% reduction
            case H -> 0.45;   // ~45% reduction
        };
    }

    /**
     * Check if this level is suitable for long-term storage
     */
    public boolean isSuitableForLongTerm() {
        return this == Q || this == H;
    }

    /**
     * Check if this level is suitable for high-frequency scanning
     */
    public boolean isSuitableForHighFrequency() {
        return this == M || this == Q || this == H;
    }

    /**
     * Get recommended minimum QR code size for this error correction level
     */
    public int getRecommendedMinimumSize() {
        return switch (this) {
            case L -> 200;  // 200x200 pixels
            case M -> 250;  // 250x250 pixels
            case Q -> 300;  // 300x300 pixels
            case H -> 350;  // 350x350 pixels
        };
    }

    @Override
    public String toString() {
        return displayName + " (" + getRecoveryPercentage() + "% recovery)";
    }
}