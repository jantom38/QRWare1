package com.qrware.dto;

public class LowStockReportDTO {
    private Long productId;
    private String sku;
    private String name;
    private Integer currentStock;
    private Integer minimumStock;
    private Integer reorderPoint;
    private String status;

    public LowStockReportDTO(Long productId, String sku, String name, Long currentStock, Integer minimumStock, Integer reorderPoint) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.currentStock = currentStock != null ? currentStock.intValue() : 0;
        this.minimumStock = minimumStock;
        this.reorderPoint = reorderPoint;
        this.status = determineStatus();
    }

    private String determineStatus() {
        if (minimumStock != null && currentStock <= minimumStock) {
            return "CRITICAL";
        }
        if (reorderPoint != null && currentStock <= reorderPoint) {
            return "WARNING";
        }
        return "OK";
    }

    public Long getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public Integer getCurrentStock() { return currentStock; }
    public Integer getMinimumStock() { return minimumStock; }
    public Integer getReorderPoint() { return reorderPoint; }
    public String getStatus() { return status; }
}