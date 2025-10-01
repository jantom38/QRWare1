package com.qrware.repository.inventory;

import com.qrware.domain.inventory.MovementHistory;
import com.qrware.domain.inventory.MovementType;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for MovementHistory entity operations
 */
@Repository
public interface MovementHistoryRepository extends BaseRepository<MovementHistory> {

    /**
     * Find movement history by inventory item ID
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.id = :itemId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByInventoryItemId(@Param("itemId") Long itemId);

    /**
     * Find movement history by inventory item QR code
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.qrCode = :qrCode ORDER BY m.movementDate DESC")
    List<MovementHistory> findByInventoryItemQrCode(@Param("qrCode") String qrCode);

    /**
     * Find movement history by product ID
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.product.id = :productId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByProductId(@Param("productId") Long productId);

    /**
     * Find movement history by product SKU
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.product.sku = :sku ORDER BY m.movementDate DESC")
    List<MovementHistory> findByProductSku(@Param("sku") String sku);

    /**
     * Find movement history by location ID
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.fromLocation.id = :locationId OR m.toLocation.id = :locationId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByLocationId(@Param("locationId") Long locationId);

    /**
     * Find movement history by from location
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.fromLocation.id = :locationId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByFromLocationId(@Param("locationId") Long locationId);

    /**
     * Find movement history by to location
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.toLocation.id = :locationId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByToLocationId(@Param("locationId") Long locationId);

    /**
     * Find movement history by zone ID
     */
    @Query("SELECT m FROM MovementHistory m WHERE " +
           "m.fromLocation.zone.id = :zoneId OR m.toLocation.zone.id = :zoneId OR m.inventoryItem.location.zone.id = :zoneId " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findByZoneId(@Param("zoneId") Long zoneId);

    /**
     * Find movement history by movement type
     */
    List<MovementHistory> findByMovementTypeOrderByMovementDateDesc(MovementType movementType);

    /**
     * Find movement history by multiple movement types
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN :types ORDER BY m.movementDate DESC")
    List<MovementHistory> findByMovementTypeIn(@Param("types") List<MovementType> types);

    /**
     * Find movement history by date range
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.movementDate BETWEEN :startDate AND :endDate ORDER BY m.movementDate DESC")
    List<MovementHistory> findByMovementDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find movement history after specific date
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.movementDate >= :date ORDER BY m.movementDate DESC")
    List<MovementHistory> findByMovementDateAfter(@Param("date") LocalDateTime date);

    /**
     * Find movement history by user ID
     */
    List<MovementHistory> findByUserIdOrderByMovementDateDesc(String userId);

    /**
     * Find movement history by user name
     */
    List<MovementHistory> findByUserNameOrderByMovementDateDesc(String userName);

    /**
     * Find movement history by reference number
     */
    List<MovementHistory> findByReferenceNumberOrderByMovementDateDesc(String referenceNumber);

    /**
     * Find movement history by reference type
     */
    List<MovementHistory> findByReferenceTypeOrderByMovementDateDesc(String referenceType);

    /**
     * Find movement history by batch ID
     */
    List<MovementHistory> findByBatchIdOrderByMovementDateDesc(String batchId);

    /**
     * Find pending approval movements
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.approved = false AND m.movementType IN " +
           "('DISPOSAL', 'SCRAP', 'ADJUSTMENT', 'LOSS', 'DAMAGE') ORDER BY m.movementDate ASC")
    List<MovementHistory> findPendingApprovalMovements();

    /**
     * Find approved movements
     */
    List<MovementHistory> findByApprovedTrueOrderByMovementDateDesc();

    /**
     * Find movements approved by specific user
     */
    List<MovementHistory> findByApprovedByOrderByApprovedDateDesc(String approvedBy);

    /**
     * Find system generated movements
     */
    List<MovementHistory> findBySystemGeneratedTrueOrderByMovementDateDesc();

    /**
     * Find user generated movements
     */
    List<MovementHistory> findBySystemGeneratedFalseOrderByMovementDateDesc();

    /**
     * Find movements with quantity changes
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.quantityChanged != 0 ORDER BY m.movementDate DESC")
    List<MovementHistory> findMovementsWithQuantityChanges();

    /**
     * Find positive quantity movements (receipts, adjustments up)
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.quantityChanged > 0 ORDER BY m.movementDate DESC")
    List<MovementHistory> findPositiveQuantityMovements();

    /**
     * Find negative quantity movements (issues, adjustments down)
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.quantityChanged < 0 ORDER BY m.movementDate DESC")
    List<MovementHistory> findNegativeQuantityMovements();

    /**
     * Find location movements (movements between locations)
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.fromLocation IS NOT NULL AND m.toLocation IS NOT NULL ORDER BY m.movementDate DESC")
    List<MovementHistory> findLocationMovements();

    /**
     * Find inbound movements
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('RECEIPT', 'RETURN', 'FOUND', 'RELEASE', 'UNHOLD', 'PRODUCTION', 'LOAN_RETURN') " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findInboundMovements();

    /**
     * Find outbound movements
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('ISSUE', 'SHIP', 'DISPOSAL', 'LOSS', 'SCRAP', 'CONSUMPTION', 'SAMPLE', 'LOAN') " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findOutboundMovements();

    /**
     * Find adjustment movements
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('ADJUSTMENT', 'CYCLE_COUNT', 'PHYSICAL_COUNT') ORDER BY m.movementDate DESC")
    List<MovementHistory> findAdjustmentMovements();

    /**
     * Find order related movements
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('RESERVE', 'PICK', 'PACK', 'SHIP', 'ALLOCATION', 'DEALLOCATION') " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findOrderRelatedMovements();

    /**
     * Find quality related movements
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('QUARANTINE', 'RELEASE', 'DAMAGE', 'EXPIRY', 'RECALL', 'REWORK', 'SAMPLE') " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findQualityRelatedMovements();

    /**
     * Find movements by reason containing keyword
     */
    @Query("SELECT m FROM MovementHistory m WHERE LOWER(m.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.movementDate DESC")
    List<MovementHistory> findByReasonContaining(@Param("keyword") String keyword);

    /**
     * Find movements by notes containing keyword
     */
    @Query("SELECT m FROM MovementHistory m WHERE LOWER(m.notes) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.movementDate DESC")
    List<MovementHistory> findByNotesContaining(@Param("keyword") String keyword);

    /**
     * Get movement statistics by type
     */
    @Query("SELECT m.movementType, COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "GROUP BY m.movementType ORDER BY COUNT(m) DESC")
    List<Object[]> getMovementStatsByType();

    /**
     * Get movement statistics by date range
     */
    @Query("SELECT DATE(m.movementDate), COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "WHERE m.movementDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(m.movementDate) ORDER BY DATE(m.movementDate)")
    List<Object[]> getMovementStatsByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Get movement statistics by user
     */
    @Query("SELECT m.userName, COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "WHERE m.userName IS NOT NULL " +
           "GROUP BY m.userName ORDER BY COUNT(m) DESC")
    List<Object[]> getMovementStatsByUser();

    /**
     * Get movement statistics by location
     */
    @Query("SELECT COALESCE(m.fromLocation.code, m.toLocation.code, 'Unknown'), COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "GROUP BY COALESCE(m.fromLocation.code, m.toLocation.code, 'Unknown') " +
           "ORDER BY COUNT(m) DESC")
    List<Object[]> getMovementStatsByLocation();

    /**
     * Get movement statistics by product
     */
    @Query("SELECT m.inventoryItem.product.sku, m.inventoryItem.product.name, COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "GROUP BY m.inventoryItem.product.id, m.inventoryItem.product.sku, m.inventoryItem.product.name " +
           "ORDER BY COUNT(m) DESC")
    List<Object[]> getMovementStatsByProduct();

    /**
     * Count movements by type
     */
    long countByMovementType(MovementType movementType);

    /**
     * Count movements by date range
     */
    @Query("SELECT COUNT(m) FROM MovementHistory m WHERE m.movementDate BETWEEN :startDate AND :endDate")
    long countByMovementDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count movements by user
     */
    long countByUserId(String userId);

    /**
     * Count pending approval movements
     */
    @Query("SELECT COUNT(m) FROM MovementHistory m WHERE m.approved = false AND m.movementType IN " +
           "('DISPOSAL', 'SCRAP', 'ADJUSTMENT', 'LOSS', 'DAMAGE')")
    long countPendingApprovalMovements();

    /**
     * Count system generated movements
     */
    long countBySystemGeneratedTrue();

    /**
     * Count user generated movements
     */
    long countBySystemGeneratedFalse();

    /**
     * Get total quantity moved by type
     */
    @Query("SELECT SUM(ABS(m.quantityChanged)) FROM MovementHistory m WHERE m.movementType = :type")
    Long getTotalQuantityMovedByType(@Param("type") MovementType type);

    /**
     * Get total quantity moved in date range
     */
    @Query("SELECT SUM(ABS(m.quantityChanged)) FROM MovementHistory m WHERE m.movementDate BETWEEN :startDate AND :endDate")
    Long getTotalQuantityMovedInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Get movements requiring attention (pending approval, errors)
     */
    @Query("SELECT m FROM MovementHistory m WHERE " +
           "(m.approved = false AND m.movementType IN ('DISPOSAL', 'SCRAP', 'ADJUSTMENT', 'LOSS', 'DAMAGE')) OR " +
           "(m.quantityAfter IS NOT NULL AND m.quantityAfter < 0) " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> getMovementsRequiringAttention();

    /**
     * Find recent movements for dashboard
     */
    @Query("SELECT m FROM MovementHistory m ORDER BY m.movementDate DESC LIMIT 50")
    List<MovementHistory> findRecentMovements();

    /**
     * Find movements by inventory item and date range
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.id = :itemId AND m.movementDate BETWEEN :startDate AND :endDate ORDER BY m.movementDate DESC")
    List<MovementHistory> findByInventoryItemAndDateRange(@Param("itemId") Long itemId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find last movement for inventory item
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.id = :itemId ORDER BY m.movementDate DESC LIMIT 1")
    MovementHistory findLastMovementByInventoryItem(@Param("itemId") Long itemId);

    /**
     * Find movements with temperature/humidity data
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.temperature IS NOT NULL OR m.humidity IS NOT NULL ORDER BY m.movementDate DESC")
    List<MovementHistory> findMovementsWithEnvironmentalData();

    /**
     * Get audit trail for compliance
     */
    @Query("SELECT m FROM MovementHistory m WHERE " +
           "m.movementDate BETWEEN :startDate AND :endDate AND " +
           "m.movementType IN ('RECEIPT', 'ISSUE', 'ADJUSTMENT', 'DISPOSAL', 'SHIP') " +
           "ORDER BY m.movementDate ASC")
    List<MovementHistory> getAuditTrail(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find movements by batch processing
     */
    @Query("SELECT m FROM MovementHistory m WHERE m.batchId = :batchId ORDER BY m.movementDate ASC")
    List<MovementHistory> findByBatchProcessing(@Param("batchId") String batchId);

    /**
     * Get movement velocity (movements per hour) for date range
     */
    @Query("SELECT COUNT(m) * 1.0 / (EXTRACT(EPOCH FROM (:endDate - :startDate)) / 3600) " +
           "FROM MovementHistory m WHERE m.movementDate BETWEEN :startDate AND :endDate")
    Double getMovementVelocity(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}