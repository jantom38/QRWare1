package com.qrware.repository.order;

import com.qrware.domain.order.Order;
import com.qrware.domain.order.OrderStatus;
import com.qrware.domain.order.OrderType;
import com.qrware.domain.order.OrderPriority;
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
public interface OrderRepository extends BaseRepository<Order> {

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.product " +
           "LEFT JOIN FETCH oi.inventoryItem " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    Optional<Order> findByOrderNumber(String orderNumber);
    
    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    
    @Query("SELECT o FROM Order o WHERE o.status IN :activeStatuses")
    List<Order> findActiveOrders(@Param("activeStatuses") List<OrderStatus> activeStatuses);

    List<Order> findByType(OrderType type);
    
    Page<Order> findByType(OrderType type, Pageable pageable);

    List<Order> findByCreatedBy(User createdBy);
    
    List<Order> findByAssignedTo(User assignedTo);
    
    Page<Order> findByAssignedTo(User assignedTo, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.assignedTo = :user AND o.status IN :statuses")
    List<Order> findByAssignedToAndStatusIn(@Param("user") User user, @Param("statuses") List<OrderStatus> statuses);

    List<Order> findByPriority(OrderPriority priority);
    
    @Query("SELECT o FROM Order o WHERE o.priority IN ('URGENT', 'CRITICAL') AND o.status IN :activeStatuses")
    List<Order> findHighPriorityActiveOrders(@Param("activeStatuses") List<OrderStatus> activeStatuses);

    @Query("SELECT o FROM Order o WHERE o.expectedDate <= :date AND o.status IN :activeStatuses")
    List<Order> findOverdueOrders(@Param("date") LocalDateTime date, @Param("activeStatuses") List<OrderStatus> activeStatuses);
    
    @Query("SELECT o FROM Order o WHERE o.expectedDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersExpectedBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE " +
           "(:type IS NULL OR o.type = :type) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:priority IS NULL OR o.priority = :priority) AND " +
           "(:assignedTo IS NULL OR o.assignedTo = :assignedTo) AND " +
           "(:orderNumber IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :orderNumber, '%')))")
    Page<Order> findOrdersWithFilters(@Param("type") OrderType type,
                                     @Param("status") OrderStatus status,
                                     @Param("priority") OrderPriority priority,
                                     @Param("assignedTo") User assignedTo,
                                     @Param("orderNumber") String orderNumber,
                                     Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderCountByStatus();
    
    @Query("SELECT o.type, COUNT(o) FROM Order o WHERE o.createdAt >= :fromDate GROUP BY o.type")
    List<Object[]> getOrderCountByTypeSince(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.assignedTo = :user AND o.status IN :activeStatuses")
    Long countActiveOrdersByUser(@Param("user") User user, @Param("activeStatuses") List<OrderStatus> activeStatuses);

    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, o.createdAt, o.completedAt)) " +
           "FROM Order o WHERE o.completedAt IS NOT NULL AND o.type = :type")
    Double getAverageCompletionTimeHours(@Param("type") OrderType type);
    
    @Query("SELECT o FROM Order o WHERE o.status = 'IN_PROGRESS' AND " +
           "o.startedAt IS NOT NULL AND " +
           "TIMESTAMPDIFF(HOUR, o.startedAt, CURRENT_TIMESTAMP) > :hours")
    List<Order> findOrdersInProgressLongerThan(@Param("hours") int hours);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi LEFT JOIN oi.product p WHERE " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Order> searchOrders(@Param("searchTerm") String searchTerm, Pageable pageable);

    Optional<Order> findByExternalReference(String externalReference);
    
    List<Order> findByExternalReferenceIsNotNull();

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :since ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(@Param("since") LocalDateTime since);
    
    @Query("SELECT o.assignedTo, COUNT(o) FROM Order o WHERE o.status IN :activeStatuses " +
           "GROUP BY o.assignedTo HAVING o.assignedTo IS NOT NULL ORDER BY COUNT(o) DESC")
    List<Object[]> getUserWorkload(@Param("activeStatuses") List<OrderStatus> activeStatuses);
}