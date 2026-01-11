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

    public MovementHistory createMovementHistory(Long inventoryItemId, MovementType movementType,
                                                 Integer quantityBefore, Integer quantityAfter,
                                                 Location fromLocation, Location toLocation, String reason) {
        InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + inventoryItemId));

        MovementHistory movement = new MovementHistory();
        movement.setInventoryItem(inventoryItem);
        movement.setMovementType(movementType);
        movement.setMovementDate(LocalDateTime.now());
        movement.setQuantityBefore(quantityBefore);
        movement.setQuantityAfter(quantityAfter);
        
        if (quantityBefore != null && quantityAfter != null) {
            movement.setQuantityChanged(quantityAfter - quantityBefore);
        } else if (quantityAfter != null) {
            if (movementType.decreasesQuantity()) {
                movement.setQuantityChanged(-quantityAfter);
            } else {
                movement.setQuantityChanged(quantityAfter);
            }
        } else {
            movement.setQuantityChanged(0);
        }
        
        movement.setFromLocation(fromLocation);
        movement.setToLocation(toLocation);
        movement.setReason(reason);

        String currentUser = SecurityUtils.getCurrentUsername().orElse("system");
        movement.setUserId(currentUser);
        movement.setUserName(currentUser);

        movement.setApproved(!movement.requiresApproval());
        movement.setSystemGenerated(false);

        return movementHistoryRepository.save(movement);
    }

    public MovementHistory createSystemMovement(Long inventoryItemId, MovementType movementType,
                                                Integer quantityBefore, Integer quantityAfter,
                                                Location fromLocation, Location toLocation,
                                                String reason, String referenceNumber) {
        MovementHistory movement = createMovementHistory(inventoryItemId, movementType,
                quantityBefore, quantityAfter,
                fromLocation, toLocation, reason);
        movement.setSystemGenerated(true);
        movement.setReferenceNumber(referenceNumber);
        movement.setApproved(true);

        return movementHistoryRepository.save(movement);
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByItemId(Long itemId) {
        return movementHistoryRepository.findByInventoryItemId(itemId);
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByProductId(Long productId) {
        return movementHistoryRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByLocationId(Long locationId) {
        return movementHistoryRepository.findByLocationId(locationId);
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByType(MovementType movementType) {
        return movementHistoryRepository.findByMovementTypeOrderByMovementDateDesc(movementType);
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return movementHistoryRepository.findByMovementDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getRecentMovements(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return movementHistoryRepository.findRecentMovements().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getPendingApprovalMovements() {
        return movementHistoryRepository.findPendingApprovalMovements();
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getInboundMovements() {
        return movementHistoryRepository.findInboundMovements();
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getOutboundMovements() {
        return movementHistoryRepository.findOutboundMovements();
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getAdjustmentMovements() {
        return movementHistoryRepository.findAdjustmentMovements();
    }

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

    public MovementHistory approveMovement(Long movementId, String approverComment) {
        MovementHistory movement = movementHistoryRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found with id: " + movementId));

        if (movement.getApproved()) {
            throw new IllegalStateException("Movement is already approved");
        }

        String currentUser = SecurityUtils.getCurrentUsername().orElse("system");
        movement.approve(currentUser, currentUser);

        if (approverComment != null && !approverComment.trim().isEmpty()) {
            movement.setNotes(movement.getNotes() != null ?
                    movement.getNotes() + "\nApprover comment: " + approverComment :
                    "Approver comment: " + approverComment);
        }

        return movementHistoryRepository.save(movement);
    }

    @Transactional(readOnly = true)
    public MovementHistory getMovementById(Long movementId) {
        return movementHistoryRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found with id: " + movementId));
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementsRequiringAttention() {
        return movementHistoryRepository.getMovementsRequiringAttention();
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getAuditTrail(LocalDateTime startDate, LocalDateTime endDate) {
        return movementHistoryRepository.getAuditTrail(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Double getMovementVelocity(LocalDateTime startDate, LocalDateTime endDate) {
        return movementHistoryRepository.getMovementVelocity(startDate, endDate);
    }

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

    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementsWithEnvironmentalData() {
        return movementHistoryRepository.findMovementsWithEnvironmentalData();
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementsByBatchId(String batchId) {
        return movementHistoryRepository.findByBatchIdOrderByMovementDateDesc(batchId);
    }

    @Transactional(readOnly = true)
    public List<MovementHistory> getMovementsByReferenceNumber(String referenceNumber) {
        return movementHistoryRepository.findByReferenceNumberOrderByMovementDateDesc(referenceNumber);
    }

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