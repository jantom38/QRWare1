package com.qrware.service;

import com.qrware.domain.order.*;
import com.qrware.domain.product.Product;
import com.qrware.domain.user.User;
import com.qrware.domain.warehouse.Location;
import com.qrware.domain.inventory.MovementType;
import com.qrware.repository.order.OrderRepository;
import com.qrware.repository.order.OrderItemRepository;
import com.qrware.repository.order.OrderStatusHistoryRepository;
import com.qrware.repository.product.ProductRepository;
import com.qrware.repository.user.UserRepository;
import com.qrware.repository.warehouse.LocationRepository;
import com.qrware.service.MovementHistoryService;
import com.qrware.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private MovementHistoryService movementHistoryService;

    // === ORDER MANAGEMENT ===

    public Order createOrder(String orderNumber, OrderType type, String description,
                             User createdBy, User assignedTo, Location sourceLocation,
                             Location destinationLocation, LocalDateTime expectedDate,
                             OrderPriority priority) {

        if (orderRepository.existsByOrderNumber(orderNumber)) {
            throw new IllegalArgumentException("Order number already exists: " + orderNumber);
        }

        Order order = new Order(orderNumber, type, createdBy);
        order.setDescription(description);
        order.setAssignedTo(assignedTo);
        order.setSourceLocation(sourceLocation);
        order.setDestinationLocation(destinationLocation);
        order.setExpectedDate(expectedDate);
        order.setPriority(priority != null ? priority : OrderPriority.NORMAL);

        order = orderRepository.save(order);

        createStatusHistory(order, null, OrderStatus.CREATED, createdBy, "Order created");

        return order;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public List<Order> getActiveOrders() {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS,
                OrderStatus.ON_HOLD, OrderStatus.PARTIALLY_COMPLETED
        );
        return orderRepository.findActiveOrders(activeStatuses);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByAssignedTo(user);
    }

    public List<Order> getOverdueOrders() {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS,
                OrderStatus.PARTIALLY_COMPLETED
        );
        return orderRepository.findOverdueOrders(LocalDateTime.now(), activeStatuses);
    }

    public Page<Order> searchOrders(String searchTerm, Pageable pageable) {
        return orderRepository.searchOrders(searchTerm, pageable);
    }

    // === ORDER STATUS MANAGEMENT ===

    public Order startOrder(Long orderId, User user) {
        Order order = getOrderById(orderId);

        if (!order.canBeStarted()) {
            throw new IllegalStateException("Order cannot be started in current status: " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.start(user);
        order = orderRepository.save(order);

        createStatusHistory(order, oldStatus, order.getStatus(), user, "Order started");

        return order;
    }

    public Order completeOrder(Long orderId, User user) {
        Order order = getOrderById(orderId);

        if (!order.canBeCompleted()) {
            throw new IllegalStateException("Order cannot be completed in current status: " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.complete(user);
        order = orderRepository.save(order);

        createStatusHistory(order, oldStatus, order.getStatus(), user, "Order completed");

        createMovementHistoryForCompletedOrder(order);

        return order;
    }

    public Order cancelOrder(Long orderId, User user, String reason) {
        Order order = getOrderById(orderId);

        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.cancel(user, reason);
        order = orderRepository.save(order);

        createStatusHistory(order, oldStatus, order.getStatus(), user, reason);

        return order;
    }

    public Order assignOrder(Long orderId, Long userId, User assignedBy) {
        Order order = getOrderById(orderId);
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        OrderStatus oldStatus = order.getStatus();
        order.assign(assignee);
        order = orderRepository.save(order);

        if (oldStatus != order.getStatus()) {
            createStatusHistory(order, oldStatus, order.getStatus(), assignedBy,
                    "Order assigned to " + assignee.getUsername());
        }

        return order;
    }

    // === ORDER ITEMS MANAGEMENT ===

    public OrderItem addOrderItem(Long orderId, Long productId, Integer requestedQuantity,
                                  Long sourceLocationId, Long destinationLocationId,
                                  BigDecimal unitPrice, String notes) {

        Order order = getOrderById(orderId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Location sourceLocation = sourceLocationId != null ?
                locationRepository.findById(sourceLocationId).orElse(null) : null;
        Location destinationLocation = destinationLocationId != null ?
                locationRepository.findById(destinationLocationId).orElse(null) : null;

        Integer nextLineNumber = orderItemRepository.findMaxLineNumberByOrder(order)
                .orElse(0) + 1;

        OrderItem orderItem = new OrderItem(order, nextLineNumber, product, requestedQuantity);
        orderItem.setSourceLocation(sourceLocation);
        orderItem.setDestinationLocation(destinationLocation);
        orderItem.setUnitPrice(unitPrice);
        orderItem.setNotes(notes);

        orderItem = orderItemRepository.save(orderItem);

        updateOrderProgress(order);

        return orderItem;
    }

    public OrderItem completeOrderItem(Long orderItemId, Integer completedQuantity,
                                       String completionNotes, String qrCodeData) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + orderItemId));

        orderItem.complete(completedQuantity, completionNotes);
        if (qrCodeData != null) {
            orderItem.setQrCodeData(qrCodeData);
        }

        orderItem = orderItemRepository.save(orderItem);

        updateOrderProgress(orderItem.getOrder());

        return orderItem;
    }

    public OrderItem scanQRForOrderItem(Long orderItemId, String qrCodeData) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + orderItemId));

        orderItem.setQrCodeData(qrCodeData);
        return orderItemRepository.save(orderItem);
    }

    public Optional<OrderItem> findOrderItemByQRCode(String qrCodeData) {
        return orderItemRepository.findByQrCodeData(qrCodeData);
    }

    // === STATISTICS AND REPORTS ===

    public Long getOrderCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public List<Object[]> getOrderCountByStatus() {
        return orderRepository.getOrderCountByStatus();
    }

    public Long getActiveOrderCountByUser(User user) {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS,
                OrderStatus.PARTIALLY_COMPLETED
        );
        return orderRepository.countActiveOrdersByUser(user, activeStatuses);
    }

    public List<Order> getHighPriorityOrders() {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS,
                OrderStatus.PARTIALLY_COMPLETED
        );
        return orderRepository.findHighPriorityActiveOrders(activeStatuses);
    }

    // === PRIVATE HELPER METHODS ===

    private void createStatusHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus,
                                     User changedBy, String reason) {
        OrderStatusHistory history = new OrderStatusHistory(order, oldStatus, newStatus, changedBy, reason);
        statusHistoryRepository.save(history);
    }

    private void updateOrderProgress(Order order) {
        order.updateProgress();
        orderRepository.save(order);
    }

    private void createMovementHistoryForCompletedOrder(Order order) {
        MovementType movementType = getMovementTypeForOrder(order.getType());

        for (OrderItem item : order.getOrderItems()) {
            if (item.isCompleted() && item.getInventoryItem() != null) {
                // Corrected call to use 8 arguments:
                movementHistoryService.createSystemMovement(
                        item.getInventoryItem().getId(), // 1. Inventory Item ID
                        movementType,                    // 2. Movement Type
                        null,                            // 3. quantityBefore (Assuming null/0 for initial state)
                        item.getCompletedQuantity(),     // 4. quantityAfter (Completed quantity)
                        item.getSourceLocation(),        // 5. fromLocation
                        item.getDestinationLocation(),   // 6. toLocation
                        "Completed via order: " + order.getOrderNumber(), // 7. reason
                        order.getOrderNumber()           // 8. referenceNumber
                );
            }
        }
    }

    private MovementType getMovementTypeForOrder(OrderType orderType) {
        switch (orderType) {
            case INBOUND: return MovementType.ORDER_RECEIPT;
            case OUTBOUND: return MovementType.ORDER_ISSUE;
            case PICK: return MovementType.ORDER_PICK;
            case PUTAWAY: return MovementType.ORDER_PACK;
            case TRANSFER: return MovementType.TRANSFER;
            case RETURN: return MovementType.ORDER_RETURN;
            case ADJUSTMENT: return MovementType.ORDER_ADJUSTMENT;
            default: return MovementType.ORDER_ISSUE;
        }
    }

    // === BUSINESS VALIDATION ===

    public String generateOrderNumber(OrderType type) {
        String prefix = type.name().substring(0, 3).toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp.substring(timestamp.length() - 8);
    }

    public boolean canUserAccessOrder(Order order, User user) {
        return order.getCreatedBy().equals(user) ||
                order.getAssignedTo() != null && order.getAssignedTo().equals(user) ||
                user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));
    }
}