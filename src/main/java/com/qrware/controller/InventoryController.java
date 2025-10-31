package com.qrware.controller;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.InventoryStatus;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.inventory.MovementHistoryRepository;
import com.qrware.exception.ResourceNotFoundException;

// --- NOWE IMPORTY ---
import com.qrware.dto.DTOMapper;
import com.qrware.dto.InventoryItemDTO;
// --- ---

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
import java.util.stream.Collectors; // Import dla stream().map()

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = {"http://localhost:3000", "http://10.0.2.2:8080"})
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryItemRepository inventoryRepository;

    @Autowired
    private MovementHistoryRepository movementHistoryRepository;

    // --- WSTRZYKNIJ SWÓJ MAPPER ---
    @Autowired
    private DTOMapper dtoMapper;

    // Pobierz wszystkie pozycje z paginacją
    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    // --- ZMIEŃ TYP ZWRACANY NA PAGE<INVENTORYITEMDTO> ---
    public ResponseEntity<Page<InventoryItemDTO>> getAllInventoryItems(Pageable pageable) {

        logger.info("GET /api/inventory - Strona: {}, Rozmiar: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // 1. Pobierz w pełni załadowane encje (dzięki @EntityGraph w repo)
        Page<InventoryItem> itemsPage = inventoryRepository.findAll(pageable);

        // 2. Zmapuj Page<Encja> na Page<DTO> używając swojego mappera
        Page<InventoryItemDTO> dtoPage = itemsPage.map(dtoMapper::toDTO);

        logger.info("Pomyślnie pobrano i zmapowano {} pozycji.", dtoPage.getNumberOfElements());

        // 3. Zwróć stronę DTO
        return ResponseEntity.ok(dtoPage);
    }

    // Pobierz pozycję po ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    // --- ZMIEŃ TYP ZWRACANY NA INVENTORYITEMDTO ---
    public ResponseEntity<InventoryItemDTO> getInventoryItemById(@PathVariable Long id) {
        logger.info("GET /api/inventory/{} - Pobieranie pozycji po ID.", id);

        // Użyj metody findById, która ma @EntityGraph
        Optional<InventoryItem> item = inventoryRepository.findById(id);

        if (item.isPresent()) {
            logger.info("Znaleziono pozycję dla ID: {}", id);
            // Zmapuj pojedynczy wynik do DTO i zwróć
            return ResponseEntity.ok(dtoMapper.toDTO(item.get()));
        }

        logger.warn("Nie znaleziono pozycji dla ID: {}", id);
        throw new ResourceNotFoundException("Inventory item", "id", id);
    }

    // Pobierz pozycje po produktie
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    // --- ZMIEŃ TYP ZWRACANY NA LIST<INVENTORYITEMDTO> ---
    public ResponseEntity<List<InventoryItemDTO>> getInventoryByProduct(@PathVariable Long productId) {
        logger.info("GET /api/inventory/product/{}", productId);
        List<InventoryItem> items = inventoryRepository.findByProductId(productId);
        List<InventoryItemDTO> dtos = items.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Pobierz pozycje po lokalizacji
    @GetMapping("/location/{locationId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    // --- ZMIEŃ TYP ZWRACANY NA LIST<INVENTORYITEMDTO> ---
    public ResponseEntity<List<InventoryItemDTO>> getInventoryByLocation(@PathVariable Long locationId) {
        logger.info("GET /api/inventory/location/{}", locationId);
        List<InventoryItem> items = inventoryRepository.findByLocationId(locationId);
        List<InventoryItemDTO> dtos = items.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Pobierz pozycje po statusie
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    // --- ZMIEŃ TYP ZWRACANY NA LIST<INVENTORYITEMDTO> ---
    public ResponseEntity<List<InventoryItemDTO>> getInventoryByStatus(@PathVariable InventoryStatus status) {
        logger.info("GET /api/inventory/status/{}", status);
        List<InventoryItem> items = inventoryRepository.findByStatus(status);
        List<InventoryItemDTO> dtos = items.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Pozycje z niskim stanem
    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    // --- ZMIEŃ TYP ZWRACANY NA LIST<INVENTORYITEMDTO> ---
    public ResponseEntity<List<InventoryItemDTO>> getLowStockItems() {
        logger.info("GET /api/inventory/low-stock");
        List<InventoryItem> items = inventoryRepository.findLowStockItems();
        List<InventoryItemDTO> dtos = items.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // --- Metody POST/PUT/DELETE ---
    // Dobrą praktyką jest również zwracanie DTO po utworzeniu/aktualizacji

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    // --- ZMIEŃ TYP ZWRACANY NA INVENTORYITEMDTO ---
    public ResponseEntity<InventoryItemDTO> createInventoryItem(@Valid @RequestBody CreateInventoryRequest request) {
        logger.info("POST /api/inventory - Tworzenie nowej pozycji.");

        InventoryItem item = new InventoryItem();
        // TODO: Ustaw Product i Location na podstawie ID z requestu
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
        logger.info("Utworzono nową pozycję z ID: {}", savedItem.getId());

        // Zwróć DTO (musimy ponownie wczytać z findById, aby dociągnąć relacje dla mappera)
        InventoryItem reloadedItem = inventoryRepository.findById(savedItem.getId()).orElse(savedItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toDTO(reloadedItem));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    // --- ZMIEŃ TYP ZWRACANY NA INVENTORYITEMDTO ---
    public ResponseEntity<InventoryItemDTO> updateInventoryItem(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateInventoryRequest request) {
        logger.info("PUT /api/inventory/{} - Aktualizacja pozycji.", id);
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", "id", id));

        // TODO: Dodać update dla Location
        if (request.getQuantity() != null) item.setQuantity(request.getQuantity().intValue());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        if (request.getSerialNumber() != null) item.setSerialNumber(request.getSerialNumber());
        // ... (reszta pól) ...

        InventoryItem updatedItem = inventoryRepository.save(item);

        // Zwróć DTO (musimy ponownie wczytać z findById, aby dociągnąć relacje dla mappera)
        InventoryItem reloadedItem = inventoryRepository.findById(updatedItem.getId()).orElse(updatedItem);
        return ResponseEntity.ok(dtoMapper.toDTO(reloadedItem));
    }

    // Usuń pozycję
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

    // Aktualizuj ilość - przyjęcie towaru
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    // --- ZMIEŃ TYP ZWRACANY NA INVENTORYITEMDTO ---
    public ResponseEntity<InventoryItemDTO> receiveStock(@PathVariable Long id,
                                                         @RequestBody QuantityUpdateRequest request) {
        logger.info("POST /api/inventory/{}/receive - Przyjęcie towaru.", id);
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", "id", id));

        Integer newQuantity = item.getQuantity() + request.getQuantity().intValue();
        item.setQuantity(newQuantity);

        InventoryItem updatedItem = inventoryRepository.save(item);

        // Zwróć DTO
        InventoryItem reloadedItem = inventoryRepository.findById(updatedItem.getId()).orElse(updatedItem);
        return ResponseEntity.ok(dtoMapper.toDTO(reloadedItem));
    }

    // Aktualizuj ilość - wydanie towaru
    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    // --- ZMIEŃ TYP ZWRACANY NA INVENTORYITEMDTO ---
    public ResponseEntity<InventoryItemDTO> issueStock(@PathVariable Long id,
                                                       @RequestBody QuantityUpdateRequest request) {
        logger.info("POST /api/inventory/{}/issue - Wydanie towaru.", id);
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", "id", id));

        Integer newQuantity = item.getQuantity() - request.getQuantity().intValue();

        if (newQuantity < 0) {
            logger.warn("Próba wydania większej ilości towaru niż dostępna dla ID: {}", id);
            return ResponseEntity.badRequest().build(); // TODO: Lepsza obsługa błędu
        }

        item.setQuantity(newQuantity);
        InventoryItem updatedItem = inventoryRepository.save(item);

        // Zwróć DTO
        InventoryItem reloadedItem = inventoryRepository.findById(updatedItem.getId()).orElse(updatedItem);
        return ResponseEntity.ok(dtoMapper.toDTO(reloadedItem));
    }


    // --- DTOs (pozostają bez zmian) ---
    public static class CreateInventoryRequest {
        // ... (zawartość bez zmian)
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
        // ... (zawartość bez zmian)
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
        // ... (zawartość bez zmian)
        private BigDecimal quantity;
        private String reason;
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}