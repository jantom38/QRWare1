package com.qrware.repository.order;

import com.qrware.domain.order.Order;
import com.qrware.domain.order.OrderItem;
import com.qrware.domain.order.OrderItemStatus;
import com.qrware.domain.product.Product;
import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.warehouse.Location;
import com.qrware.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends BaseRepository<OrderItem> {

    // Order-based queries
    List<OrderItem> findByOrder(Order order);
    
    List<OrderItem> findByOrderOrderByLineNumber(Order order);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order = :order AND oi.status = :status")
    List<OrderItem> findByOrderAndStatus(@Param("order") Order order, @Param("status") OrderItemStatus status);

    // Product-based queries
    List<OrderItem> findByProduct(Product product);
    
    Page<OrderItem> findByProduct(Product product, Pageable pageable);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product = :product AND oi.status IN :statuses")
    List<OrderItem> findByProductAndStatusIn(@Param("product") Product product, @Param("statuses") List<OrderItemStatus> statuses);

    // Status-based queries
    List<OrderItem> findByStatus(OrderItemStatus status);
    
    List<OrderItem> findByStatusIn(List<OrderItemStatus> statuses);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.status IN :activeStatuses")
    List<OrderItem> findActiveOrderItems(@Param("activeStatuses") List<OrderItemStatus> activeStatuses);

    // Location-based queries
    List<OrderItem> findBySourceLocation(Location sourceLocation);
    
    List<OrderItem> findByDestinationLocation(Location destinationLocation);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.sourceLocation = :location OR oi.destinationLocation = :location")
    List<OrderItem> findByLocation(@Param("location") Location location);

    // Inventory-based queries
    List<OrderItem> findByInventoryItem(InventoryItem inventoryItem);
    
    Optional<OrderItem> findByInventoryItemAndStatus(InventoryItem inventoryItem, OrderItemStatus status);

    // QR Code queries
    @Query("SELECT oi FROM OrderItem oi WHERE oi.qrCodeData = :qrData")
    Optional<OrderItem> findByQrCodeData(@Param("qrData") String qrData);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.qrCodeData IS NOT NULL")
    List<OrderItem> findItemsWithQrCode();
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.qrCodeData IS NULL AND oi.order.type IN ('INBOUND', 'OUTBOUND')")
    List<OrderItem> findItemsRequiringQrScan();

    // Batch and Serial number queries
    List<OrderItem> findByBatchNumber(String batchNumber);
    
    Optional<OrderItem> findBySerialNumber(String serialNumber);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.batchNumber IS NOT NULL AND oi.expiryDate <= :date")
    List<OrderItem> findExpiringBatches(@Param("date") LocalDateTime date);

    // Completion queries
    @Query("SELECT oi FROM OrderItem oi WHERE oi.completedQuantity < oi.requestedQuantity AND oi.status = 'PARTIALLY_COMPLETED'")
    List<OrderItem> findPartiallyCompletedItems();
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.completedQuantity = 0 AND oi.status IN :pendingStatuses")
    List<OrderItem> findPendingItems(@Param("pendingStatuses") List<OrderItemStatus> pendingStatuses);

    // Date-based queries
    @Query("SELECT oi FROM OrderItem oi WHERE oi.pickedAt BETWEEN :startDate AND :endDate")
    List<OrderItem> findItemsPickedBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.completedAt BETWEEN :startDate AND :endDate")
    List<OrderItem> findItemsCompletedBetween(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    // Statistics queries
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.status = :status")
    Long countByStatus(@Param("status") OrderItemStatus status);
    
    @Query("SELECT oi.status, COUNT(oi) FROM OrderItem oi GROUP BY oi.status")
    List<Object[]> getOrderItemCountByStatus();
    
    @Query("SELECT SUM(oi.completedQuantity) FROM OrderItem oi WHERE oi.product = :product AND oi.completedAt BETWEEN :startDate AND :endDate")
    Long getTotalCompletedQuantityByProduct(@Param("product") Product product, 
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Performance queries
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, oi.pickedAt, oi.completedAt)) " +
           "FROM OrderItem oi WHERE oi.pickedAt IS NOT NULL AND oi.completedAt IS NOT NULL")
    Double getAveragePickToCompleteTimeMinutes();
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.status = 'IN_PROGRESS' AND " +
           "oi.pickedAt IS NOT NULL AND " +
           "TIMESTAMPDIFF(HOUR, oi.pickedAt, CURRENT_TIMESTAMP) > :hours")
    List<OrderItem> findItemsInProgressLongerThan(@Param("hours") int hours);

    // Complex filters
    @Query("SELECT oi FROM OrderItem oi WHERE " +
           "(:orderId IS NULL OR oi.order.id = :orderId) AND " +
           "(:productId IS NULL OR oi.product.id = :productId) AND " +
           "(:status IS NULL OR oi.status = :status) AND " +
           "(:sourceLocationId IS NULL OR oi.sourceLocation.id = :sourceLocationId) AND " +
           "(:destinationLocationId IS NULL OR oi.destinationLocation.id = :destinationLocationId)")
    Page<OrderItem> findOrderItemsWithFilters(@Param("orderId") Long orderId,
                                              @Param("productId") Long productId,
                                              @Param("status") OrderItemStatus status,
                                              @Param("sourceLocationId") Long sourceLocationId,
                                              @Param("destinationLocationId") Long destinationLocationId,
                                              Pageable pageable);

    // Search functionality
    @Query("SELECT DISTINCT oi FROM OrderItem oi LEFT JOIN oi.product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(oi.batchNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(oi.serialNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(oi.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<OrderItem> searchOrderItems(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Line number queries
    @Query("SELECT MAX(oi.lineNumber) FROM OrderItem oi WHERE oi.order = :order")
    Optional<Integer> findMaxLineNumberByOrder(@Param("order") Order order);
    
    Optional<OrderItem> findByOrderAndLineNumber(Order order, Integer lineNumber);

    // Quantity analysis
    @Query("SELECT oi FROM OrderItem oi WHERE oi.completedQuantity > oi.requestedQuantity")
    List<OrderItem> findOvercompletedItems();
    
    @Query("SELECT SUM(oi.requestedQuantity - oi.completedQuantity) FROM OrderItem oi " +
           "WHERE oi.status IN ('PENDING', 'IN_PROGRESS', 'PARTIALLY_COMPLETED') AND oi.product = :product")
    Long getOutstandingQuantityByProduct(@Param("product") Product product);

    // Recent activity
    @Query("SELECT oi FROM OrderItem oi WHERE oi.completedAt >= :since ORDER BY oi.completedAt DESC")
    List<OrderItem> findRecentlyCompletedItems(@Param("since") LocalDateTime since);
}