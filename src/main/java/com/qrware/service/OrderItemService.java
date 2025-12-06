package com.qrware.service;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.order.Order;
import com.qrware.domain.order.OrderItem;
import com.qrware.domain.order.OrderItemStatus;
import com.qrware.domain.product.Product;
import com.qrware.domain.warehouse.Location;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.order.OrderItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderItemService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderItemService.class);

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + id));
    }

    public List<OrderItem> getOrderItemsByOrder(Order order) {
        return orderItemRepository.findByOrderOrderByLineNumber(order);
    }

    public List<OrderItem> getOrderItemsByProduct(Product product) {
        return orderItemRepository.findByProduct(product);
    }

    public List<OrderItem> getActiveOrderItems() {
        List<OrderItemStatus> activeStatuses = List.of(
                OrderItemStatus.PENDING, OrderItemStatus.IN_PROGRESS,
                OrderItemStatus.PICKED, OrderItemStatus.PARTIALLY_COMPLETED
        );
        return orderItemRepository.findActiveOrderItems(activeStatuses);
    }

    public Page<OrderItem> searchOrderItems(String searchTerm, Pageable pageable) {
        return orderItemRepository.searchOrderItems(searchTerm, pageable);
    }

    public OrderItem linkInventoryToOrderItem(Long orderItemId, Long inventoryItemId) {
        OrderItem orderItem = getOrderItemById(orderItemId);
        InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + inventoryItemId));

        orderItem.setInventoryItem(inventoryItem);
        return orderItemRepository.save(orderItem);
    }

    public List<OrderItem> getItemsRequiringQRScan() {
        return orderItemRepository.findItemsRequiringQrScan();
    }

    public List<OrderItem> getItemsWithQRCode() {
        return orderItemRepository.findItemsWithQrCode();
    }

    public OrderItem pickOrderItem(Long orderItemId) {
        OrderItem orderItem = getOrderItemById(orderItemId);

        if (orderItem.getStatus() != OrderItemStatus.PENDING) {
            throw new IllegalStateException("Order item cannot be picked in current status: " + orderItem.getStatus());
        }

        orderItem.pick();
        return orderItemRepository.save(orderItem);
    }

    public OrderItem completeOrderItem(Long orderItemId, Integer completedQuantity, String notes) {
        OrderItem orderItem = getOrderItemById(orderItemId);

        if (!orderItem.canBeCompleted()) {
            throw new IllegalStateException("Order item cannot be completed in current status: " + orderItem.getStatus());
        }

        orderItem.complete(completedQuantity, notes);
        return orderItemRepository.save(orderItem);
    }

    public OrderItem cancelOrderItem(Long orderItemId, String reason) {
        OrderItem orderItem = getOrderItemById(orderItemId);
        orderItem.cancel(reason);
        return orderItemRepository.save(orderItem);
    }

    public OrderItem setBatchNumber(Long orderItemId, String batchNumber) {
        OrderItem orderItem = getOrderItemById(orderItemId);
        orderItem.setBatchNumber(batchNumber);
        return orderItemRepository.save(orderItem);
    }

    public OrderItem setSerialNumber(Long orderItemId, String serialNumber) {
        OrderItem orderItem = getOrderItemById(orderItemId);
        orderItem.setSerialNumber(serialNumber);
        return orderItemRepository.save(orderItem);
    }

    public OrderItem setExpiryDate(Long orderItemId, LocalDateTime expiryDate) {
        OrderItem orderItem = getOrderItemById(orderItemId);
        orderItem.setExpiryDate(expiryDate);
        return orderItemRepository.save(orderItem);
    }

    public List<OrderItem> getOrderItemsByBatch(String batchNumber) {
        return orderItemRepository.findByBatchNumber(batchNumber);
    }

    public Optional<OrderItem> getOrderItemBySerialNumber(String serialNumber) {
        return orderItemRepository.findBySerialNumber(serialNumber);
    }

    public List<OrderItem> getExpiringBatches(LocalDateTime date) {
        return orderItemRepository.findExpiringBatches(date);
    }

    public OrderItem setSourceLocation(Long orderItemId, Location sourceLocation) {
        OrderItem orderItem = getOrderItemById(orderItemId);
        orderItem.setSourceLocation(sourceLocation);
        return orderItemRepository.save(orderItem);
    }

    public OrderItem setDestinationLocation(Long orderItemId, Location destinationLocation) {
        OrderItem orderItem = getOrderItemById(orderItemId);
        orderItem.setDestinationLocation(destinationLocation);
        return orderItemRepository.save(orderItem);
    }

    public List<OrderItem> getOrderItemsByLocation(Location location) {
        return orderItemRepository.findByLocation(location);
    }

    public List<OrderItem> getPartiallyCompletedItems() {
        return orderItemRepository.findPartiallyCompletedItems();
    }

    public List<OrderItem> getPendingItems() {
        List<OrderItemStatus> pendingStatuses = List.of(
                OrderItemStatus.PENDING, OrderItemStatus.IN_PROGRESS
        );
        return orderItemRepository.findPendingItems(pendingStatuses);
    }

    public Long getTotalCompletedQuantityByProduct(Product product, LocalDateTime startDate, LocalDateTime endDate) {
        return orderItemRepository.getTotalCompletedQuantityByProduct(product, startDate, endDate);
    }

    public List<OrderItem> getItemsCompletedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return orderItemRepository.findItemsCompletedBetween(startDate, endDate);
    }

    public List<OrderItem> getItemsInProgressLongerThan(int hours) {
        return orderItemRepository.findItemsInProgressLongerThan(hours);
    }

    public Double getAveragePickToCompleteTime() {
        return orderItemRepository.getAveragePickToCompleteTimeMinutes();
    }

    public Long getOrderItemCountByStatus(OrderItemStatus status) {
        return orderItemRepository.countByStatus(status);
    }

    public List<Object[]> getOrderItemCountByStatus() {
        return orderItemRepository.getOrderItemCountByStatus();
    }

    public List<OrderItem> getOvercompletedItems() {
        return orderItemRepository.findOvercompletedItems();
    }

    public Long getOutstandingQuantityByProduct(Product product) {
        return orderItemRepository.getOutstandingQuantityByProduct(product);
    }

    public List<OrderItem> getRecentlyCompletedItems(LocalDateTime since) {
        return orderItemRepository.findRecentlyCompletedItems(since);
    }

    public boolean isOrderItemReadyForCompletion(Long orderItemId) {
        OrderItem orderItem = getOrderItemById(orderItemId);

        if (orderItem.requiresQRScan() && !orderItem.isQRScanned()) {
            return false;
        }

        return orderItem.canBeCompleted();
    }

    public boolean validateQuantityForCompletion(Long orderItemId, Integer quantity) {
        OrderItem orderItem = getOrderItemById(orderItemId);

        return quantity > 0 &&
                quantity <= orderItem.getRequestedQuantity() &&
                quantity > orderItem.getCompletedQuantity();
    }

    public String getNextBatchNumber(Product product) {
        String productCode = product.getSku().length() > 3 ?
                product.getSku().substring(0, 3).toUpperCase() :
                product.getSku().toUpperCase();

        String dateCode = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        String sequence = String.format("%03d", (int)(Math.random() * 999) + 1);

        return productCode + "-" + dateCode + "-" + sequence;
    }


    public Page<OrderItem> getOrderItemsWithFilters(Long orderId, Long productId,
                                                    OrderItemStatus status, Long sourceLocationId,
                                                    Long destinationLocationId, Pageable pageable) {
        return orderItemRepository.findOrderItemsWithFilters(
                orderId, productId, status, sourceLocationId, destinationLocationId, pageable);
    }

    public List<OrderItem> getOrderItemsByOrderAndStatus(Order order, OrderItemStatus status) {
        return orderItemRepository.findByOrderAndStatus(order, status);
    }

    public Optional<OrderItem> getOrderItemByOrderAndLineNumber(Order order, Integer lineNumber) {
        return orderItemRepository.findByOrderAndLineNumber(order, lineNumber);
    }
}
