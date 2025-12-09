package com.qrware.service;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.MovementType;
import com.qrware.domain.order.*;
import com.qrware.domain.product.Product;
import com.qrware.domain.user.User;
import com.qrware.domain.warehouse.Location;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.order.OrderItemRepository;
import com.qrware.repository.order.OrderRepository;
import com.qrware.repository.order.OrderStatusHistoryRepository;
import com.qrware.repository.product.ProductRepository;
import com.qrware.repository.user.UserRepository;
import com.qrware.repository.warehouse.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final String QR_DATA_SEPARATOR = "###";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final MovementHistoryService movementHistoryService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderStatusHistoryRepository statusHistoryRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        LocationRepository locationRepository,
                        InventoryItemRepository inventoryItemRepository,
                        MovementHistoryService movementHistoryService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.movementHistoryService = movementHistoryService;
    }

    public Optional<Object> processQRScan(String qrCode) {
        return processQRScan(qrCode, null);
    }

    public Optional<Object> processQRScan(String qrCode, Long orderId) {
        logger.info("QR scan processing for code: {} (orderId: {})", qrCode, orderId);
        String systemId = qrCode.split(QR_DATA_SEPARATOR)[0];

        // First check for OrderItem directly by QR code
        Optional<OrderItem> orderItemOptional = orderItemRepository.findByQrCodeData(systemId);
        if (orderItemOptional.isPresent()) {
            logger.info("Found OrderItem by QR code: {}", systemId);
            return Optional.of(orderItemOptional.get());
        }

        // Then check for InventoryItem
        Optional<InventoryItem> inventoryItemOptional = inventoryItemRepository.findByQrCode(systemId);
        if (inventoryItemOptional.isPresent()) {
            InventoryItem inventoryItem = inventoryItemOptional.get();
            logger.info("Found InventoryItem by QR code: {} - ID: {}, Product: {}, Location: {}", 
                systemId, inventoryItem.getId(), inventoryItem.getProduct().getName(), 
                inventoryItem.getLocation().getName());
            
            // Try to find a matching OrderItem that needs this inventory item
            Optional<OrderItem> matchingOrderItem = findMatchingOrderItemForInventory(inventoryItem, orderId);
            if (matchingOrderItem.isPresent()) {
                OrderItem orderItem = matchingOrderItem.get();
                logger.info("SUCCESS: Found matching OrderItem {} for InventoryItem {}", orderItem.getId(), inventoryItem.getId());
                
                // Link the inventory item to the order item
                orderItem.setInventoryItem(inventoryItem);
                orderItem.setQrCodeData(systemId); // Set the QR code data
                orderItem = orderItemRepository.save(orderItem);
                
                // Reserve the inventory if not already reserved
                if (inventoryItem.getAvailableQuantity() >= orderItem.getRequestedQuantity()) {
                    inventoryItem.reserve(orderItem.getRequestedQuantity());
                    inventoryItemRepository.save(inventoryItem);
                    logger.info("Reserved {} units of inventory item {} for order item {}", 
                        orderItem.getRequestedQuantity(), inventoryItem.getId(), orderItem.getId());
                }
                
                logger.info("RETURNING OrderItem instead of InventoryItem: {}", orderItem.getId());
                return Optional.of(orderItem); // Return the linked OrderItem instead of InventoryItem
            } else {
                logger.warn("NO MATCH: No matching OrderItem found for InventoryItem {}. Returning InventoryItem.", inventoryItem.getId());
                // No matching order item found, return the inventory item
                return Optional.of(inventoryItem);
            }
        }

        logger.error("No object found for QR code: {}", systemId);
        return Optional.empty();
    }

    private Optional<OrderItem> findMatchingOrderItemForInventory(InventoryItem inventoryItem, Long orderId) {
        // Look for active order items with the same product that don't have inventory assigned yet
        List<OrderItemStatus> activeStatuses = Arrays.asList(
            OrderItemStatus.PENDING, 
            OrderItemStatus.IN_PROGRESS
        );
        
        logger.info("Looking for matching OrderItems for InventoryItem {} (product: {}, location: {}, contextOrderId: {})", 
            inventoryItem.getId(), inventoryItem.getProduct().getName(), inventoryItem.getLocation().getName(), orderId);
        
        List<OrderItem> candidates = orderItemRepository.findByProductAndStatusInAndInventoryItemIsNull(
            inventoryItem.getProduct(), 
            activeStatuses
        );
        
        logger.info("Found {} candidate OrderItems with same product and no inventory assigned", candidates.size());
        for (OrderItem candidate : candidates) {
            logger.info("Candidate OrderItem: ID={}, Order={} (OrderId={}), Product={}, SourceLocation={}, Status={}", 
                candidate.getId(), candidate.getOrder().getOrderNumber(), candidate.getOrder().getId(),
                candidate.getProduct().getName(), 
                candidate.getSourceLocation() != null ? candidate.getSourceLocation().getName() : "null",
                candidate.getStatus());
        }
        
        return candidates.stream()
        .filter(orderItem -> {
            // If orderId is provided, prioritize items from that specific order
            boolean orderMatch = orderId == null || orderItem.getOrder().getId().equals(orderId);
            
            // Additional checks if needed
            boolean locationMatch = orderItem.getSourceLocation() == null || 
                   orderItem.getSourceLocation().equals(inventoryItem.getLocation());
            
            logger.info("OrderItem {} checks: orderMatch={} (required: {}, actual: {}), locationMatch={} (required: {}, actual: {})", 
                orderItem.getId(), 
                orderMatch, orderId, orderItem.getOrder().getId(),
                locationMatch,
                orderItem.getSourceLocation() != null ? orderItem.getSourceLocation().getName() : "null",
                inventoryItem.getLocation().getName());
            
            return orderMatch && locationMatch;
        })
        // If orderId is provided, prioritize items from that order
        .sorted((a, b) -> {
            if (orderId != null) {
                boolean aIsFromTargetOrder = a.getOrder().getId().equals(orderId);
                boolean bIsFromTargetOrder = b.getOrder().getId().equals(orderId);
                if (aIsFromTargetOrder && !bIsFromTargetOrder) return -1;
                if (!aIsFromTargetOrder && bIsFromTargetOrder) return 1;
            }
            return 0;
        })
        .findFirst();
    }

    public OrderItem processOrderItemScan(Long orderItemId, String qrCode) {
        logger.info("Processing scan for OrderItem ID: {} with QR code: {}", orderItemId, qrCode);
        String systemId = qrCode.split(QR_DATA_SEPARATOR)[0];

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + orderItemId));

        if (orderItem.getRequiresExactInventory()) {
            if (orderItem.getInventoryItem() == null) {
                throw new IllegalStateException("OrderItem requires an exact inventory item, but none is assigned.");
            }
            if (orderItem.getInventoryItem().getQrCode().equals(systemId)) {
                throw new IllegalStateException("Incorrect item scanned. Expected QR: " + orderItem.getInventoryItem().getQrCode() + ", but got: " + systemId);
            }
            logger.info("Correct item scanned for exact-match OrderItem.");
        } else {
            if (orderItem.getInventoryItem() == null) {
                InventoryItem inventoryItem = inventoryItemRepository.findByQrCode(systemId)
                        .orElseThrow(() -> new ResourceNotFoundException("InventoryItem not found for QR code: " + systemId));
                
                if (!inventoryItem.getProduct().equals(orderItem.getProduct())) {
                    throw new IllegalStateException("Incorrect product type scanned. Expected " + orderItem.getProduct().getName() + ", got " + inventoryItem.getProduct().getName());
                }

                inventoryItem.reserve(orderItem.getRequestedQuantity());
                orderItem.setInventoryItem(inventoryItem);
                logger.info("Successfully assigned and reserved InventoryItem ID {} to flexible OrderItem ID {}", inventoryItem.getId(), orderItem.getId());
            }
        }

        orderItem.pick();
        return orderItemRepository.save(orderItem);
    }

    public OrderItem addOrderItem(Long orderId, Long productId, Integer requestedQuantity,
                                  Long sourceLocationId, Long destinationLocationId,
                                  BigDecimal unitPrice, String notes, Boolean requiresExactInventory) {

        Order order = getOrderById(orderId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        Location sourceLocation = (sourceLocationId != null) ? locationRepository.findById(sourceLocationId).orElse(null) : null;
        Location destinationLocation = (destinationLocationId != null) ? locationRepository.findById(destinationLocationId).orElse(null) : null;

        OrderItem orderItem = new OrderItem(order, orderItemRepository.findMaxLineNumberByOrder(order).orElse(0) + 1, product, requestedQuantity);
        orderItem.setSourceLocation(sourceLocation);
        orderItem.setDestinationLocation(destinationLocation);
        orderItem.setUnitPrice(unitPrice);
        orderItem.setNotes(notes);
        orderItem.setRequiresExactInventory(requiresExactInventory);

        if (requiresExactInventory) {
            if (sourceLocation == null) {
                throw new IllegalArgumentException("Source location is required for exact inventory matching.");
            }
            InventoryItem inventoryItem = inventoryItemRepository
                    .findFirstAvailableByProductAndLocation(product, sourceLocation)
                    .orElseThrow(() -> new IllegalStateException("No available stock for product " + product.getName() + " at location " + sourceLocation.getName() + " to create a specific stock order."));
            
            inventoryItem.reserve(requestedQuantity);
            orderItem.setInventoryItem(inventoryItem);
            logger.info("Reserved {} of {} from InventoryItem ID {}", requestedQuantity, product.getName(), inventoryItem.getId());
        }

        orderItemRepository.save(orderItem);
        updateOrderProgress(order);
        return orderItem;
    }

    public Order completeOrder(Long orderId, User user) {
        Order order = getOrderById(orderId);

        if (!order.canBeCompleted()) {
            throw new IllegalStateException("Order cannot be completed in current status: " + order.getStatus());
        }

        for (OrderItem item : order.getOrderItems()) {
            if (item.getInventoryItem() != null && item.isCompleted()) {
                InventoryItem inventoryItem = item.getInventoryItem();
                inventoryItem.fulfillReservationAndDecreaseStock(item.getCompletedQuantity());
                inventoryItemRepository.save(inventoryItem);
            }
        }

        OrderStatus oldStatus = order.getStatus();
        order.complete(user);
        orderRepository.save(order);

        createStatusHistory(order, oldStatus, order.getStatus(), user, "Order completed");
        createMovementHistoryForCompletedOrder(order);

        return order;
    }

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

    public Order getOrderById(Long id) { return orderRepository.findByIdWithDetails(id).orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id)); }
    public Order getOrderByNumber(String orderNumber) { return orderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber)); }
    public Page<Order> getAllOrders(Pageable pageable) { return orderRepository.findAll(pageable); }
    public List<Order> getActiveOrders() { return orderRepository.findActiveOrders(Arrays.asList(OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS, OrderStatus.ON_HOLD, OrderStatus.PARTIALLY_COMPLETED)); }
    public List<Order> getOrdersByUser(User user) { return orderRepository.findByAssignedTo(user); }
    public Order startOrder(Long orderId, User user) { Order order = getOrderById(orderId); if (!order.canBeStarted()) { throw new IllegalStateException("Order cannot be started in current status: " + order.getStatus()); } OrderStatus oldStatus = order.getStatus(); order.start(user); orderRepository.save(order); createStatusHistory(order, oldStatus, order.getStatus(), user, "Order started"); return order; }
    public Order cancelOrder(Long orderId, User user, String reason) { Order order = getOrderById(orderId); if (!order.canBeCancelled()) { throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus()); } OrderStatus oldStatus = order.getStatus(); order.cancel(user, reason); orderRepository.save(order); createStatusHistory(order, oldStatus, order.getStatus(), user, reason); return order; }
    public Order assignOrder(Long orderId, Long userId, User assignedBy) { Order order = getOrderById(orderId); User assignee = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId)); OrderStatus oldStatus = order.getStatus(); order.assign(assignee); orderRepository.save(order); if (oldStatus != order.getStatus()) { createStatusHistory(order, oldStatus, order.getStatus(), assignedBy, "Order assigned to " + assignee.getUsername()); } return order; }
    public OrderItem completeOrderItem(Long orderItemId, Integer completedQuantity, String completionNotes, String qrCodeData) { 
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + orderItemId)); 
        
        orderItem.complete(completedQuantity, completionNotes); 
        if (qrCodeData != null) { 
            orderItem.setQrCodeData(qrCodeData); 
        } 
        
        // Tworzymy MovementHistory dla pojedynczego item
        createMovementHistoryForOrderItem(orderItem);
        
        orderItemRepository.save(orderItem); 
        updateOrderProgress(orderItem.getOrder()); 
        return orderItem;
    }
    private void createStatusHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus, User changedBy, String reason) { statusHistoryRepository.save(new OrderStatusHistory(order, oldStatus, newStatus, changedBy, reason)); }
    private void updateOrderProgress(Order order) { order.updateProgress(); orderRepository.save(order); }
    private void createMovementHistoryForCompletedOrder(Order order) { 
        MovementType movementType = getMovementTypeForOrder(order.getType()); 
        for (OrderItem item : order.getOrderItems()) { 
            if (item.isCompleted() && item.getInventoryItem() != null) { 
                movementHistoryService.createSystemMovement(
                    item.getInventoryItem().getId(), 
                    movementType, 
                    null, 
                    item.getCompletedQuantity(), 
                    item.getSourceLocation(), 
                    item.getDestinationLocation(), 
                    "Completed via order: " + order.getOrderNumber(), 
                    order.getOrderNumber()
                ); 
            } 
        } 
    }

    private void createMovementHistoryForOrderItem(OrderItem orderItem) {
        try {
            if (orderItem.isCompleted()) {
                MovementType movementType = getMovementTypeForOrder(orderItem.getOrder().getType());
                
                if (orderItem.getInventoryItem() != null) {
                    // Standardowy przypadek z InventoryItem
                    movementHistoryService.createSystemMovement(
                        orderItem.getInventoryItem().getId(),
                        movementType,
                        null,
                        orderItem.getCompletedQuantity(),
                        orderItem.getSourceLocation(),
                        orderItem.getDestinationLocation(),
                        "Completed via order item: " + orderItem.getOrder().getOrderNumber() + " line " + orderItem.getLineNumber(),
                        orderItem.getOrder().getOrderNumber()
                    );
                } else {
                    // Przypadek bez InventoryItem - tworzymy ogólny wpis historii
                    logger.warn("Creating movement history without InventoryItem for OrderItem ID: {}", orderItem.getId());
                    // BEZPIECZNE TWORZENIE RUCHU BEZ INVENTORY ITEM
                    createOrderMovementWithoutInventoryItem(orderItem, movementType);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to create movement history for OrderItem ID: {}, Error: {}", orderItem.getId(), e.getMessage());
            // Nie przerywamy procesu, tylko logujemy błąd
        }
    }

    private void createOrderMovementWithoutInventoryItem(OrderItem orderItem, MovementType movementType) {
        try {
            // Tworzymy ruch w historii bez konkretnej pozycji magazynowej
            movementHistoryService.createOrderMovementHistory(
                movementType,
                orderItem.getCompletedQuantity(),
                orderItem.getSourceLocation(),
                orderItem.getDestinationLocation(),
                "Order item completed: " + orderItem.getProduct().getName() + " (SKU: " + orderItem.getProduct().getSku() + ")",
                orderItem.getOrder().getOrderNumber()
            );
            logger.info("Created order movement history for OrderItem ID: {} without InventoryItem", orderItem.getId());
        } catch (Exception e) {
            logger.error("Failed to create order movement history for OrderItem ID: {}, Error: {}", orderItem.getId(), e.getMessage());
        }
    }
    private MovementType getMovementTypeForOrder(OrderType orderType) { return switch (orderType) { case INBOUND -> MovementType.ORDER_RECEIPT; case OUTBOUND, PICK, PUTAWAY, ADJUSTMENT -> MovementType.ORDER_ISSUE; case TRANSFER -> MovementType.TRANSFER; case RETURN -> MovementType.ORDER_RETURN; default -> MovementType.ORDER_ISSUE; }; }
    public boolean canUserAccessOrder(Order order, User user) { return order.getCreatedBy().equals(user) || (order.getAssignedTo() != null && order.getAssignedTo().equals(user)) || user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")); }
    public String generateOrderNumber(OrderType type) { String prefix = type.name().substring(0, 3).toUpperCase(); String timestamp = String.valueOf(System.currentTimeMillis()); return prefix + "-" + timestamp.substring(timestamp.length() - 8); }
    public List<Order> getOverdueOrders() { List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS, OrderStatus.PARTIALLY_COMPLETED); return orderRepository.findOverdueOrders(LocalDateTime.now(), activeStatuses); }
    public List<Order> getHighPriorityOrders() { List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS, OrderStatus.PARTIALLY_COMPLETED); return orderRepository.findHighPriorityActiveOrders(activeStatuses); }
    public Optional<OrderItem> findOrderItemByQRCode(String qrCodeData) { return orderItemRepository.findByQrCodeData(qrCodeData); }
    public Page<Order> searchOrders(String searchTerm, Pageable pageable) { return orderRepository.searchOrders(searchTerm, pageable); }
    public List<Object[]> getOrderCountByStatus() { return orderRepository.getOrderCountByStatus(); }
}