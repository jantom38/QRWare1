package com.qrware.dto;

import com.qrware.domain.qr.ErrorCorrectionLevel;
import com.qrware.domain.qr.QRCodeType;

import java.time.LocalDateTime;

public class QRCodeDTO {
    private Long id;
    private String code;
    private QRCodeType type;
    private String entityType;
    private Long entityId;
    private String data;
    private String metadata;
    private Boolean active;
    private LocalDateTime expiresAt;
    private LocalDateTime lastScanned;
    private Long scanCount;
    private String format;
    private Integer size;
    private ErrorCorrectionLevel errorCorrectionLevel;
    private String generatedBy;
    private String generationReason;
    private String imagePath;

    public QRCodeDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public QRCodeType getType() { return type; }
    public void setType(QRCodeType type) { this.type = type; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getLastScanned() { return lastScanned; }
    public void setLastScanned(LocalDateTime lastScanned) { this.lastScanned = lastScanned; }
    
    public Long getScanCount() { return scanCount; }
    public void setScanCount(Long scanCount) { this.scanCount = scanCount; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    
    public ErrorCorrectionLevel getErrorCorrectionLevel() { return errorCorrectionLevel; }
    public void setErrorCorrectionLevel(ErrorCorrectionLevel errorCorrectionLevel) { this.errorCorrectionLevel = errorCorrectionLevel; }
    
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    
    public String getGenerationReason() { return generationReason; }
    public void setGenerationReason(String generationReason) { this.generationReason = generationReason; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}