package com.qrware.repository.inventory;

import com.qrware.domain.inventory.MovementHistory;
import com.qrware.domain.inventory.MovementType;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovementHistoryRepository extends BaseRepository<MovementHistory> {

    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.id = :itemId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByInventoryItemId(@Param("itemId") Long itemId);

    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.qrCode = :qrCode ORDER BY m.movementDate DESC")
    List<MovementHistory> findByInventoryItemQrCode(@Param("qrCode") String qrCode);

    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.product.id = :productId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByProductId(@Param("productId") Long productId);

    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.product.sku = :sku ORDER BY m.movementDate DESC")
    List<MovementHistory> findByProductSku(@Param("sku") String sku);

    @Query("SELECT m FROM MovementHistory m WHERE m.fromLocation.id = :locationId OR m.toLocation.id = :locationId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByLocationId(@Param("locationId") Long locationId);

    @Query("SELECT m FROM MovementHistory m WHERE m.fromLocation.id = :locationId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByFromLocationId(@Param("locationId") Long locationId);

    @Query("SELECT m FROM MovementHistory m WHERE m.toLocation.id = :locationId ORDER BY m.movementDate DESC")
    List<MovementHistory> findByToLocationId(@Param("locationId") Long locationId);

    @Query("SELECT m FROM MovementHistory m WHERE " +
           "m.fromLocation.zone.id = :zoneId OR m.toLocation.zone.id = :zoneId OR m.inventoryItem.location.zone.id = :zoneId " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findByZoneId(@Param("zoneId") Long zoneId);

    List<MovementHistory> findByMovementTypeOrderByMovementDateDesc(MovementType movementType);

    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN :types ORDER BY m.movementDate DESC")
    List<MovementHistory> findByMovementTypeIn(@Param("types") List<MovementType> types);

    @Query("SELECT m FROM MovementHistory m WHERE m.movementDate BETWEEN :startDate AND :endDate ORDER BY m.movementDate DESC")
    List<MovementHistory> findByMovementDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM MovementHistory m WHERE m.movementDate >= :date ORDER BY m.movementDate DESC")
    List<MovementHistory> findByMovementDateAfter(@Param("date") LocalDateTime date);

    List<MovementHistory> findByUserIdOrderByMovementDateDesc(String userId);

    List<MovementHistory> findByUserNameOrderByMovementDateDesc(String userName);

    List<MovementHistory> findByReferenceNumberOrderByMovementDateDesc(String referenceNumber);

    List<MovementHistory> findByReferenceTypeOrderByMovementDateDesc(String referenceType);

    List<MovementHistory> findByBatchIdOrderByMovementDateDesc(String batchId);

    @Query("SELECT m FROM MovementHistory m WHERE m.approved = false AND m.movementType IN " +
           "('DISPOSAL', 'SCRAP', 'ADJUSTMENT', 'LOSS', 'DAMAGE') ORDER BY m.movementDate ASC")
    List<MovementHistory> findPendingApprovalMovements();

    List<MovementHistory> findByApprovedTrueOrderByMovementDateDesc();

    List<MovementHistory> findByApprovedByOrderByApprovedDateDesc(String approvedBy);

    List<MovementHistory> findBySystemGeneratedTrueOrderByMovementDateDesc();

    List<MovementHistory> findBySystemGeneratedFalseOrderByMovementDateDesc();

    @Query("SELECT m FROM MovementHistory m WHERE m.quantityChanged != 0 ORDER BY m.movementDate DESC")
    List<MovementHistory> findMovementsWithQuantityChanges();

    @Query("SELECT m FROM MovementHistory m WHERE m.quantityChanged > 0 ORDER BY m.movementDate DESC")
    List<MovementHistory> findPositiveQuantityMovements();

    @Query("SELECT m FROM MovementHistory m WHERE m.quantityChanged < 0 ORDER BY m.movementDate DESC")
    List<MovementHistory> findNegativeQuantityMovements();

    @Query("SELECT m FROM MovementHistory m WHERE m.fromLocation IS NOT NULL AND m.toLocation IS NOT NULL ORDER BY m.movementDate DESC")
    List<MovementHistory> findLocationMovements();

    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('RECEIPT', 'RETURN', 'FOUND', 'RELEASE', 'UNHOLD', 'PRODUCTION', 'LOAN_RETURN') " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findInboundMovements();

    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('ISSUE', 'SHIP', 'DISPOSAL', 'LOSS', 'SCRAP', 'CONSUMPTION', 'SAMPLE', 'LOAN') " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findOutboundMovements();

    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('ADJUSTMENT', 'CYCLE_COUNT', 'PHYSICAL_COUNT') ORDER BY m.movementDate DESC")
    List<MovementHistory> findAdjustmentMovements();

    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('RESERVE', 'PICK', 'PACK', 'SHIP', 'ALLOCATION', 'DEALLOCATION') " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findOrderRelatedMovements();

    @Query("SELECT m FROM MovementHistory m WHERE m.movementType IN " +
           "('QUARANTINE', 'RELEASE', 'DAMAGE', 'EXPIRY', 'RECALL', 'REWORK', 'SAMPLE') " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> findQualityRelatedMovements();

    @Query("SELECT m FROM MovementHistory m WHERE LOWER(m.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.movementDate DESC")
    List<MovementHistory> findByReasonContaining(@Param("keyword") String keyword);

    @Query("SELECT m FROM MovementHistory m WHERE LOWER(m.notes) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.movementDate DESC")
    List<MovementHistory> findByNotesContaining(@Param("keyword") String keyword);

    @Query("SELECT m.movementType, COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "GROUP BY m.movementType ORDER BY COUNT(m) DESC")
    List<Object[]> getMovementStatsByType();

    @Query("SELECT DATE(m.movementDate), COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "WHERE m.movementDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(m.movementDate) ORDER BY DATE(m.movementDate)")
    List<Object[]> getMovementStatsByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m.userName, COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "WHERE m.userName IS NOT NULL " +
           "GROUP BY m.userName ORDER BY COUNT(m) DESC")
    List<Object[]> getMovementStatsByUser();

    @Query("SELECT COALESCE(m.fromLocation.code, m.toLocation.code, 'Unknown'), COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "GROUP BY COALESCE(m.fromLocation.code, m.toLocation.code, 'Unknown') " +
           "ORDER BY COUNT(m) DESC")
    List<Object[]> getMovementStatsByLocation();

    @Query("SELECT m.inventoryItem.product.sku, m.inventoryItem.product.name, COUNT(m), SUM(ABS(m.quantityChanged)) " +
           "FROM MovementHistory m " +
           "GROUP BY m.inventoryItem.product.id, m.inventoryItem.product.sku, m.inventoryItem.product.name " +
           "ORDER BY COUNT(m) DESC")
    List<Object[]> getMovementStatsByProduct();

    long countByMovementType(MovementType movementType);

    @Query("SELECT COUNT(m) FROM MovementHistory m WHERE m.movementDate BETWEEN :startDate AND :endDate")
    long countByMovementDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByUserId(String userId);

    @Query("SELECT COUNT(m) FROM MovementHistory m WHERE m.approved = false AND m.movementType IN " +
           "('DISPOSAL', 'SCRAP', 'ADJUSTMENT', 'LOSS', 'DAMAGE')")
    long countPendingApprovalMovements();

    long countBySystemGeneratedTrue();

    long countBySystemGeneratedFalse();

    @Query("SELECT SUM(ABS(m.quantityChanged)) FROM MovementHistory m WHERE m.movementType = :type")
    Long getTotalQuantityMovedByType(@Param("type") MovementType type);

    @Query("SELECT SUM(ABS(m.quantityChanged)) FROM MovementHistory m WHERE m.movementDate BETWEEN :startDate AND :endDate")
    Long getTotalQuantityMovedInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM MovementHistory m WHERE " +
           "(m.approved = false AND m.movementType IN ('DISPOSAL', 'SCRAP', 'ADJUSTMENT', 'LOSS', 'DAMAGE')) OR " +
           "(m.quantityAfter IS NOT NULL AND m.quantityAfter < 0) " +
           "ORDER BY m.movementDate DESC")
    List<MovementHistory> getMovementsRequiringAttention();

    @Query("SELECT m FROM MovementHistory m ORDER BY m.movementDate DESC")
    List<MovementHistory> findRecentMovements();

    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.id = :itemId AND m.movementDate BETWEEN :startDate AND :endDate ORDER BY m.movementDate DESC")
    List<MovementHistory> findByInventoryItemAndDateRange(@Param("itemId") Long itemId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM MovementHistory m WHERE m.inventoryItem.id = :itemId ORDER BY m.movementDate DESC")
    List<MovementHistory> findLastMovementByInventoryItem(@Param("itemId") Long itemId);

    @Query("SELECT m FROM MovementHistory m WHERE m.temperature IS NOT NULL OR m.humidity IS NOT NULL ORDER BY m.movementDate DESC")
    List<MovementHistory> findMovementsWithEnvironmentalData();

    @Query("SELECT m FROM MovementHistory m WHERE " +
           "m.movementDate BETWEEN :startDate AND :endDate AND " +
           "m.movementType IN ('RECEIPT', 'ISSUE', 'ADJUSTMENT', 'DISPOSAL', 'SHIP') " +
           "ORDER BY m.movementDate ASC")
    List<MovementHistory> getAuditTrail(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM MovementHistory m WHERE m.batchId = :batchId ORDER BY m.movementDate ASC")
    List<MovementHistory> findByBatchProcessing(@Param("batchId") String batchId);

    @Query("SELECT COUNT(m) * 1.0 / (EXTRACT(EPOCH FROM (:endDate - :startDate)) / 3600) " +
           "FROM MovementHistory m WHERE m.movementDate BETWEEN :startDate AND :endDate")
    Double getMovementVelocity(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}