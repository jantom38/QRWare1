package com.qrware.dto;

public class InventoryAlertDTO {
    private String type;
    private String severity;
    private String sku;
    private String productName;
    private String message;
    private Long entityId;

    public InventoryAlertDTO(String type, String severity, String sku, String productName, String message, Long entityId) {
        this.type = type;
        this.severity = severity;
        this.sku = sku;
        this.productName = productName;
        this.message = message;
        this.entityId = entityId;
    }

    public String getType() { return type; }
    public String getSeverity() { return severity; }
    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public String getMessage() { return message; }
    public Long getEntityId() { return entityId; }
}