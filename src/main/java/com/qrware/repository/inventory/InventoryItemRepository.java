package com.qrware.repository.inventory;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.InventoryStatus;
import com.qrware.domain.product.Product;
import com.qrware.domain.warehouse.Location;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends BaseRepository<InventoryItem> {

    Optional<InventoryItem> findByQrCode(String qrCode);

    boolean existsByQrCode(String qrCode);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM InventoryItem i WHERE i.qrCode = :qrCode AND i.id != :itemId")
    boolean existsByQrCodeAndIdNot(@Param("qrCode") String qrCode, @Param("itemId") Long itemId);

    @Query("SELECT i FROM InventoryItem i WHERE i.product.id = :productId")
    List<InventoryItem> findByProductId(@Param("productId") Long productId);

    @Query("SELECT i FROM InventoryItem i WHERE i.product.sku = :sku")
    List<InventoryItem> findByProductSku(@Param("sku") String sku);

    @Query("SELECT i FROM InventoryItem i WHERE i.product = :product AND i.location = :location")
    Optional<InventoryItem> findByProductAndLocation(@Param("product") Product product, @Param("location") Location location);

    @Query("SELECT i FROM InventoryItem i WHERE i.product = :product AND i.location = :location AND i.availableQuantity > 0 ORDER BY i.createdAt ASC")
    Optional<InventoryItem> findFirstAvailableByProductAndLocation(@Param("product") Product product, @Param("location") Location location);

    @Query("SELECT i FROM InventoryItem i WHERE i.location.id = :locationId")
    List<InventoryItem> findByLocationId(@Param("locationId") Long locationId);

    @Query("SELECT i FROM InventoryItem i WHERE i.location.code = :locationCode")
    List<InventoryItem> findByLocationCode(@Param("locationCode") String locationCode);

    @Query("SELECT i FROM InventoryItem i WHERE i.location.zone.id = :zoneId")
    List<InventoryItem> findByZoneId(@Param("zoneId") Long zoneId);

    @Query("SELECT i FROM InventoryItem i WHERE i.location.zone.code = :zoneCode")
    List<InventoryItem> findByZoneCode(@Param("zoneCode") String zoneCode);

    List<InventoryItem> findByStatus(InventoryStatus status);

    @Query("SELECT i FROM InventoryItem i WHERE i.status IN :statuses")
    List<InventoryItem> findByStatusIn(@Param("statuses") List<InventoryStatus> statuses);

    @Query("SELECT i FROM InventoryItem i WHERE i.status = 'AVAILABLE' AND i.quarantine = false AND i.hold = false AND i.availableQuantity > 0")
    List<InventoryItem> findAvailableItems();

    @Query("SELECT i FROM InventoryItem i WHERE i.product.id = :productId AND i.status = 'AVAILABLE' AND i.quarantine = false AND i.hold = false AND i.availableQuantity > 0")
    List<InventoryItem> findAvailableItemsByProductId(@Param("productId") Long productId);

    List<InventoryItem> findByLotNumber(String lotNumber);

    List<InventoryItem> findByBatchNumber(String batchNumber);

    Optional<InventoryItem> findBySerialNumber(String serialNumber);

    List<InventoryItem> findBySupplierReference(String supplierReference);

    List<InventoryItem> findByPurchaseOrderNumber(String purchaseOrderNumber);

    @Query("SELECT i FROM InventoryItem i WHERE i.receivedDate BETWEEN :startDate AND :endDate")
    List<InventoryItem> findByReceivedDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM InventoryItem i WHERE i.receivedDate >= :date")
    List<InventoryItem> findByReceivedDateAfter(@Param("date") LocalDate date);

    @Query("SELECT i FROM InventoryItem i WHERE i.expiryDate IS NOT NULL AND i.expiryDate < CURRENT_DATE")
    List<InventoryItem> findExpiredItems();

    @Query("SELECT i FROM InventoryItem i WHERE i.expiryDate IS NOT NULL AND i.expiryDate BETWEEN CURRENT_DATE AND :expiryDate")
    List<InventoryItem> findItemsExpiringSoon(@Param("expiryDate") LocalDate expiryDate);

    @Query("SELECT i FROM InventoryItem i WHERE i.expiryDate IS NOT NULL AND i.expiryDate BETWEEN CURRENT_DATE AND :endDate")
    List<InventoryItem> findItemsExpiringWithinDays(@Param("endDate") LocalDate endDate);

    List<InventoryItem> findByQuarantineTrue();

    List<InventoryItem> findByHoldTrue();

    @Query("SELECT i FROM InventoryItem i WHERE i.reservedQuantity > 0")
    List<InventoryItem> findItemsWithReservedQuantity();

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.product.minimumStock")
    List<InventoryItem> findLowStockItems();

    @Query("SELECT i FROM InventoryItem i WHERE i.product.reorderPoint IS NOT NULL AND i.quantity <= i.product.reorderPoint")
    List<InventoryItem> findItemsBelowReorderPoint();

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity BETWEEN :minQuantity AND :maxQuantity")
    List<InventoryItem> findByQuantityBetween(@Param("minQuantity") Integer minQuantity, @Param("maxQuantity") Integer maxQuantity);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity = 0")
    List<InventoryItem> findZeroQuantityItems();

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity < 0")
    List<InventoryItem> findNegativeQuantityItems();

    @Query("SELECT i FROM InventoryItem i WHERE i.unitCost BETWEEN :minCost AND :maxCost")
    List<InventoryItem> findByUnitCostBetween(@Param("minCost") BigDecimal minCost, @Param("maxCost") BigDecimal maxCost);

    @Query("SELECT i FROM InventoryItem i WHERE i.totalCost BETWEEN :minCost AND :maxCost")
    List<InventoryItem> findByTotalCostBetween(@Param("minCost") BigDecimal minCost, @Param("maxCost") BigDecimal maxCost);

    @Query("SELECT i FROM InventoryItem i WHERE i.lastCountedDate IS NULL OR i.lastCountedDate < :date")
    List<InventoryItem> findItemsNotCountedSince(@Param("date") LocalDateTime date);

    @Query("SELECT i FROM InventoryItem i WHERE i.lastMovedDate IS NULL OR i.lastMovedDate < :date")
    List<InventoryItem> findItemsNotMovedSince(@Param("date") LocalDateTime date);

    List<InventoryItem> findByConditionRating(Integer conditionRating);

    @Query("SELECT i FROM InventoryItem i WHERE i.conditionRating <= :maxRating")
    List<InventoryItem> findByConditionRatingLessThanEqual(@Param("maxRating") Integer maxRating);

    @Query("SELECT i FROM InventoryItem i WHERE i.temperature BETWEEN :minTemp AND :maxTemp")
    List<InventoryItem> findByTemperatureBetween(@Param("minTemp") Integer minTemp, @Param("maxTemp") Integer maxTemp);

    @Query("SELECT i FROM InventoryItem i WHERE i.humidity BETWEEN :minHumidity AND :maxHumidity")
    List<InventoryItem> findByHumidityBetween(@Param("minHumidity") Integer minHumidity, @Param("maxHumidity") Integer maxHumidity);

    @Query("SELECT SUM(i.quantity) FROM InventoryItem i WHERE i.product.id = :productId")
    Long getTotalQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(i.availableQuantity) FROM InventoryItem i WHERE i.product.id = :productId AND i.status = 'AVAILABLE' AND i.quarantine = false AND i.hold = false")
    Long getAvailableQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(i.reservedQuantity) FROM InventoryItem i WHERE i.product.id = :productId")
    Long getReservedQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(i.totalCost) FROM InventoryItem i WHERE i.product.id = :productId AND i.totalCost IS NOT NULL")
    BigDecimal getTotalValueByProductId(@Param("productId") Long productId);

    @Query("SELECT i.location.code, COUNT(i), SUM(i.quantity), SUM(i.availableQuantity), SUM(i.totalCost) " +
           "FROM InventoryItem i WHERE i.totalCost IS NOT NULL " +
           "GROUP BY i.location.id, i.location.code ORDER BY i.location.code")
    List<Object[]> getInventorySummaryByLocation();

    @Query("SELECT i.product.sku, i.product.name, COUNT(i), SUM(i.quantity), SUM(i.availableQuantity), SUM(i.totalCost) " +
           "FROM InventoryItem i WHERE i.totalCost IS NOT NULL " +
           "GROUP BY i.product.id, i.product.sku, i.product.name ORDER BY i.product.sku")
    List<Object[]> getInventorySummaryByProduct();

    @Query("SELECT i.location.zone.code, i.location.zone.name, COUNT(i), SUM(i.quantity), SUM(i.availableQuantity), SUM(i.totalCost) " +
           "FROM InventoryItem i WHERE i.totalCost IS NOT NULL " +
           "GROUP BY i.location.zone.id, i.location.zone.code, i.location.zone.name ORDER BY i.location.zone.code")
    List<Object[]> getInventorySummaryByZone();

    @Query("SELECT " +
           "SUM(CASE WHEN i.receivedDate >= :date30 THEN i.quantity ELSE 0 END) as qty0to30, " +
           "SUM(CASE WHEN i.receivedDate < :date30 AND i.receivedDate >= :date60 THEN i.quantity ELSE 0 END) as qty30to60, " +
           "SUM(CASE WHEN i.receivedDate < :date60 AND i.receivedDate >= :date90 THEN i.quantity ELSE 0 END) as qty60to90, " +
           "SUM(CASE WHEN i.receivedDate < :date90 THEN i.quantity ELSE 0 END) as qtyOver90 " +
           "FROM InventoryItem i")
    Object[] getInventoryAging(@Param("date30") LocalDate date30, @Param("date60") LocalDate date60, @Param("date90") LocalDate date90);

    long countByStatus(InventoryStatus status);

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.expiryDate IS NOT NULL AND i.expiryDate < CURRENT_DATE")
    long countExpiredItems();

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.expiryDate IS NOT NULL AND i.expiryDate BETWEEN CURRENT_DATE AND :expiryDate")
    long countItemsExpiringSoon(@Param("expiryDate") LocalDate expiryDate);

    long countByQuarantineTrue();

    long countByHoldTrue();

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.quantity <= i.product.minimumStock")
    long countLowStockItems();

    @Query("SELECT " +
           "COUNT(i) as totalItems, " +
           "SUM(i.quantity) as totalQuantity, " +
           "SUM(i.availableQuantity) as totalAvailableQuantity, " +
           "SUM(i.reservedQuantity) as totalReservedQuantity, " +
           "SUM(CASE WHEN i.totalCost IS NOT NULL THEN i.totalCost ELSE 0 END) as totalValue, " +
           "COUNT(DISTINCT i.product.id) as uniqueProducts, " +
           "COUNT(DISTINCT i.location.id) as uniqueLocations " +
           "FROM InventoryItem i")
    Object[] getInventoryStatistics();

    @Query("SELECT i FROM InventoryItem i WHERE i.createdBy = :username")
    List<InventoryItem> findCreatedBy(@Param("username") String username);

    @Query("SELECT i FROM InventoryItem i ORDER BY i.receivedDate DESC, i.createdAt DESC")
    List<InventoryItem> findMostRecentlyReceived();

    @Query("SELECT i FROM InventoryItem i WHERE i.totalCost IS NOT NULL ORDER BY i.totalCost DESC")
    List<InventoryItem> findHighestValueItems();

    @Query("SELECT i FROM InventoryItem i WHERE " +
           "(i.expiryDate IS NOT NULL AND i.expiryDate <= :alertDate) OR " +
           "i.quarantine = true OR " +
           "i.hold = true OR " +
           "i.conditionRating <= :minCondition")
    List<InventoryItem> findItemsRequiringAttention(@Param("alertDate") LocalDate alertDate, @Param("minCondition") Integer minCondition);

    @Query("SELECT i FROM InventoryItem i WHERE EXISTS " +
           "(SELECT i2 FROM InventoryItem i2 WHERE i2.id != i.id AND i2.product.id = i.product.id AND i2.location.id = i.location.id)")
    List<InventoryItem> findDuplicateItems();

    @Query("SELECT i FROM InventoryItem i WHERE " +
           "i.status = 'AVAILABLE' AND " +
           "i.quarantine = false AND " +
           "i.hold = false AND " +
           "i.availableQuantity >= :requiredQuantity AND " +
           "i.location.active = true AND " +
           "i.location.pickable = true " +
           "ORDER BY i.location.zone.pickingPriority ASC, i.receivedDate ASC")
    List<InventoryItem> findPickableItems(@Param("requiredQuantity") Integer requiredQuantity);

    @Query("SELECT i FROM InventoryItem i WHERE i.product.id = :productId AND i.status = 'AVAILABLE' AND i.quarantine = false AND i.hold = false AND i.availableQuantity > 0 " +
           "ORDER BY i.receivedDate ASC, i.createdAt ASC")
    List<InventoryItem> findItemsByFIFO(@Param("productId") Long productId);

    @Query("""
    SELECT i FROM InventoryItem i
    WHERE i.product.id = :productId
      AND i.status = 'AVAILABLE'
      AND i.quarantine = false
      AND i.hold = false
      AND i.availableQuantity > 0
    ORDER BY COALESCE(i.expiryDate, CAST('9999-12-31' AS date)) ASC,
             i.receivedDate ASC
""")
    List<InventoryItem> findItemsByFEFO(@Param("productId") Long productId);

    @Query("SELECT i FROM InventoryItem i " +
            "JOIN i.product p " +
            "JOIN i.location l " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<InventoryItem> searchInventory(@Param("query") String query);
}