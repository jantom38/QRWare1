package com.qrware.repository.order;

import com.qrware.domain.order.Order;
import com.qrware.domain.order.OrderStatus;
import com.qrware.domain.order.OrderStatusHistory;
import com.qrware.domain.user.User;
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
public interface OrderStatusHistoryRepository extends BaseRepository<OrderStatusHistory> {

    // Order-based queries
    List<OrderStatusHistory> findByOrder(Order order);
    
    List<OrderStatusHistory> findByOrderOrderByChangedAtDesc(Order order);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order = :order ORDER BY osh.changedAt ASC")
    List<OrderStatusHistory> findByOrderOrderByChangedAtAsc(@Param("order") Order order);

    // Status-based queries
    List<OrderStatusHistory> findByNewStatus(OrderStatus newStatus);
    
    List<OrderStatusHistory> findByOldStatus(OrderStatus oldStatus);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.oldStatus = :oldStatus AND osh.newStatus = :newStatus")
    List<OrderStatusHistory> findByStatusTransition(@Param("oldStatus") OrderStatus oldStatus, 
                                                   @Param("newStatus") OrderStatus newStatus);

    // User-based queries
    List<OrderStatusHistory> findByChangedBy(User changedBy);
    
    Page<OrderStatusHistory> findByChangedBy(User changedBy, Pageable pageable);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.changedBy = :user AND osh.changedAt BETWEEN :startDate AND :endDate")
    List<OrderStatusHistory> findByChangedByAndDateRange(@Param("user") User user, 
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    // Date-based queries
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.changedAt BETWEEN :startDate AND :endDate")
    List<OrderStatusHistory> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.changedAt >= :since ORDER BY osh.changedAt DESC")
    List<OrderStatusHistory> findRecentChanges(@Param("since") LocalDateTime since);

    // System vs User changes
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.systemGenerated = :systemGenerated")
    List<OrderStatusHistory> findBySystemGenerated(@Param("systemGenerated") Boolean systemGenerated);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.systemGenerated = false")
    List<OrderStatusHistory> findUserInitiatedChanges();
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.systemGenerated = true")
    List<OrderStatusHistory> findSystemInitiatedChanges();

    // Latest status queries
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order = :order AND " +
           "osh.changedAt = (SELECT MAX(osh2.changedAt) FROM OrderStatusHistory osh2 WHERE osh2.order = :order)")
    Optional<OrderStatusHistory> findLatestByOrder(@Param("order") Order order);
    
    @Query("SELECT DISTINCT osh FROM OrderStatusHistory osh WHERE osh.id IN " +
           "(SELECT MAX(osh2.id) FROM OrderStatusHistory osh2 GROUP BY osh2.order)")
    List<OrderStatusHistory> findLatestStatusForAllOrders();

    // Statistics queries
    @Query("SELECT osh.newStatus, COUNT(osh) FROM OrderStatusHistory osh " +
           "WHERE osh.changedAt BETWEEN :startDate AND :endDate GROUP BY osh.newStatus")
    List<Object[]> getStatusChangeCountByPeriod(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT osh.changedBy, COUNT(osh) FROM OrderStatusHistory osh " +
           "WHERE osh.changedAt BETWEEN :startDate AND :endDate AND osh.systemGenerated = false " +
           "GROUP BY osh.changedBy ORDER BY COUNT(osh) DESC")
    List<Object[]> getUserActivityStats(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    // Transition analysis
    @Query("SELECT osh.oldStatus, osh.newStatus, COUNT(osh) FROM OrderStatusHistory osh " +
           "WHERE osh.oldStatus IS NOT NULL GROUP BY osh.oldStatus, osh.newStatus")
    List<Object[]> getStatusTransitionMatrix();
    
    @Query("SELECT COUNT(osh) FROM OrderStatusHistory osh WHERE osh.newStatus = 'CANCELLED'")
    Long countCancellations();
    
    @Query("SELECT COUNT(osh) FROM OrderStatusHistory osh WHERE osh.newStatus = 'COMPLETED'")
    Long countCompletions();

    // Performance analysis
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, created.changedAt, completed.changedAt)) FROM " +
           "OrderStatusHistory created JOIN OrderStatusHistory completed ON created.order = completed.order " +
           "WHERE created.newStatus = 'CREATED' AND completed.newStatus = 'COMPLETED'")
    Double getAverageOrderLifetimeHours();
    
    @Query("SELECT o.id, MIN(osh.changedAt) as firstChange, MAX(osh.changedAt) as lastChange FROM " +
           "OrderStatusHistory osh JOIN osh.order o GROUP BY o.id")
    List<Object[]> getOrderLifetimeData();

    // Reason analysis
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.reason IS NOT NULL AND " +
           "LOWER(osh.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<OrderStatusHistory> findByReasonContaining(@Param("keyword") String keyword);
    
    @Query("SELECT osh.reason, COUNT(osh) FROM OrderStatusHistory osh WHERE osh.reason IS NOT NULL " +
           "GROUP BY osh.reason ORDER BY COUNT(osh) DESC")
    List<Object[]> getTopReasons();

    // Complex queries
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE " +
           "(:orderId IS NULL OR osh.order.id = :orderId) AND " +
           "(:newStatus IS NULL OR osh.newStatus = :newStatus) AND " +
           "(:changedBy IS NULL OR osh.changedBy = :changedBy) AND " +
           "(:systemGenerated IS NULL OR osh.systemGenerated = :systemGenerated) AND " +
           "(:startDate IS NULL OR osh.changedAt >= :startDate) AND " +
           "(:endDate IS NULL OR osh.changedAt <= :endDate)")
    Page<OrderStatusHistory> findWithFilters(@Param("orderId") Long orderId,
                                            @Param("newStatus") OrderStatus newStatus,
                                            @Param("changedBy") User changedBy,
                                            @Param("systemGenerated") Boolean systemGenerated,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

    // Audit queries
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.id = :orderId ORDER BY osh.changedAt DESC")
    List<OrderStatusHistory> getOrderAuditTrail(@Param("orderId") Long orderId);
    
    @Query("SELECT COUNT(DISTINCT osh.order) FROM OrderStatusHistory osh WHERE osh.changedBy = :user")
    Long countDistinctOrdersModifiedByUser(@Param("user") User user);

    // Search functionality
    @Query("SELECT DISTINCT osh FROM OrderStatusHistory osh WHERE " +
           "LOWER(osh.reason) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(osh.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(osh.order.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<OrderStatusHistory> searchStatusHistory(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Time-based analysis
    @Query("SELECT DATE(osh.changedAt), COUNT(osh) FROM OrderStatusHistory osh " +
           "WHERE osh.changedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(osh.changedAt) ORDER BY DATE(osh.changedAt)")
    List<Object[]> getDailyStatusChangeCount(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT HOUR(osh.changedAt), COUNT(osh) FROM OrderStatusHistory osh " +
           "WHERE osh.changedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY HOUR(osh.changedAt) ORDER BY HOUR(osh.changedAt)")
    List<Object[]> getHourlyStatusChangeCount(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
}