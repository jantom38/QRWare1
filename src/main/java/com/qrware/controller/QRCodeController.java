package com.qrware.controller;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.order.OrderItem;
import com.qrware.domain.qr.QRCodeData;
import com.qrware.domain.qr.QRCodeType;
import com.qrware.domain.qr.ErrorCorrectionLevel;
import com.qrware.dto.QRCodeDTO;
import com.qrware.dto.DTOMapper;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.order.OrderItemRepository;
import com.qrware.repository.qr.QRCodeDataRepository;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.service.FileStorageService;
import com.qrware.service.QRCodeGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@CrossOrigin(origins = "*")
public class QRCodeController {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeController.class);

    @Autowired
    private QRCodeDataRepository qrCodeRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private QRCodeGenerationService qrCodeGenerationService;

    @GetMapping
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<Page<QRCodeDTO>> getAllQRCodes(Pageable pageable) {
        logger.info("Fetching all QR codes with pagination");
        Page<QRCodeData> qrCodes = qrCodeRepository.findAll(pageable);
        Page<QRCodeDTO> qrCodeDTOs = qrCodes.map(dtoMapper::toDTO);
        return ResponseEntity.ok(qrCodeDTOs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<QRCodeDTO> getQRCodeById(@PathVariable Long id) {
        logger.info("Fetching QR code by ID: {}", id);
        Optional<QRCodeData> qrCode = qrCodeRepository.findById(id);
        if (qrCode.isPresent()) {
            return ResponseEntity.ok(dtoMapper.toDTO(qrCode.get()));
        }
        logger.warn("QR Code with ID {} not found", id);
        throw new ResourceNotFoundException("QR Code", "id", id);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<QRCodeDTO> getQRCodeByCode(@PathVariable String code) {
        logger.info("Scanning QR code: {}", code);
        Optional<QRCodeData> qrCode = qrCodeRepository.findByCode(code);
        if (qrCode.isPresent()) {
            QRCodeData qrCodeData = qrCode.get();
            qrCodeData.setScanCount(qrCodeData.getScanCount() + 1);
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            qrCodeRepository.save(qrCodeData);

            return ResponseEntity.ok(dtoMapper.toDTO(qrCodeData));
        }
        logger.warn("QR Code '{}' not found in database", code);
        throw new ResourceNotFoundException("QR Code", "code", code);
    }

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

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<List<QRCodeDTO>> getActiveQRCodes() {
        List<QRCodeData> qrCodes = qrCodeRepository.findByActiveTrue();
        List<QRCodeDTO> qrCodeDTOs = qrCodes.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(qrCodeDTOs);
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<List<QRCodeDTO>> getQRCodesByType(@PathVariable QRCodeType type) {
        List<QRCodeData> qrCodes = qrCodeRepository.findByType(type);
        List<QRCodeDTO> qrCodeDTOs = qrCodes.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(qrCodeDTOs);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('QR_GENERATE')")
    public ResponseEntity<QRCodeDTO> generateQRCode(@Valid @RequestBody GenerateQRRequest request) {
        logger.info("Received request to generate QR Code via generic endpoint. Data: {}", request.getData());
        try {
            QRCodeData savedQRCode;

            boolean autoGenerate = request.getCode() == null || request.getCode().trim().isEmpty();

            if (autoGenerate) {
                logger.info("Auto-generating QR code image and ID for entity: {}", request.getEntityType());
                savedQRCode = qrCodeGenerationService.generateQRCodeSync(
                        request.getData(),
                        request.getType(),
                        request.getEntityType(),
                        request.getEntityId(),
                        request.getGeneratedBy() != null ? request.getGeneratedBy() : "API",
                        request.getGenerationReason()
                );
            } else {
                logger.info("Saving manually provided QR code: {}", request.getCode());
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

                savedQRCode = qrCodeRepository.save(qrCode);
            }

            if ("inventory_item".equalsIgnoreCase(request.getEntityType())) {
                InventoryItem item = inventoryItemRepository.findById(request.getEntityId()).orElseThrow(() -> new ResourceNotFoundException("InventoryItem not found"));
                item.setQrCode(savedQRCode.getCode());
                inventoryItemRepository.save(item);
            } else if ("order_item".equalsIgnoreCase(request.getEntityType())) {
                OrderItem item = orderItemRepository.findById(request.getEntityId()).orElseThrow(() -> new ResourceNotFoundException("OrderItem not found"));
                item.setQrCodeData(savedQRCode.getCode());
                orderItemRepository.save(item);
            }

            logger.info("QR Code generated successfully with ID: {}", savedQRCode.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toDTO(savedQRCode));

        } catch (Exception e) {
            logger.error("CRITICAL ERROR in /generate endpoint: ", e);
            throw new RuntimeException("Błąd podczas generowania kodu QR: " + e.getMessage(), e);
        }
    }

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('QR_GENERATE')")
    public ResponseEntity<Void> deleteQRCode(@PathVariable Long id) {
        logger.info("Deleting (soft) QR Code ID: {}", id);
        Optional<QRCodeData> existingQRCode = qrCodeRepository.findById(id);
        if (!existingQRCode.isPresent()) {
            throw new ResourceNotFoundException("QR Code", "id", id);
        }

        QRCodeData qrCode = existingQRCode.get();
        qrCode.setActive(false);
        qrCodeRepository.save(qrCode);

        return ResponseEntity.noContent().build();
    }

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

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<QRStatsResponse> getQRStats() {
        long totalCodes = qrCodeRepository.count();
        long activeCodes = qrCodeRepository.findByActiveTrue().size();

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

    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> getQRCodeImage(@PathVariable String fileName) {
        logger.info("Requesting image file: {}", fileName);
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
            logger.warn("Image file not found: {}", fileName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error serving image file: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate-with-image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> generateQRCodeWithImage(@RequestBody @Valid GenerateQRImageRequest request) {
        logger.info("Request to generate QR with image for data: {}", request.getData());
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

            logger.info("QR Code generated successfully. Image path: {}", qrCode.getImagePath());
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            logger.error("FAILED to generate QR with image: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to generate QR code: " + e.getMessage()));
        }
    }

    @PostMapping("/scan/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> recordScan(@PathVariable String code) {
        try {
            qrCodeGenerationService.recordScan(code);
            return ResponseEntity.ok(Map.of("message", "Scan recorded successfully"));
        } catch (Exception e) {
            logger.error("Error recording scan for code {}: ", code, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to record scan: " + e.getMessage()));
        }
    }

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

        public long getTotalCodes() { return totalCodes; }
        public void setTotalCodes(long totalCodes) { this.totalCodes = totalCodes; }
        public long getActiveCodes() { return activeCodes; }
        public void setActiveCodes(long activeCodes) { this.activeCodes = activeCodes; }
        public long getInactiveCodes() { return inactiveCodes; }
        public void setInactiveCodes(long inactiveCodes) { this.inactiveCodes = inactiveCodes; }
        public long getTotalScans() { return totalScans; }
        public void setTotalScans(long totalScans) { this.totalScans = totalScans; }
    }

    public static class GenerateQRImageRequest {
        private String data;
        private QRCodeType type;
        private String entityType;
        private Long entityId;
        private String generatedBy;
        private String generationReason;

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