package com.qrware.controller;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.InventoryStatus;
import com.qrware.domain.inventory.MovementType;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.inventory.MovementHistoryRepository;
import com.qrware.service.MovementHistoryService;
import com.qrware.exception.ResourceNotFoundException;

import com.qrware.dto.DTOMapper;
import com.qrware.dto.InventoryItemDTO;
import com.qrware.dto.InventoryAlertDTO;
import com.qrware.dto.LowStockReportDTO;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryItemRepository inventoryRepository;

    @Autowired
    private MovementHistoryRepository movementHistoryRepository;
    
    @Autowired
    private MovementHistoryService movementHistoryService;
    
    @Autowired
    private com.qrware.repository.product.ProductRepository productRepository;
    @Autowired
    private com.qrware.repository.warehouse.LocationRepository locationRepository;
    @Autowired
    private DTOMapper dtoMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<Page<InventoryItemDTO>> getAllInventoryItems(Pageable pageable) {

        logger.info("GET /api/inventory - Strona: {}, Rozmiar: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<InventoryItem> itemsPage = inventoryRepository.findAll(pageable);

        Page<InventoryItemDTO> dtoPage = itemsPage.map(dtoMapper::toDTO);

        logger.info("Pomyślnie pobrano i zmapowano {} pozycji.", dtoPage.getNumberOfElements());

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<InventoryItemDTO> getInventoryItemById(@PathVariable Long id) {
        logger.info("GET /api/inventory/{} - Pobieranie pozycji po ID.", id);

        Optional<InventoryItem> item = inventoryRepository.findById(id);

        if (item.isPresent()) {
            logger.info("Znaleziono pozycję dla ID: {}", id);
            return ResponseEntity.ok(dtoMapper.toDTO(item.get()));
        }

        logger.warn("Nie znaleziono pozycji dla ID: {}", id);
        throw new ResourceNotFoundException("Inventory item", "id", id);
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryItemDTO>> getInventoryByProduct(@PathVariable Long productId) {
        logger.info("GET /api/inventory/product/{}", productId);
        List<InventoryItem> items = inventoryRepository.findByProductId(productId);
        List<InventoryItemDTO> dtos = items.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/qr/{qrCode}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<InventoryItemDTO> getInventoryByQRCode(@PathVariable String qrCode) {
        logger.info("GET /api/inventory/qr/{} - Pobieranie pozycji po QR kodzie.", qrCode);
        
        Optional<InventoryItem> item = inventoryRepository.findByQrCode(qrCode);
        
        if (item.isPresent()) {
            logger.info("Znaleziono pozycję magazynową dla QR kodu: {}", qrCode);
            return ResponseEntity.ok(dtoMapper.toDTO(item.get()));
        }
        
        logger.warn("Nie znaleziono pozycji magazynowej dla QR kodu: {}", qrCode);
        throw new ResourceNotFoundException("Inventory item", "qrCode", qrCode);
    }

    @GetMapping("/location/{locationId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryItemDTO>> getInventoryByLocation(@PathVariable Long locationId) {
        logger.info("GET /api/inventory/location/{}", locationId);
        List<InventoryItem> items = inventoryRepository.findByLocationId(locationId);
        List<InventoryItemDTO> dtos = items.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryItemDTO>> getInventoryByStatus(@PathVariable InventoryStatus status) {
        logger.info("GET /api/inventory/status/{}", status);
        List<InventoryItem> items = inventoryRepository.findByStatus(status);
        List<InventoryItemDTO> dtos = items.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryItemDTO>> getLowStockItems() {
        logger.info("GET /api/inventory/low-stock");
        List<InventoryItem> items = inventoryRepository.findLowStockItems();
        List<InventoryItemDTO> dtos = items.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryAlertDTO>> getInventoryAlerts() {
        logger.info("GET /api/inventory/alerts");
        
        List<InventoryAlertDTO> alerts = new ArrayList<>();


        List<LowStockReportDTO> lowStockProducts = productRepository.findLowStockProducts();
        for (LowStockReportDTO product : lowStockProducts) {
            String severity = "WARNING";
            String message = "Niski stan magazynowy. Obecnie: " + product.getCurrentStock();
            
            if (product.getStatus().equals("CRITICAL")) {
                severity = "CRITICAL";
                message = "KRYTYCZNIE niski stan magazynowy! Poniżej minimum (" + product.getMinimumStock() + ").";
            } else if (product.getReorderPoint() != null) {
                message += ". Punkt zamawiania: " + product.getReorderPoint();
            }

            alerts.add(new InventoryAlertDTO(
                "LOW_STOCK",
                severity,
                product.getSku(),
                product.getName(),
                message,
                product.getProductId()
            ));
        }


        List<InventoryItem> expiredItems = inventoryRepository.findExpiredItems();
        for (InventoryItem item : expiredItems) {
            alerts.add(new InventoryAlertDTO(
                "EXPIRED",
                "CRITICAL",
                item.getProduct().getSku(),
                item.getProduct().getName(),
                "Towar przeterminowany! Data ważności: " + item.getExpiryDate() + ". Ilość: " + item.getQuantity() + ". Lokalizacja: " + item.getLocation().getCode(),
                item.getId()
            ));
        }

        return ResponseEntity.ok(alerts);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<InventoryItemDTO> createInventoryItem(@Valid @RequestBody CreateInventoryRequest request) {
        logger.info("POST /api/inventory - Tworzenie nowej pozycji.");

        var product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        var location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", request.getLocationId()));

        InventoryItem item = new InventoryItem();

        item.setProduct(product);
        item.setLocation(location); 

        item.setQuantity(request.getQuantity());
        item.setReservedQuantity(request.getReservedQuantity() != null ? request.getReservedQuantity() : 0);
        item.setStatus(request.getStatus());
        item.setQrCode(request.getQrCode());
        item.setLotNumber(request.getLotNumber());
        item.setBatchNumber(request.getBatchNumber());
        item.setSerialNumber(request.getSerialNumber());
        item.setReceivedDate(request.getReceivedDate() != null ? request.getReceivedDate() : java.time.LocalDate.now());
        item.setExpiryDate(request.getExpiryDate());
        item.setManufactureDate(request.getManufactureDate());
        item.setUnitCost(request.getUnitCost());
        item.setSupplierReference(request.getSupplierReference());
        item.setManufacturer(request.getManufacturer());
        item.setPurchaseOrderNumber(request.getPurchaseOrderNumber());
        item.setNotes(request.getNotes());
        item.setTemperature(request.getTemperature());
        item.setHumidity(request.getHumidity());
        item.setConditionRating(request.getConditionRating() != null ? request.getConditionRating() : 10);
        item.setQuarantine(request.getQuarantine() != null ? request.getQuarantine() : false);
        item.setQuarantineReason(request.getQuarantineReason());
        item.setHold(request.getHold() != null ? request.getHold() : false);
        item.setHoldReason(request.getHoldReason());

        InventoryItem savedItem = inventoryRepository.save(item);
        
        if (savedItem == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        logger.info("Utworzono nową pozycję z ID: {}", savedItem.getId());


        InventoryItem reloadedItem = inventoryRepository.findById(savedItem.getId()).orElse(savedItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toDTO(reloadedItem));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<InventoryItemDTO> updateInventoryItem(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateInventoryRequest request) {
        logger.info("PUT /api/inventory/{} - Aktualizacja pozycji.", id);
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", "id", id));

        if (request.getQuantity() != null) item.setQuantity(request.getQuantity());
        if (request.getReservedQuantity() != null) item.setReservedQuantity(request.getReservedQuantity());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        if (request.getLotNumber() != null) item.setLotNumber(request.getLotNumber());
        if (request.getBatchNumber() != null) item.setBatchNumber(request.getBatchNumber());
        if (request.getSerialNumber() != null) item.setSerialNumber(request.getSerialNumber());
        if (request.getReceivedDate() != null) item.setReceivedDate(request.getReceivedDate());
        if (request.getExpiryDate() != null) item.setExpiryDate(request.getExpiryDate());
        if (request.getManufactureDate() != null) item.setManufactureDate(request.getManufactureDate());
        if (request.getUnitCost() != null) item.setUnitCost(request.getUnitCost());
        if (request.getSupplierReference() != null) item.setSupplierReference(request.getSupplierReference());
        if (request.getManufacturer() != null) item.setManufacturer(request.getManufacturer());
        if (request.getPurchaseOrderNumber() != null) item.setPurchaseOrderNumber(request.getPurchaseOrderNumber());
        if (request.getNotes() != null) item.setNotes(request.getNotes());
        if (request.getTemperature() != null) item.setTemperature(request.getTemperature());
        if (request.getHumidity() != null) item.setHumidity(request.getHumidity());
        if (request.getConditionRating() != null) item.setConditionRating(request.getConditionRating());
        if (request.getQuarantine() != null) item.setQuarantine(request.getQuarantine());
        if (request.getQuarantineReason() != null) item.setQuarantineReason(request.getQuarantineReason());
        if (request.getHold() != null) item.setHold(request.getHold());
        if (request.getHoldReason() != null) item.setHoldReason(request.getHoldReason());

        InventoryItem updatedItem = inventoryRepository.save(item);
        
        if (updatedItem == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        InventoryItem reloadedItem = inventoryRepository.findById(updatedItem.getId()).orElse(updatedItem);
        return ResponseEntity.ok(dtoMapper.toDTO(reloadedItem));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_DELETE')")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        logger.info("DELETE /api/inventory/{} - Usuwanie pozycji.", id);
        if (!inventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory item", "id", id);
        }
        inventoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<InventoryItemDTO> receiveStock(@PathVariable Long id,
                                                         @RequestBody QuantityUpdateRequest request) {
        logger.info("POST /api/inventory/{}/receive - Przyjęcie towaru.", id);
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", "id", id));

        Integer previousQuantity = item.getQuantity();
        Integer newQuantity = previousQuantity + request.getQuantity();
        item.setQuantity(newQuantity);

        InventoryItem updatedItem = inventoryRepository.save(item);
        
        if (updatedItem == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            movementHistoryService.createMovementHistory(
                item.getId(),
                MovementType.RECEIPT,
                previousQuantity,
                newQuantity,
                null, 
                item.getLocation(), 
                request.getReason() != null ? request.getReason() : "Przyjęcie towaru przez aplikację"
            );
            logger.info("Utworzono wpis w historii ruchów dla przyjęcia towaru. ID pozycji: {}", id);
        } catch (Exception e) {
            logger.error("Błąd podczas tworzenia wpisu w historii ruchów: {}", e.getMessage());
        }

        InventoryItem reloadedItem = inventoryRepository.findById(updatedItem.getId()).orElse(updatedItem);
        return ResponseEntity.ok(dtoMapper.toDTO(reloadedItem));
    }
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryItemDTO>> searchInventory(@RequestParam("query") String query) {
        logger.info("GET /api/inventory/search?query={}", query);

        List<InventoryItem> items = inventoryRepository.searchInventory(query);

        List<InventoryItemDTO> dtos = items.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<InventoryItemDTO> issueStock(@PathVariable Long id,
                                                       @RequestBody QuantityUpdateRequest request) {
        logger.info("POST /api/inventory/{}/issue - Wydanie towaru.", id);
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", "id", id));

        Integer previousQuantity = item.getQuantity();
        Integer newQuantity = previousQuantity - request.getQuantity();

        if (newQuantity < 0) {
            logger.warn("Próba wydania większej ilości towaru niż dostępna dla ID: {}", id);
            return ResponseEntity.badRequest().build(); 
        }

        item.setQuantity(newQuantity);
        InventoryItem updatedItem = inventoryRepository.save(item);
        
        if (updatedItem == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            movementHistoryService.createMovementHistory(
                item.getId(),
                MovementType.ISSUE,
                previousQuantity,
                newQuantity,
                item.getLocation(), 
                null, 
                request.getReason() != null ? request.getReason() : "Wydanie towaru przez aplikację"
            );
            logger.info("Utworzono wpis w historii ruchów dla wydania towaru. ID pozycji: {}", id);
        } catch (Exception e) {
            logger.error("Błąd podczas tworzenia wpisu w historii ruchów: {}", e.getMessage());
        }

        InventoryItem reloadedItem = inventoryRepository.findById(updatedItem.getId()).orElse(updatedItem);
        return ResponseEntity.ok(dtoMapper.toDTO(reloadedItem));
    }


    public static class CreateInventoryRequest {
        private Long productId;
        private Long locationId;
        private Integer quantity;
        private Integer reservedQuantity = 0;
        private InventoryStatus status = InventoryStatus.AVAILABLE;
        private String qrCode;
        private String lotNumber;
        private String batchNumber;
        private String serialNumber;
        private java.time.LocalDate receivedDate;
        private java.time.LocalDate expiryDate;
        private java.time.LocalDate manufactureDate;
        private BigDecimal unitCost;
        private String supplierReference;
        private String manufacturer;
        private String purchaseOrderNumber;
        private String notes;
        private Integer temperature;
        private Integer humidity;
        private Integer conditionRating = 10;
        private Boolean quarantine = false;
        private String quarantineReason;
        private Boolean hold = false;
        private String holdReason;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getReservedQuantity() { return reservedQuantity; }
        public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
        public InventoryStatus getStatus() { return status; }
        public void setStatus(InventoryStatus status) { this.status = status; }
        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
        public String getLotNumber() { return lotNumber; }
        public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public java.time.LocalDate getReceivedDate() { return receivedDate; }
        public void setReceivedDate(java.time.LocalDate receivedDate) { this.receivedDate = receivedDate; }
        public java.time.LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(java.time.LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public java.time.LocalDate getManufactureDate() { return manufactureDate; }
        public void setManufactureDate(java.time.LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
        public String getSupplierReference() { return supplierReference; }
        public void setSupplierReference(String supplierReference) { this.supplierReference = supplierReference; }
        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
        public String getPurchaseOrderNumber() { return purchaseOrderNumber; }
        public void setPurchaseOrderNumber(String purchaseOrderNumber) { this.purchaseOrderNumber = purchaseOrderNumber; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Integer getTemperature() { return temperature; }
        public void setTemperature(Integer temperature) { this.temperature = temperature; }
        public Integer getHumidity() { return humidity; }
        public void setHumidity(Integer humidity) { this.humidity = humidity; }
        public Integer getConditionRating() { return conditionRating; }
        public void setConditionRating(Integer conditionRating) { this.conditionRating = conditionRating; }
        public Boolean getQuarantine() { return quarantine; }
        public void setQuarantine(Boolean quarantine) { this.quarantine = quarantine; }
        public String getQuarantineReason() { return quarantineReason; }
        public void setQuarantineReason(String quarantineReason) { this.quarantineReason = quarantineReason; }
        public Boolean getHold() { return hold; }
        public void setHold(Boolean hold) { this.hold = hold; }
        public String getHoldReason() { return holdReason; }
        public void setHoldReason(String holdReason) { this.holdReason = holdReason; }
    }

    public static class UpdateInventoryRequest {
        private Long locationId;
        private Integer quantity;
        private Integer reservedQuantity;
        private InventoryStatus status;
        private String lotNumber;
        private String batchNumber;
        private String serialNumber;
        private java.time.LocalDate receivedDate;
        private java.time.LocalDate expiryDate;
        private java.time.LocalDate manufactureDate;
        private BigDecimal unitCost;
        private String supplierReference;
        private String manufacturer;
        private String purchaseOrderNumber;
        private String notes;
        private Integer temperature;
        private Integer humidity;
        private Integer conditionRating;
        private Boolean quarantine;
        private String quarantineReason;
        private Boolean hold;
        private String holdReason;

        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getReservedQuantity() { return reservedQuantity; }
        public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
        public InventoryStatus getStatus() { return status; }
        public void setStatus(InventoryStatus status) { this.status = status; }
        public String getLotNumber() { return lotNumber; }
        public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public java.time.LocalDate getReceivedDate() { return receivedDate; }
        public void setReceivedDate(java.time.LocalDate receivedDate) { this.receivedDate = receivedDate; }
        public java.time.LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(java.time.LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public java.time.LocalDate getManufactureDate() { return manufactureDate; }
        public void setManufactureDate(java.time.LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
        public String getSupplierReference() { return supplierReference; }
        public void setSupplierReference(String supplierReference) { this.supplierReference = supplierReference; }
        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
        public String getPurchaseOrderNumber() { return purchaseOrderNumber; }
        public void setPurchaseOrderNumber(String purchaseOrderNumber) { this.purchaseOrderNumber = purchaseOrderNumber; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Integer getTemperature() { return temperature; }
        public void setTemperature(Integer temperature) { this.temperature = temperature; }
        public Integer getHumidity() { return humidity; }
        public void setHumidity(Integer humidity) { this.humidity = humidity; }
        public Integer getConditionRating() { return conditionRating; }
        public void setConditionRating(Integer conditionRating) { this.conditionRating = conditionRating; }
        public Boolean getQuarantine() { return quarantine; }
        public void setQuarantine(Boolean quarantine) { this.quarantine = quarantine; }
        public String getQuarantineReason() { return quarantineReason; }
        public void setQuarantineReason(String quarantineReason) { this.quarantineReason = quarantineReason; }
        public Boolean getHold() { return hold; }
        public void setHold(Boolean hold) { this.hold = hold; }
        public String getHoldReason() { return holdReason; }
        public void setHoldReason(String holdReason) { this.holdReason = holdReason; }
    }

    public static class QuantityUpdateRequest {
        private Integer quantity;
        private String reason;
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}