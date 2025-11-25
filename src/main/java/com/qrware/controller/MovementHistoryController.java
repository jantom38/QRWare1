package com.qrware.controller;

import com.qrware.domain.inventory.MovementHistory;
import com.qrware.domain.inventory.MovementType;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.MovementHistoryDTO;
import com.qrware.service.MovementHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movement-history")
@Tag(name = "Movement History", description = "Zarządzanie historią ruchów magazynowych")
public class MovementHistoryController {

    @Autowired
    private MovementHistoryService movementHistoryService;

    @Autowired
    private DTOMapper dtoMapper;

    @GetMapping("/inventory-item/{itemId}")
    @Operation(summary = "Pobierz historię ruchów dla pozycji magazynowej")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getMovementHistoryByItemId(
            @Parameter(description = "ID pozycji magazynowej") @PathVariable Long itemId) {

        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByItemId(itemId);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Pobierz historię ruchów dla produktu")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getMovementHistoryByProductId(
            @Parameter(description = "ID produktu") @PathVariable Long productId) {

        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByProductId(productId);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Pobierz historię ruchów dla lokalizacji")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getMovementHistoryByLocationId(
            @Parameter(description = "ID lokalizacji") @PathVariable Long locationId) {

        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByLocationId(locationId);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/type/{movementType}")
    @Operation(summary = "Pobierz historię ruchów według typu")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getMovementHistoryByType(
            @Parameter(description = "Typ ruchu") @PathVariable String movementType) {

        try {
            MovementType type = MovementType.valueOf(movementType.toUpperCase());
            List<MovementHistory> movements = movementHistoryService.getMovementHistoryByType(type);
            List<MovementHistoryDTO> movementDTOs = movements.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(movementDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/date-range")
    @Operation(summary = "Pobierz historię ruchów z zakresu dat")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getMovementHistoryByDateRange(
            @Parameter(description = "Data początkowa (ISO format)")
            @RequestParam String startDate,
            @Parameter(description = "Data końcowa (ISO format)")
            @RequestParam String endDate) {

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<MovementHistory> movements = movementHistoryService.getMovementHistoryByDateRange(start, end);
            List<MovementHistoryDTO> movementDTOs = movements.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(movementDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/recent")
    @Operation(summary = "Pobierz ostatnie ruchy")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getRecentMovements(
            @Parameter(description = "Limit wyników") @RequestParam(defaultValue = "50") int limit) {

        List<MovementHistory> movements = movementHistoryService.getRecentMovements(limit);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/pending-approval")
    @Operation(summary = "Pobierz ruchy oczekujące na zatwierdzenie")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getPendingApprovalMovements() {

        List<MovementHistory> movements = movementHistoryService.getPendingApprovalMovements();
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/inbound")
    @Operation(summary = "Pobierz ruchy przychodzące")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getInboundMovements(
            @Parameter(description = "Limit wyników") @RequestParam(required = false) Integer limit) {

        List<MovementHistory> movements = movementHistoryService.getInboundMovements();
        if (limit != null && limit > 0) {
            movements = movements.stream().limit(limit).collect(Collectors.toList());
        }

        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/outbound")
    @Operation(summary = "Pobierz ruchy wychodzące")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getOutboundMovements(
            @Parameter(description = "Limit wyników") @RequestParam(required = false) Integer limit) {

        List<MovementHistory> movements = movementHistoryService.getOutboundMovements();
        if (limit != null && limit > 0) {
            movements = movements.stream().limit(limit).collect(Collectors.toList());
        }

        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/adjustments")
    @Operation(summary = "Pobierz korekty")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getAdjustmentMovements(
            @Parameter(description = "Limit wyników") @RequestParam(required = false) Integer limit) {

        List<MovementHistory> movements = movementHistoryService.getAdjustmentMovements();
        if (limit != null && limit > 0) {
            movements = movements.stream().limit(limit).collect(Collectors.toList());
        }

        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/search")
    @Operation(summary = "Wyszukaj ruchy")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> searchMovements(
            @Parameter(description = "Słowo kluczowe") @RequestParam String keyword,
            @Parameter(description = "Gdzie szukać: reason, notes, both")
            @RequestParam(defaultValue = "reason") String searchIn) {

        List<MovementHistory> movements = movementHistoryService.searchMovements(keyword, searchIn);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @PutMapping("/{movementId}/approve")
    @Operation(summary = "Zatwierdź ruch")
    @PreAuthorize("hasAuthority('MOVEMENT_WRITE')")
    public ResponseEntity<MovementHistoryDTO> approveMovement(
            @Parameter(description = "ID ruchu") @PathVariable Long movementId,
            @RequestBody ApprovalRequest request) {

        try {
            MovementHistory approvedMovement = movementHistoryService.approveMovement(
                    movementId,
                    request.getApproverComment()
            );

            MovementHistoryDTO movementDTO = convertToDTO(approvedMovement);

            return ResponseEntity.ok(movementDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{movementId}")
    @Operation(summary = "Pobierz szczegóły ruchu")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<MovementHistoryDTO> getMovementById(
            @Parameter(description = "ID ruchu") @PathVariable Long movementId) {

        try {
            MovementHistory movement = movementHistoryService.getMovementById(movementId);
            MovementHistoryDTO movementDTO = convertToDTO(movement);

            return ResponseEntity.ok(movementDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/requiring-attention")
    @Operation(summary = "Pobierz ruchy wymagające uwagi")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getMovementsRequiringAttention() {

        List<MovementHistory> movements = movementHistoryService.getMovementsRequiringAttention();
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/audit-trail")
    @Operation(summary = "Pobierz ścieżkę audytową")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getAuditTrail(
            @Parameter(description = "Data początkowa") @RequestParam String startDate,
            @Parameter(description = "Data końcowa") @RequestParam String endDate) {

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<MovementHistory> movements = movementHistoryService.getAuditTrail(start, end);
            List<MovementHistoryDTO> movementDTOs = movements.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(movementDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/velocity")
    @Operation(summary = "Pobierz prędkość ruchów (ruchy na godzinę)")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<Double> getMovementVelocity(
            @Parameter(description = "Data początkowa") @RequestParam String startDate,
            @Parameter(description = "Data końcowa") @RequestParam String endDate) {

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            Double velocity = movementHistoryService.getMovementVelocity(start, end);

            return ResponseEntity.ok(velocity != null ? velocity : 0.0);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats/by-type")
    @Operation(summary = "Pobierz statystyki ruchów według typu")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<Map<String, Object>> getMovementStatsByType() {

        Map<String, Object> stats = movementHistoryService.getMovementStatsByType();

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/by-date")
    @Operation(summary = "Pobierz statystyki ruchów według dat")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<Map<String, Object>> getMovementStatsByDate(
            @Parameter(description = "Data początkowa") @RequestParam String startDate,
            @Parameter(description = "Data końcowa") @RequestParam String endDate) {

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            Map<String, Object> stats = movementHistoryService.getMovementStatsByDate(start, end);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/environmental-data")
    @Operation(summary = "Pobierz ruchy z danymi środowiskowymi")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getMovementsWithEnvironmentalData() {

        List<MovementHistory> movements = movementHistoryService.getMovementsWithEnvironmentalData();
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Pobierz ruchy według ID partii")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getMovementsByBatchId(
            @Parameter(description = "ID partii") @PathVariable String batchId) {

        List<MovementHistory> movements = movementHistoryService.getMovementsByBatchId(batchId);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Pobierz ruchy według numeru referencyjnego")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<List<MovementHistoryDTO>> getMovementsByReferenceNumber(
            @Parameter(description = "Numer referencyjny") @PathVariable String referenceNumber) {

        List<MovementHistory> movements = movementHistoryService.getMovementsByReferenceNumber(referenceNumber);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @GetMapping("/summary")
    @Operation(summary = "Pobierz podsumowanie ruchów")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<Map<String, Long>> getMovementCountSummary() {

        Map<String, Long> summary = movementHistoryService.getMovementCountSummary();

        return ResponseEntity.ok(summary);
    }

    /**
     * Convert MovementHistory entity to DTO
     */
    private MovementHistoryDTO convertToDTO(MovementHistory movement) {
        return dtoMapper.toMovementHistoryDTO(movement);
    }

    /**
     * Request class for approval
     */
    public static class ApprovalRequest {
        private String approverComment;

        public String getApproverComment() {
            return approverComment;
        }

        public void setApproverComment(String approverComment) {
            this.approverComment = approverComment;
        }
    }

}