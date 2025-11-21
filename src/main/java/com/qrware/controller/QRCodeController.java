package com.qrware.controller;

import com.qrware.domain.qr.QRCodeData;
import com.qrware.domain.qr.QRCodeType;
import com.qrware.domain.qr.ErrorCorrectionLevel;
import com.qrware.dto.QRCodeDTO;
import com.qrware.dto.DTOMapper;
import com.qrware.repository.qr.QRCodeDataRepository;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.service.FileStorageService;
import com.qrware.service.QRCodeGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/qr-codes")
@CrossOrigin(origins = {"http://localhost:3000", "http://10.0.2.2:8080"})
public class QRCodeController {

    @Autowired
    private QRCodeDataRepository qrCodeRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private QRCodeGenerationService qrCodeGenerationService;

    // Pobierz wszystkie kody QR z paginacją
    @GetMapping
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<Page<QRCodeDTO>> getAllQRCodes(Pageable pageable) {
        Page<QRCodeData> qrCodes = qrCodeRepository.findAll(pageable);
        Page<QRCodeDTO> qrCodeDTOs = qrCodes.map(dtoMapper::toDTO);
        return ResponseEntity.ok(qrCodeDTOs);
    }

    // Pobierz kod QR po ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<QRCodeDTO> getQRCodeById(@PathVariable Long id) {
        Optional<QRCodeData> qrCode = qrCodeRepository.findById(id);
        if (qrCode.isPresent()) {
            return ResponseEntity.ok(dtoMapper.toDTO(qrCode.get()));
        }
        throw new ResourceNotFoundException("QR Code", "id", id);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<QRCodeDTO> getQRCodeByCode(@PathVariable String code) {
        Optional<QRCodeData> qrCode = qrCodeRepository.findByCode(code);
        if (qrCode.isPresent()) {
            QRCodeData qrCodeData = qrCode.get();
            qrCodeData.setScanCount(qrCodeData.getScanCount() + 1);
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            qrCodeRepository.save(qrCodeData);
            
            return ResponseEntity.ok(dtoMapper.toDTO(qrCodeData));
        }
        throw new ResourceNotFoundException("QR Code", "code", code);
    }

    // Pobierz kody QR po typie encji
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<QRCodeDTO> getQRCodeByEntity(@PathVariable String entityType, 
                                                     @PathVariable Long entityId) {
        Optional<QRCodeData> qrCode = qrCodeRepository.findByEntityTypeAndEntityId(entityType, entityId);
        if (qrCode.isPresent()) {
            return ResponseEntity.ok(dtoMapper.toDTO(qrCode.get()));
        }
        throw new ResourceNotFoundException("QR Code", "entity", entityType + ":" + entityId);
    }

    // Pobierz aktywne kody QR
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<List<QRCodeDTO>> getActiveQRCodes() {
        List<QRCodeData> qrCodes = qrCodeRepository.findByActiveTrue();
        List<QRCodeDTO> qrCodeDTOs = qrCodes.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(qrCodeDTOs);
    }

    // Pobierz kody QR po typie
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<List<QRCodeDTO>> getQRCodesByType(@PathVariable QRCodeType type) {
        List<QRCodeData> qrCodes = qrCodeRepository.findByType(type);
        List<QRCodeDTO> qrCodeDTOs = qrCodes.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(qrCodeDTOs);
    }

    // Wygeneruj nowy kod QR
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('QR_GENERATE')")
    public ResponseEntity<QRCodeDTO> generateQRCode(@Valid @RequestBody GenerateQRRequest request) {
        QRCodeData qrCode = new QRCodeData();
        qrCode.setCode(request.getCode());
        qrCode.setType(request.getType());
        qrCode.setEntityType(request.getEntityType());
        qrCode.setEntityId(request.getEntityId());
        qrCode.setData(request.getData());
        qrCode.setMetadata(request.getMetadata());
        qrCode.setActive(true);
        qrCode.setExpiresAt(request.getExpiresAt());
        qrCode.setScanCount(0L);
        qrCode.setFormat(request.getFormat() != null ? request.getFormat() : "PNG");
        qrCode.setSize(request.getSize() != null ? request.getSize() : 300);
        qrCode.setErrorCorrectionLevel(request.getErrorCorrectionLevel() != null ? request.getErrorCorrectionLevel() : ErrorCorrectionLevel.M);
        qrCode.setGeneratedBy(request.getGeneratedBy());
        qrCode.setGenerationReason(request.getGenerationReason());

        QRCodeData savedQRCode = qrCodeRepository.save(qrCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toDTO(savedQRCode));
    }

    // Aktualizuj kod QR
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('QR_GENERATE')")
    public ResponseEntity<QRCodeDTO> updateQRCode(@PathVariable Long id, 
                                                @Valid @RequestBody UpdateQRRequest request) {
        Optional<QRCodeData> existingQRCode = qrCodeRepository.findById(id);
        if (!existingQRCode.isPresent()) {
            throw new ResourceNotFoundException("QR Code", "id", id);
        }

        QRCodeData qrCode = existingQRCode.get();
        
        if (request.getData() != null) qrCode.setData(request.getData());
        if (request.getMetadata() != null) qrCode.setMetadata(request.getMetadata());
        if (request.getActive() != null) qrCode.setActive(request.getActive());
        if (request.getExpiresAt() != null) qrCode.setExpiresAt(request.getExpiresAt());

        QRCodeData updatedQRCode = qrCodeRepository.save(qrCode);
        return ResponseEntity.ok(dtoMapper.toDTO(updatedQRCode));
    }

    // Usuń kod QR (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('QR_GENERATE')")
    public ResponseEntity<Void> deleteQRCode(@PathVariable Long id) {
        Optional<QRCodeData> existingQRCode = qrCodeRepository.findById(id);
        if (!existingQRCode.isPresent()) {
            throw new ResourceNotFoundException("QR Code", "id", id);
        }

        QRCodeData qrCode = existingQRCode.get();
        qrCode.setActive(false);
        qrCodeRepository.save(qrCode);
        
        return ResponseEntity.noContent().build();
    }

    // Aktywuj/dezaktywuj kod QR
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('QR_GENERATE')")
    public ResponseEntity<QRCodeDTO> toggleQRCodeActive(@PathVariable Long id) {
        Optional<QRCodeData> existingQRCode = qrCodeRepository.findById(id);
        if (!existingQRCode.isPresent()) {
            throw new ResourceNotFoundException("QR Code", "id", id);
        }

        QRCodeData qrCode = existingQRCode.get();
        qrCode.setActive(!qrCode.getActive());
        QRCodeData updatedQRCode = qrCodeRepository.save(qrCode);
        
        return ResponseEntity.ok(dtoMapper.toDTO(updatedQRCode));
    }

    // Pobierz statystyki kodów QR
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<QRStatsResponse> getQRStats() {
        long totalCodes = qrCodeRepository.count();
        long activeCodes = qrCodeRepository.findByActiveTrue().size();
        
        // Oblicz łączną liczbę skanowań
        List<QRCodeData> allCodes = qrCodeRepository.findAll();
        long totalScans = allCodes.stream()
                .mapToLong(QRCodeData::getScanCount)
                .sum();
        
        QRStatsResponse stats = new QRStatsResponse();
        stats.setTotalCodes(totalCodes);
        stats.setActiveCodes(activeCodes);
        stats.setInactiveCodes(totalCodes - activeCodes);
        stats.setTotalScans(totalScans);
        
        return ResponseEntity.ok(stats);
    }

    // DTOs
    public static class GenerateQRRequest {
        private String code;
        private QRCodeType type;
        private String entityType;
        private Long entityId;
        private String data;
        private String metadata;
        private LocalDateTime expiresAt;
        private String format;
        private Integer size;
        private ErrorCorrectionLevel errorCorrectionLevel;
        private String generatedBy;
        private String generationReason;

        // Gettery i settery
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
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
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
    }

    public static class UpdateQRRequest {
        private String data;
        private String metadata;
        private Boolean active;
        private LocalDateTime expiresAt;

        // Gettery i settery
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }

    public static class QRStatsResponse {
        private long totalCodes;
        private long activeCodes;
        private long inactiveCodes;
        private long totalScans;

        // Gettery i settery
        public long getTotalCodes() { return totalCodes; }
        public void setTotalCodes(long totalCodes) { this.totalCodes = totalCodes; }
        public long getActiveCodes() { return activeCodes; }
        public void setActiveCodes(long activeCodes) { this.activeCodes = activeCodes; }
        public long getInactiveCodes() { return inactiveCodes; }
        public void setInactiveCodes(long inactiveCodes) { this.inactiveCodes = inactiveCodes; }
        public long getTotalScans() { return totalScans; }
        public void setTotalScans(long totalScans) { this.totalScans = totalScans; }
    }

    // ==================== ENDPOINTS DO OBSŁUGI PLIKÓW QR ====================

    /**
     * Pobiera obraz QR kodu
     */
    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> getQRCodeImage(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadQRCodeAsResource(fileName);
            
            String contentType = "image/png";
            if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.toLowerCase().endsWith(".svg")) {
                contentType = "image/svg+xml";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
                    
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generuje nowy QR kod z obrazem (nowa implementacja)
     */
    @PostMapping("/generate-with-image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> generateQRCodeWithImage(@RequestBody @Valid GenerateQRImageRequest request) {
        try {
            QRCodeData qrCode = qrCodeGenerationService.generateQRCodeSync(
                    request.getData(),
                    request.getType(),
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getGeneratedBy(),
                    request.getGenerationReason()
            );

            QRCodeDTO dto = dtoMapper.toDTO(qrCode);
            dto.setImagePath(fileStorageService.getFileUrl(qrCode.getImagePath()));
            
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to generate QR code: " + e.getMessage()));
        }
    }

    /**
     * Rejestruje skanowanie QR kodu
     */
    @PostMapping("/scan/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> recordScan(@PathVariable String code) {
        try {
            qrCodeGenerationService.recordScan(code);
            return ResponseEntity.ok(Map.of("message", "Scan recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to record scan: " + e.getMessage()));
        }
    }

    /**
     * Request class for QR generation with image
     */
    public static class GenerateQRImageRequest {
        private String data;
        private QRCodeType type;
        private String entityType;
        private Long entityId;
        private String generatedBy;
        private String generationReason;

        // Gettery i settery
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        
        public QRCodeType getType() { return type; }
        public void setType(QRCodeType type) { this.type = type; }
        
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        
        public String getGeneratedBy() { return generatedBy; }
        public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
        
        public String getGenerationReason() { return generationReason; }
        public void setGenerationReason(String generationReason) { this.generationReason = generationReason; }
    }
}