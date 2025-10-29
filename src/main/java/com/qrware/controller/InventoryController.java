package com.qrware.controller;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.InventoryStatus;
import com.qrware.domain.inventory.MovementType;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.inventory.MovementHistoryRepository;
import com.qrware.exception.ResourceNotFoundException;
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

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = {"http://localhost:3000", "http://10.0.2.2:8080"})
public class InventoryController {

    @Autowired
    private InventoryItemRepository inventoryRepository;

    @Autowired
    private MovementHistoryRepository movementHistoryRepository;

    // Pobierz wszystkie pozycje z paginacją
    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<Page<InventoryItem>> getAllInventoryItems(Pageable pageable) {
        Page<InventoryItem> items = inventoryRepository.findAll(pageable);
        return ResponseEntity.ok(items);
    }

    // Pobierz pozycję po ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<InventoryItem> getInventoryItemById(@PathVariable Long id) {
        Optional<InventoryItem> item = inventoryRepository.findById(id);
        if (item.isPresent()) {
            return ResponseEntity.ok(item.get());
        }
        throw new ResourceNotFoundException("Inventory item", "id", id);
    }

    // Pobierz pozycje po produktie
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryItem>> getInventoryByProduct(@PathVariable Long productId) {
        List<InventoryItem> items = inventoryRepository.findByProductId(productId);
        return ResponseEntity.ok(items);
    }

    // Pobierz pozycje po lokalizacji
    @GetMapping("/location/{locationId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryItem>> getInventoryByLocation(@PathVariable Long locationId) {
        List<InventoryItem> items = inventoryRepository.findByLocationId(locationId);
        return ResponseEntity.ok(items);
    }

    // Pobierz pozycje po statusie
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryItem>> getInventoryByStatus(@PathVariable InventoryStatus status) {
        List<InventoryItem> items = inventoryRepository.findByStatus(status);
        return ResponseEntity.ok(items);
    }

    // Dodaj nową pozycję
    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<InventoryItem> createInventoryItem(@Valid @RequestBody CreateInventoryRequest request) {
        InventoryItem item = new InventoryItem();
        // TODO: Pobierz Product i Location z bazy danych
        // item.setProduct(productRepository.findById(request.getProductId()).orElseThrow());
        // item.setLocation(locationRepository.findById(request.getLocationId()).orElseThrow());
        item.setQuantity(request.getQuantity().intValue());
        item.setStatus(request.getStatus());
        item.setSerialNumber(request.getSerialNumber());
        item.setBatchNumber(request.getBatchNumber());
        item.setLotNumber(request.getLotNumber());
        item.setExpiryDate(request.getExpirationDate());
        item.setQrCode(request.getQrCode());

        InventoryItem savedItem = inventoryRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    // Aktualizuj pozycję
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<InventoryItem> updateInventoryItem(@PathVariable Long id, 
                                                           @Valid @RequestBody UpdateInventoryRequest request) {
        Optional<InventoryItem> existingItem = inventoryRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("Inventory item", "id", id);
        }

        InventoryItem item = existingItem.get();
        // TODO: Dodać update dla Location gdy będzie ProductRepository i LocationRepository
        if (request.getQuantity() != null) item.setQuantity(request.getQuantity().intValue());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        if (request.getSerialNumber() != null) item.setSerialNumber(request.getSerialNumber());
        if (request.getBatchNumber() != null) item.setBatchNumber(request.getBatchNumber());
        if (request.getLotNumber() != null) item.setLotNumber(request.getLotNumber());
        if (request.getExpirationDate() != null) item.setExpiryDate(request.getExpirationDate());

        InventoryItem updatedItem = inventoryRepository.save(item);
        return ResponseEntity.ok(updatedItem);
    }

    // Usuń pozycję
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_DELETE')")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory item", "id", id);
        }
        inventoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Aktualizuj ilość - przyjęcie towaru
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<InventoryItem> receiveStock(@PathVariable Long id, 
                                                    @RequestBody QuantityUpdateRequest request) {
        Optional<InventoryItem> existingItem = inventoryRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("Inventory item", "id", id);
        }

        InventoryItem item = existingItem.get();
        Integer newQuantity = item.getQuantity() + request.getQuantity().intValue();
        item.setQuantity(newQuantity);

        InventoryItem updatedItem = inventoryRepository.save(item);
        return ResponseEntity.ok(updatedItem);
    }

    // Aktualizuj ilość - wydanie towaru
    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<InventoryItem> issueStock(@PathVariable Long id, 
                                                  @RequestBody QuantityUpdateRequest request) {
        Optional<InventoryItem> existingItem = inventoryRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("Inventory item", "id", id);
        }

        InventoryItem item = existingItem.get();
        Integer newQuantity = item.getQuantity() - request.getQuantity().intValue();
        
        if (newQuantity < 0) {
            return ResponseEntity.badRequest().build();
        }

        item.setQuantity(newQuantity);
        InventoryItem updatedItem = inventoryRepository.save(item);
        return ResponseEntity.ok(updatedItem);
    }

    // Pozycje z niskim stanem
    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        List<InventoryItem> items = inventoryRepository.findLowStockItems();
        return ResponseEntity.ok(items);
    }

    // DTOs
    public static class CreateInventoryRequest {
        private Long productId;
        private Long locationId;
        private BigDecimal quantity;
        private InventoryStatus status = InventoryStatus.AVAILABLE;
        private String qrCode;
        private String serialNumber;
        private String batchNumber;
        private String lotNumber;
        private java.time.LocalDate expirationDate;

        // Gettery i settery
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public InventoryStatus getStatus() { return status; }
        public void setStatus(InventoryStatus status) { this.status = status; }
        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        public String getLotNumber() { return lotNumber; }
        public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
        public java.time.LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(java.time.LocalDate expirationDate) { this.expirationDate = expirationDate; }
    }

    public static class UpdateInventoryRequest {
        private Long locationId;
        private BigDecimal quantity;
        private InventoryStatus status;
        private String serialNumber;
        private String batchNumber;
        private String lotNumber;
        private java.time.LocalDate expirationDate;

        // Gettery i settery
        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public InventoryStatus getStatus() { return status; }
        public void setStatus(InventoryStatus status) { this.status = status; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        public String getLotNumber() { return lotNumber; }
        public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
        public java.time.LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(java.time.LocalDate expirationDate) { this.expirationDate = expirationDate; }
    }

    public static class QuantityUpdateRequest {
        private BigDecimal quantity;
        private String reason;

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}