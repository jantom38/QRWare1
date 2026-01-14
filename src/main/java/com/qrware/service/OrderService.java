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
        String systemId = qrCode.split(QR_DATA_SEPARATOR)[0];

        Optional<OrderItem> orderItemOptional = orderItemRepository.findByQrCodeData(systemId);
        if (orderItemOptional.isPresent()) {
            return Optional.of(orderItemOptional.get());
        }

        Optional<InventoryItem> inventoryItemOptional = inventoryItemRepository.findByQrCode(systemId);
        if (inventoryItemOptional.isPresent()) {
            InventoryItem inventoryItem = inventoryItemOptional.get();

            Optional<OrderItem> matchingOrderItem = findMatchingOrderItemForInventory(inventoryItem, orderId);

            if (matchingOrderItem.isPresent()) {
                OrderItem orderItem = matchingOrderItem.get();

                orderItem.setInventoryItem(inventoryItem);
                orderItem.setQrCodeData(systemId);
                orderItem = orderItemRepository.save(orderItem);

                if (inventoryItem.getAvailableQuantity() >= orderItem.getRequestedQuantity()) {
                    inventoryItem.reserve(orderItem.getRequestedQuantity());
                    inventoryItemRepository.save(inventoryItem);
                }

                return Optional.of(orderItem);
            } else {
                return Optional.of(inventoryItem);
            }
        }

        return Optional.empty();
    }

    private Optional<OrderItem> findMatchingOrderItemForInventory(InventoryItem inventoryItem, Long orderId) {
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
            boolean orderMatch = orderId == null || orderItem.getOrder().getId().equals(orderId);
            
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
            // Reload item to ensure we have the latest relationships (especially inventoryItem)
            // This is crucial within the same transaction if the item was modified separately.
            OrderItem freshItem = orderItemRepository.findById(item.getId()).orElse(item);

            if (freshItem.getInventoryItem() != null && freshItem.isCompleted()) {
                InventoryItem inventoryItem = freshItem.getInventoryItem();
                // If item was reserved, we fulfill reservation.
                // If item was NOT reserved (e.g. flexible pick), we just decrease stock.
                // TRANSFER / PUTAWAY should MOVE stock between locations, not remove it from inventory.
                if (order.getType() == OrderType.TRANSFER || order.getType() == OrderType.PUTAWAY) {
                    Location from = freshItem.getSourceLocation() != null ? freshItem.getSourceLocation() : order.getSourceLocation();
                    Location to = freshItem.getDestinationLocation() != null ? freshItem.getDestinationLocation() : order.getDestinationLocation();
                    if (to == null) {
                        throw new IllegalStateException("Destination location is required for " + order.getType());
                    }

                    int movedQty = freshItem.getCompletedQuantity();
                    if (inventoryItem.getQuantity() == null || inventoryItem.getQuantity() < movedQty) {
                        throw new IllegalStateException("Not enough stock to transfer. inventoryItemId=" + inventoryItem.getId() + ", qty=" + inventoryItem.getQuantity() + ", movedQty=" + movedQty);
                    }

                    // Release reservation without decreasing stock total
                    if (inventoryItem.getReservedQuantity() != null && inventoryItem.getReservedQuantity() >= movedQty) {
                        inventoryItem.setReservedQuantity(inventoryItem.getReservedQuantity() - movedQty);
                    }

                    // If we are moving the whole remaining quantity of this inventory record, just move the record
                    if (inventoryItem.getQuantity() != null && inventoryItem.getQuantity() == movedQty) {
                        Location effectiveFrom = from != null ? from : inventoryItem.getLocation();

                        inventoryItem.move(to, "Order relocation: " + order.getOrderNumber());
                        inventoryItemRepository.save(inventoryItem);

                        // Log transfer movement for order types (MOVE would also be created by InventoryItem.move(), but we want TRANSFER for relocation orders)
                        try {
                            movementHistoryService.createSystemMovement(
                                    inventoryItem.getId(),
                                    MovementType.TRANSFER,
                                    null,
                                    movedQty,
                                    effectiveFrom,
                                    to,
                                    "Transfer via order: " + order.getOrderNumber(),
                                    order.getOrderNumber()
                            );
                        } catch (Exception e) {
                            logger.warn("Failed to create movement history for transfer: {}", e.getMessage());
                        }
                    } else {
                        // Partial move: split stock -> decrease source, increase/create destination
                        inventoryItem.setQuantity(inventoryItem.getQuantity() - movedQty);
                        inventoryItemRepository.save(inventoryItem);

                        InventoryItem destItem = inventoryItemRepository.findFirstAvailableByProductAndLocation(inventoryItem.getProduct(), to)
                                .orElseGet(() -> {
                                    InventoryItem ni = new InventoryItem();
                                    ni.setProduct(inventoryItem.getProduct());
                                    ni.setLocation(to);
                                    ni.setStatus(inventoryItem.getStatus());
                                    ni.setReceivedDate(java.time.LocalDate.now());
                                    ni.setBatchNumber(inventoryItem.getBatchNumber());
                                    ni.setLotNumber(inventoryItem.getLotNumber());
                                    ni.setSerialNumber(inventoryItem.getSerialNumber());
                                    ni.setUnitCost(inventoryItem.getUnitCost());
                                    ni.setSupplierReference(inventoryItem.getSupplierReference());
                                    ni.setManufacturer(inventoryItem.getManufacturer());
                                    // qr_code in inventory_items is NOT NULL in your DB -> placeholder until manual generation
                                    // qr_code is NOT NULL + UNIQUE in your DB -> use unique placeholder and replace manually later
                                    ni.setQrCode("PENDING:TRF:" + order.getOrderNumber() + ":" + inventoryItem.getId() + ":" + java.util.UUID.randomUUID().toString().substring(0, 8));
                                    ni.setReservedQuantity(0);
                                    ni.setQuantity(0);
                                    return ni;
                                });
                        destItem.setQuantity((destItem.getQuantity() != null ? destItem.getQuantity() : 0) + movedQty);
                        inventoryItemRepository.save(destItem);

                        // Log movement
                        try {
                            movementHistoryService.createSystemMovement(
                                    inventoryItem.getId(),
                                    MovementType.TRANSFER,
                                    null,
                                    movedQty,
                                    from != null ? from : inventoryItem.getLocation(),
                                    to,
                                    "Transfer via order: " + order.getOrderNumber(),
                                    order.getOrderNumber()
                            );
                        } catch (Exception e) {
                            logger.warn("Failed to create movement history for transfer: {}", e.getMessage());
                        }
                    }
                } else {
                    // Default behaviour: decrease stock (outbound)
                    if (freshItem.getRequiresExactInventory()) {
                         inventoryItem.fulfillReservationAndDecreaseStock(freshItem.getCompletedQuantity());
                    } else {
                         if (inventoryItem.getReservedQuantity() >= freshItem.getCompletedQuantity()) {
                             inventoryItem.fulfillReservationAndDecreaseStock(freshItem.getCompletedQuantity());
                         } else {
                             inventoryItem.setQuantity(inventoryItem.getQuantity() - freshItem.getCompletedQuantity());
                         }
                    }
                    inventoryItemRepository.save(inventoryItem);
                }
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

    public Order updateOrder(Long orderId,
                             String description,
                             OrderPriority priority,
                             User assignedTo,
                             Location sourceLocation,
                             Location destinationLocation,
                             LocalDateTime expectedDate,
                             User updatedBy) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot edit order in status: " + order.getStatus());
        }

        if (description != null) {
            order.setDescription(description);
        }
        if (priority != null) {
            order.setPriority(priority);
        }

        order.setAssignedTo(assignedTo);
        order.setSourceLocation(sourceLocation);
        order.setDestinationLocation(destinationLocation);
        order.setExpectedDate(expectedDate);

        order = orderRepository.save(order);
        // zapis historii jako „aktualizacja” (bez zmiany statusu)
        createStatusHistory(order, order.getStatus(), order.getStatus(), updatedBy, "Order updated");

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
            
            // Jeśli nie mamy przypisanego InventoryItem, a podano kod QR, spróbujmy go znaleźć i przypisać
            if (orderItem.getInventoryItem() == null) {
                Optional<InventoryItem> inventoryItemOpt = inventoryItemRepository.findByQrCode(qrCodeData);
                if (inventoryItemOpt.isPresent()) {
                    orderItem.setInventoryItem(inventoryItemOpt.get());
                } else {
                     logger.warn("Completed OrderItem {} with QR code {} but no InventoryItem found.", orderItemId, qrCodeData);
                }
            }
        } 
        
        orderItemRepository.save(orderItem); 
        updateOrderProgress(orderItem.getOrder()); 
        return orderItem; 
    }

    private void createStatusHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus, User changedBy, String reason) { statusHistoryRepository.save(new OrderStatusHistory(order, oldStatus, newStatus, changedBy, reason)); }
    private void updateOrderProgress(Order order) { order.updateProgress(); orderRepository.save(order); }
    private void createMovementHistoryForCompletedOrder(Order order) {
        // For TRANSFER/PUTAWAY we create movement history during the transfer logic (so we can reference from/to correctly and avoid duplicates).
        if (order.getType() == OrderType.TRANSFER || order.getType() == OrderType.PUTAWAY) {
            return;
        }
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
    private MovementType getMovementTypeForOrder(OrderType orderType) { return switch (orderType) { case INBOUND -> MovementType.ORDER_RECEIPT; case OUTBOUND, PICK, ADJUSTMENT -> MovementType.ORDER_ISSUE; case PUTAWAY -> MovementType.PUTAWAY; case TRANSFER -> MovementType.TRANSFER; case RETURN -> MovementType.ORDER_RETURN; default -> MovementType.ORDER_ISSUE; }; }
    public boolean canUserAccessOrder(Order order, User user) { return order.getCreatedBy().equals(user) || (order.getAssignedTo() != null && order.getAssignedTo().equals(user)) || user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")); }
    public String generateOrderNumber(OrderType type) { String prefix = type.name().substring(0, 3).toUpperCase(); String timestamp = String.valueOf(System.currentTimeMillis()); return prefix + "-" + timestamp.substring(timestamp.length() - 8); }
    public List<Order> getOverdueOrders() { List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS, OrderStatus.PARTIALLY_COMPLETED); return orderRepository.findOverdueOrders(LocalDateTime.now(), activeStatuses); }
    public List<Order> getHighPriorityOrders() { List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS, OrderStatus.PARTIALLY_COMPLETED); return orderRepository.findHighPriorityActiveOrders(activeStatuses); }
    public Optional<OrderItem> findOrderItemByQRCode(String qrCodeData) { return orderItemRepository.findByQrCodeData(qrCodeData); }
    public Page<Order> searchOrders(String searchTerm, Pageable pageable) { return orderRepository.searchOrders(searchTerm, pageable); }
    public List<Object[]> getOrderCountByStatus() { return orderRepository.getOrderCountByStatus(); }
}