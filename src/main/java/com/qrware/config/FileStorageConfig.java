package com.qrware.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {
    
    private String uploadDir = "uploads";
    private String qrCodeDir = "qr-codes";
    private long maxFileSize = 10485760L;
    private String[] allowedExtensions = {"png", "jpg", "jpeg", "svg"};
    
    public String getUploadDir() {
        return uploadDir;
    }
    
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
    
    public String getQrCodeDir() {
        return qrCodeDir;
    }
    
    public void setQrCodeDir(String qrCodeDir) {
        this.qrCodeDir = qrCodeDir;
    }
    
    public long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public String[] getAllowedExtensions() {
        return allowedExtensions;
    }
    
    public void setAllowedExtensions(String[] allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
    
    public String getQrCodeStoragePath() {
        return uploadDir + "/" + qrCodeDir;
    }
}