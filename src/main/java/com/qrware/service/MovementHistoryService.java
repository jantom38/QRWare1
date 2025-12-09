package com.qrware.service;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.MovementHistory;
import com.qrware.domain.inventory.MovementType;
import com.qrware.domain.warehouse.Location;
import com.qrware.dto.DTOMapper;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.inventory.MovementHistoryRepository;
import com.qrware.security.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class MovementHistoryService {

    @Autowired
    private MovementHistoryRepository movementHistoryRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private DTOMapper dtoMapper;

    /**
     * Create a new movement history record
     */
    public MovementHistory createMovementHistory(Long inventoryItemId, MovementType movementType,
                                                 Integer quantityBefore, Integer quantityAfter,
                                                 Location fromLocation, Location toLocation, String reason) {
        // BEZPIECZNE SPRAWDZENIE - sprawdzamy czy InventoryItem istnieje
        InventoryItem inventoryItem = null;
        if (inventoryItemId != null) {
            inventoryItem = inventoryItemRepository.findById(inventoryItemId).orElse(null);
            if (inventoryItem == null) {
                throw new ResourceNotFoundException("Inventory item not found with id: " + inventoryItemId);
            }
        }

        MovementHistory movement = new MovementHistory();
        movement.setInventoryItem(inventoryItem); // Może być null
        movement.setMovementType(movementType);
        movement.setMovementDate(LocalDateTime.now());
        movement.setQuantityBefore(quantityBefore);
        movement.setQuantityAfter(quantityAfter);
        movement.setQuantityChanged(quantityAfter != null && quantityBefore != null ?
                quantityAfter - quantityBefore : 0);
        movement.setFromLocation(fromLocation);
        movement.setToLocation(toLocation);
        movement.setReason(reason);

        // Set user information from security context
        // POPRAWKA: Obsługa Optional<String>
        String currentUser = SecurityUtils.getCurrentUsername().orElse("system");
        movement.setUserId(currentUser);
        movement.setUserName(currentUser);

        // Determine if approval is required
        movement.setApproved(!movement.requiresApproval());
        movement.setSystemGenerated(false);

        return movementHistoryRepository.save(movement);
    }

    /**
     * Create a system-generated movement history record
     */
    public MovementHistory createSystemMovement(Long inventoryItemId, MovementType movementType,
                                                Integer quantityBefore, Integer quantityAfter,
                                                Location fromLocation, Location toLocation,
                                                String reason, String referenceNumber) {
        MovementHistory movement = createMovementHistory(inventoryItemId, movementType,
                quantityBefore, quantityAfter,
                fromLocation, toLocation, reason);
        movement.setSystemGenerated(true);
        movement.setReferenceNumber(referenceNumber);
        movement.setApproved(true); // System movements are auto-approved

        return movementHistoryRepository.save(movement);
    }

    /**
     * Get movement history by inventory item ID
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByItemId(Long itemId) {
        return movementHistoryRepository.findByInventoryItemId(itemId);
    }

    /**
     * Get movement history by product ID
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByProductId(Long productId) {
        return movementHistoryRepository.findByProductId(productId);
    }

    /**
     * Get movement history by location ID
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByLocationId(Long locationId) {
        return movementHistoryRepository.findByLocationId(locationId);
    }

    /**
     * Get movement history by movement type
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByType(MovementType movementType) {
        return movementHistoryRepository.findByMovementTypeOrderByMovementDateDesc(movementType);
    }

    /**
     * Get movement history by date range
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return movementHistoryRepository.findByMovementDateBetween(startDate, endDate);
    }

    /**
     * Get recent movements with limit
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getRecentMovements(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return movementHistoryRepository.findRecentMovements().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get pending approval movements
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getPendingApprovalMovements() {
        return movementHistoryRepository.findPendingApprovalMovements();
    }

    /**
     * Get inbound movements
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getInboundMovements() {
        return movementHistoryRepository.findInboundMovements();
    }

    /**
     * Get outbound movements
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getOutboundMovements() {
        return movementHistoryRepository.findOutboundMovements();
    }

    /**
     * Get adjustment movements
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getAdjustmentMovements() {
        return movementHistoryRepository.findAdjustmentMovements();
    }

    /**
     * Search movements by reason or notes
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> searchMovements(String keyword, String searchIn) {
        switch (searchIn.toLowerCase()) {
            case "reason":
                return movementHistoryRepository.findByReasonContaining(keyword);
            case "notes":
                return movementHistoryRepository.findByNotesContaining(keyword);
            case "both":
                List<MovementHistory> reasonResults = movementHistoryRepository.findByReasonContaining(keyword);
                List<MovementHistory> notesResults = movementHistoryRepository.findByNotesContaining(keyword);
                return Stream.concat(reasonResults.stream(), notesResults.stream())
                        .distinct()
                        .collect(Collectors.toList());
            default:
                return movementHistoryRepository.findByReasonContaining(keyword);
        }
    }

    /**
     * Approve a movement
     */
    public MovementHistory approveMovement(Long movementId, String approverComment) {
        MovementHistory movement = movementHistoryRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found with id: " + movementId));

        if (movement.getApproved()) {
            throw new IllegalStateException("Movement is already approved");
        }

        // POPRAWKA: Obsługa Optional<String>
        String currentUser = SecurityUtils.getCurrentUsername().orElse("system");
        movement.approve(currentUser, currentUser);

        if (approverComment != null && !approverComment.trim().isEmpty()) {
            movement.setNotes(movement.getNotes() != null ?
                    movement.getNotes() + "\nApprover comment: " + approverComment :
                    "Approver comment: " + approverComment);
        }

        return movementHistoryRepository.save(movement);
    }

    /**
     * Get movement by ID
     */
    @Transactional(readOnly = true)
    public MovementHistory getMovementById(Long movementId) {
        return movementHistoryRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found with id: " + movementId));
    }

    /**
     * Get movements requiring attention
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementsRequiringAttention() {
        return movementHistoryRepository.getMovementsRequiringAttention();
    }

    /**
     * Get audit trail for compliance
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getAuditTrail(LocalDateTime startDate, LocalDateTime endDate) {
        return movementHistoryRepository.getAuditTrail(startDate, endDate);
    }

    /**
     * Get movement velocity (movements per hour)
     */
    @Transactional(readOnly = true)
    public Double getMovementVelocity(LocalDateTime startDate, LocalDateTime endDate) {
        return movementHistoryRepository.getMovementVelocity(startDate, endDate);
    }

    /**
     * Get movement statistics by type
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMovementStatsByType() {
        List<Object[]> stats = movementHistoryRepository.getMovementStatsByType();
        return stats.stream()
                .collect(Collectors.toMap(
                        stat -> ((MovementType) stat[0]).getDisplayName(),
                        stat -> Map.of(
                                "count", stat[1],
                                "totalQuantity", stat[2] != null ? stat[2] : 0
                        )
                ));
    }

    /**
     * Get movement statistics by date
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMovementStatsByDate(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> stats = movementHistoryRepository.getMovementStatsByDate(startDate, endDate);
        return stats.stream()
                .collect(Collectors.toMap(
                        stat -> stat[0].toString(),
                        stat -> Map.of(
                                "count", stat[1],
                                "totalQuantity", stat[2] != null ? stat[2] : 0
                        )
                ));
    }

    /**
     * Get movements with environmental data
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementsWithEnvironmentalData() {
        return movementHistoryRepository.findMovementsWithEnvironmentalData();
    }

    /**
     * Get movements by batch ID
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementsByBatchId(String batchId) {
        return movementHistoryRepository.findByBatchIdOrderByMovementDateDesc(batchId);
    }

    /**
     * Get movements by reference number
     */
    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementsByReferenceNumber(String referenceNumber) {
        return movementHistoryRepository.findByReferenceNumberOrderByMovementDateDesc(referenceNumber);
    }

    /**
     * Create movement history without specific inventory item (for order tracking)
     */
    public MovementHistory createOrderMovementHistory(MovementType movementType,
                                                     Integer quantity,
                                                     Location fromLocation, 
                                                     Location toLocation, 
                                                     String reason, 
                                                     String referenceNumber) {
        MovementHistory movement = new MovementHistory();
        movement.setInventoryItem(null); // Brak konkretnej pozycji magazynowej
        movement.setMovementType(movementType);
        movement.setMovementDate(LocalDateTime.now());
        movement.setQuantityBefore(null);
        movement.setQuantityAfter(null);
        movement.setQuantityChanged(quantity != null ? quantity : 0);
        movement.setFromLocation(fromLocation);
        movement.setToLocation(toLocation);
        movement.setReason(reason);
        movement.setReferenceNumber(referenceNumber);
        movement.setReferenceType("ORDER");

        // Set user information from security context
        String currentUser = SecurityUtils.getCurrentUsername().orElse("system");
        movement.setUserId(currentUser);
        movement.setUserName(currentUser);

        // Order movements are system generated and auto-approved
        movement.setSystemGenerated(true);
        movement.setApproved(true);

        return movementHistoryRepository.save(movement);
    }

    /**
     * Get movement count for dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getMovementCountSummary() {
        return Map.of(
                "total", movementHistoryRepository.count(),
                "pendingApproval", movementHistoryRepository.countPendingApprovalMovements(),
                "systemGenerated", movementHistoryRepository.countBySystemGeneratedTrue(),
                "userGenerated", movementHistoryRepository.countBySystemGeneratedFalse(),
                "today", movementHistoryRepository.countByMovementDateBetween(
                        LocalDate.now().atStartOfDay(),
                        LocalDate.now().atTime(23, 59, 59)
                )
        );
    }
}