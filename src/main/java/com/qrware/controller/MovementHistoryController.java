package com.qrware.controller;

import com.qrware.domain.inventory.MovementHistory;
import com.qrware.domain.inventory.MovementType;
import com.qrware.dto.ApiResponse;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.InventoryItemDTO;
import com.qrware.dto.MovementHistoryDTO;
import com.qrware.service.MovementHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getMovementHistoryByItemId(
            @Parameter(description = "ID pozycji magazynowej") @PathVariable Long itemId) {

        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByItemId(itemId);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Historia ruchów pobrana pomyślnie"
        ));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Pobierz historię ruchów dla produktu")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getMovementHistoryByProductId(
            @Parameter(description = "ID produktu") @PathVariable Long productId) {

        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByProductId(productId);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Historia ruchów produktu pobrana pomyślnie"
        ));
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Pobierz historię ruchów dla lokalizacji")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getMovementHistoryByLocationId(
            @Parameter(description = "ID lokalizacji") @PathVariable Long locationId) {

        List<MovementHistory> movements = movementHistoryService.getMovementHistoryByLocationId(locationId);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Historia ruchów lokalizacji pobrana pomyślnie"
        ));
    }

    @GetMapping("/type/{movementType}")
    @Operation(summary = "Pobierz historię ruchów według typu")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getMovementHistoryByType(
            @Parameter(description = "Typ ruchu") @PathVariable String movementType) {

        try {
            MovementType type = MovementType.valueOf(movementType.toUpperCase());
            List<MovementHistory> movements = movementHistoryService.getMovementHistoryByType(type);
            List<MovementHistoryDTO> movementDTOs = movements.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    movementDTOs,
                    "Historia ruchów typu " + type.getDisplayName() + " pobrana pomyślnie"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Nieprawidłowy typ ruchu: " + movementType)
            );
        }
    }

    @GetMapping("/date-range")
    @Operation(summary = "Pobierz historię ruchów z zakresu dat")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getMovementHistoryByDateRange(
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

            return ResponseEntity.ok(ApiResponse.success(
                    movementDTOs,
                    "Historia ruchów z zakresu dat pobrana pomyślnie"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Nieprawidłowy format daty. Użyj formatu ISO: yyyy-MM-ddTHH:mm:ss")
            );
        }
    }

    @GetMapping("/recent")
    @Operation(summary = "Pobierz ostatnie ruchy")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getRecentMovements(
            @Parameter(description = "Limit wyników") @RequestParam(defaultValue = "50") int limit) {

        List<MovementHistory> movements = movementHistoryService.getRecentMovements(limit);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Ostatnie ruchy pobrane pomyślnie"
        ));
    }

    @GetMapping("/pending-approval")
    @Operation(summary = "Pobierz ruchy oczekujące na zatwierdzenie")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getPendingApprovalMovements() {

        List<MovementHistory> movements = movementHistoryService.getPendingApprovalMovements();
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Ruchy oczekujące na zatwierdzenie pobrane pomyślnie"
        ));
    }

    @GetMapping("/inbound")
    @Operation(summary = "Pobierz ruchy przychodzące")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getInboundMovements(
            @Parameter(description = "Limit wyników") @RequestParam(required = false) Integer limit) {

        List<MovementHistory> movements = movementHistoryService.getInboundMovements();
        if (limit != null && limit > 0) {
            movements = movements.stream().limit(limit).collect(Collectors.toList());
        }

        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Ruchy przychodzące pobrane pomyślnie"
        ));
    }

    @GetMapping("/outbound")
    @Operation(summary = "Pobierz ruchy wychodzące")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getOutboundMovements(
            @Parameter(description = "Limit wyników") @RequestParam(required = false) Integer limit) {

        List<MovementHistory> movements = movementHistoryService.getOutboundMovements();
        if (limit != null && limit > 0) {
            movements = movements.stream().limit(limit).collect(Collectors.toList());
        }

        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Ruchy wychodzące pobrane pomyślnie"
        ));
    }

    @GetMapping("/adjustments")
    @Operation(summary = "Pobierz korekty")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getAdjustmentMovements(
            @Parameter(description = "Limit wyników") @RequestParam(required = false) Integer limit) {

        List<MovementHistory> movements = movementHistoryService.getAdjustmentMovements();
        if (limit != null && limit > 0) {
            movements = movements.stream().limit(limit).collect(Collectors.toList());
        }

        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Korekty pobrane pomyślnie"
        ));
    }

    @GetMapping("/search")
    @Operation(summary = "Wyszukaj ruchy")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> searchMovements(
            @Parameter(description = "Słowo kluczowe") @RequestParam String keyword,
            @Parameter(description = "Gdzie szukać: reason, notes, both")
            @RequestParam(defaultValue = "reason") String searchIn) {

        List<MovementHistory> movements = movementHistoryService.searchMovements(keyword, searchIn);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Wyniki wyszukiwania dla: " + keyword
        ));
    }

    @PutMapping("/{movementId}/approve")
    @Operation(summary = "Zatwierdź ruch")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovementHistoryDTO>> approveMovement(
            @Parameter(description = "ID ruchu") @PathVariable Long movementId,
            @RequestBody ApprovalRequest request) {

        try {
            MovementHistory approvedMovement = movementHistoryService.approveMovement(
                    movementId,
                    request.getApproverComment()
            );

            MovementHistoryDTO movementDTO = convertToDTO(approvedMovement);

            return ResponseEntity.ok(ApiResponse.success(
                    movementDTO,
                    "Ruch został zatwierdzony pomyślnie"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Błąd podczas zatwierdzania ruchu: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{movementId}")
    @Operation(summary = "Pobierz szczegóły ruchu")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovementHistoryDTO>> getMovementById(
            @Parameter(description = "ID ruchu") @PathVariable Long movementId) {

        try {
            MovementHistory movement = movementHistoryService.getMovementById(movementId);
            MovementHistoryDTO movementDTO = convertToDTO(movement);

            return ResponseEntity.ok(ApiResponse.success(
                    movementDTO,
                    "Szczegóły ruchu pobrane pomyślnie"
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/requiring-attention")
    @Operation(summary = "Pobierz ruchy wymagające uwagi")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getMovementsRequiringAttention() {

        List<MovementHistory> movements = movementHistoryService.getMovementsRequiringAttention();
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Ruchy wymagające uwagi pobrane pomyślnie"
        ));
    }

    @GetMapping("/audit-trail")
    @Operation(summary = "Pobierz ścieżkę audytową")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getAuditTrail(
            @Parameter(description = "Data początkowa") @RequestParam String startDate,
            @Parameter(description = "Data końcowa") @RequestParam String endDate) {

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<MovementHistory> movements = movementHistoryService.getAuditTrail(start, end);
            List<MovementHistoryDTO> movementDTOs = movements.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    movementDTOs,
                    "Ścieżka audytowa pobrana pomyślnie"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Błąd podczas pobierania ścieżki audytowej: " + e.getMessage())
            );
        }
    }

    @GetMapping("/velocity")
    @Operation(summary = "Pobierz prędkość ruchów (ruchy na godzinę)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Double>> getMovementVelocity(
            @Parameter(description = "Data początkowa") @RequestParam String startDate,
            @Parameter(description = "Data końcowa") @RequestParam String endDate) {

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            Double velocity = movementHistoryService.getMovementVelocity(start, end);

            return ResponseEntity.ok(ApiResponse.success(
                    velocity != null ? velocity : 0.0,
                    "Prędkość ruchów obliczona pomyślnie"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Błąd podczas obliczania prędkości ruchów: " + e.getMessage())
            );
        }
    }

    @GetMapping("/stats/by-type")
    @Operation(summary = "Pobierz statystyki ruchów według typu")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMovementStatsByType() {

        Map<String, Object> stats = movementHistoryService.getMovementStatsByType();

        return ResponseEntity.ok(ApiResponse.success(
                stats,
                "Statystyki ruchów według typu pobrane pomyślnie"
        ));
    }

    @GetMapping("/stats/by-date")
    @Operation(summary = "Pobierz statystyki ruchów według dat")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMovementStatsByDate(
            @Parameter(description = "Data początkowa") @RequestParam String startDate,
            @Parameter(description = "Data końcowa") @RequestParam String endDate) {

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            Map<String, Object> stats = movementHistoryService.getMovementStatsByDate(start, end);

            return ResponseEntity.ok(ApiResponse.success(
                    stats,
                    "Statystyki ruchów według dat pobrane pomyślnie"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Błąd podczas pobierania statystyk: " + e.getMessage())
            );
        }
    }

    @GetMapping("/environmental-data")
    @Operation(summary = "Pobierz ruchy z danymi środowiskowymi")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getMovementsWithEnvironmentalData() {

        List<MovementHistory> movements = movementHistoryService.getMovementsWithEnvironmentalData();
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Ruchy z danymi środowiskowymi pobrane pomyślnie"
        ));
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Pobierz ruchy według ID partii")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getMovementsByBatchId(
            @Parameter(description = "ID partii") @PathVariable String batchId) {

        List<MovementHistory> movements = movementHistoryService.getMovementsByBatchId(batchId);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Ruchy partii pobrane pomyślnie"
        ));
    }

    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Pobierz ruchy według numeru referencyjnego")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MovementHistoryDTO>>> getMovementsByReferenceNumber(
            @Parameter(description = "Numer referencyjny") @PathVariable String referenceNumber) {

        List<MovementHistory> movements = movementHistoryService.getMovementsByReferenceNumber(referenceNumber);
        List<MovementHistoryDTO> movementDTOs = movements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                movementDTOs,
                "Ruchy z numerem referencyjnym pobrane pomyślnie"
        ));
    }

    @GetMapping("/summary")
    @Operation(summary = "Pobierz podsumowanie ruchów")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('WAREHOUSE_OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getMovementCountSummary() {

        Map<String, Long> summary = movementHistoryService.getMovementCountSummary();

        return ResponseEntity.ok(ApiResponse.success(
                summary,
                "Podsumowanie ruchów pobrane pomyślnie"
        ));
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