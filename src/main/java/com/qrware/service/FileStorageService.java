package com.qrware.service;

import com.qrware.config.FileStorageConfig;
import com.qrware.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path qrCodeStorageLocation;
    private final FileStorageConfig fileStorageConfig;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
        this.qrCodeStorageLocation = Paths.get(fileStorageConfig.getQrCodeStoragePath())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.qrCodeStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Zapisuje obraz QR code do pliku
     */
    public String storeQRCodeImage(byte[] imageData, String originalFileName) {
        try {
            // Generuj unikalną nazwę pliku
            String fileName = generateUniqueFileName(originalFileName);
            
            // Sprawdź czy nazwa jest bezpieczna
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Zapisz plik
            Path targetLocation = this.qrCodeStorageLocation.resolve(fileName);
            Files.write(targetLocation, imageData, StandardOpenOption.CREATE);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    /**
     * Pobiera obraz QR code jako Resource
     */
    public Resource loadQRCodeAsResource(String fileName) {
        try {
            Path filePath = this.qrCodeStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File", "name", fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File", "name", fileName);
        }
    }

    /**
     * Usuwa plik QR code
     */
    public boolean deleteQRCodeImage(String fileName) {
        try {
            Path filePath = this.qrCodeStorageLocation.resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Sprawdza czy plik istnieje
     */
    public boolean fileExists(String fileName) {
        Path filePath = this.qrCodeStorageLocation.resolve(fileName).normalize();
        return Files.exists(filePath);
    }

    /**
     * Pobiera rozmiar pliku w bajtach
     */
    public long getFileSize(String fileName) {
        try {
            Path filePath = this.qrCodeStorageLocation.resolve(fileName).normalize();
            return Files.size(filePath);
        } catch (IOException ex) {
            return 0L;
        }
    }

    /**
     * Generuje unikalną nazwę pliku
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("qr_%s_%s.%s", timestamp, uuid, extension);
    }

    /**
     * Pobiera rozszerzenie pliku
     */
    private String getFileExtension(String fileName) {
        if (StringUtils.hasText(fileName)) {
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                return fileName.substring(dotIndex + 1).toLowerCase();
            }
        }
        return "png"; // domyślne rozszerzenie
    }

    /**
     * Pobiera pełną ścieżkę do pliku
     */
    public String getFilePath(String fileName) {
        return this.qrCodeStorageLocation.resolve(fileName).toString();
    }

    /**
     * Pobiera URL do pliku (dla API)
     */
    public String getFileUrl(String fileName) {
        return "/api/qrcodes/image/" + fileName;
    }
}