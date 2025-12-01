package com.qrware.controller;

import com.qrware.domain.inventory.MovementHistory;
import com.qrware.domain.inventory.MovementType;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.MovementHistoryDTO;
import com.qrware.service.MovementHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movement-history")
public class MovementHistoryController {

    @Autowired
    private MovementHistoryService movementHistoryService;

    @Autowired
    private DTOMapper dtoMapper;

    // --- PODSTAWOWE POBIERANIE DANYCH ---

    @GetMapping("/{id}")
    public ResponseEntity<MovementHistoryDTO> getById(@PathVariable Long id) {
        MovementHistory movement = movementHistoryService.getMovementById(id);
        return ResponseEntity.ok(dtoMapper.toMovementHistoryDTO(movement));
    }

    @GetMapping("/by-item/{itemId}")
    public ResponseEntity<List<MovementHistoryDTO>> getByInventoryItem(@PathVariable Long itemId) {
        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByItemId(itemId);
        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<MovementHistoryDTO>> getByProduct(@PathVariable Long productId) {
        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByProductId(productId);
        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }

    // --- PUNKT, W KTÓRYM WYSTĄPIŁ BŁĄD ---

    @GetMapping("/by-order/{orderReference}")
    public ResponseEntity<List<MovementHistoryDTO>> getByOrderReference(@PathVariable String orderReference) {
        // LINIA 59: Poprawiona metoda z getMovementHistoryByOrderReference (nieistniejącej)
        // na getMovementsByReferenceNumber (istniejącą)
        List<MovementHistory> movements = movementHistoryService.getMovementsByReferenceNumber(orderReference);

        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }

    // ----------------------------------------

    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<MovementHistoryDTO>> getByLocation(@PathVariable Long locationId) {
        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByLocationId(locationId);
        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<MovementHistoryDTO>> getByType(@PathVariable MovementType type) {
        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByType(type);
        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<MovementHistoryDTO>> getRecent(@RequestParam(defaultValue = "10") int limit) {
        List<MovementHistory> movements = movementHistoryService.getRecentMovements(limit);
        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }

    // --- OPERACJE WYSZUKIWANIA I FILTROWANIA ---

    @GetMapping("/search")
    public ResponseEntity<List<MovementHistoryDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "reason") String searchIn) {
        List<MovementHistory> movements = movementHistoryService.searchMovements(keyword, searchIn);
        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<MovementHistoryDTO>> getByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByDateRange(start, end);
        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/by-batch/{batchId}")
    public ResponseEntity<List<MovementHistoryDTO>> getByBatchId(@PathVariable String batchId) {
        List<MovementHistory> movements = movementHistoryService.getMovementsByBatchId(batchId);
        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }


    // --- ZARZĄDZANIE I STATYSTYKI ---

    @GetMapping("/pending-approval")
    public ResponseEntity<List<MovementHistoryDTO>> getPendingApproval() {
        List<MovementHistory> movements = movementHistoryService.getPendingApprovalMovements();
        return ResponseEntity.ok(movements.stream()
                .map(dtoMapper::toMovementHistoryDTO)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<MovementHistoryDTO> approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody) {

        String comment = requestBody != null ? requestBody.get("approverComment") : null;
        MovementHistory approvedMovement = movementHistoryService.approveMovement(id, comment);
        return ResponseEntity.ok(dtoMapper.toMovementHistoryDTO(approvedMovement));
    }

    @GetMapping("/statistics/count-summary")
    public ResponseEntity<Map<String, Long>> getMovementCountSummary() {
        return ResponseEntity.ok(movementHistoryService.getMovementCountSummary());
    }

    @GetMapping("/statistics/by-type")
    public ResponseEntity<Map<String, Object>> getMovementStatsByType() {
        return ResponseEntity.ok(movementHistoryService.getMovementStatsByType());
    }

    @GetMapping("/statistics/by-date")
    public ResponseEntity<Map<String, Object>> getMovementStatsByDate(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        return ResponseEntity.ok(movementHistoryService.getMovementStatsByDate(start, end));
    }
}