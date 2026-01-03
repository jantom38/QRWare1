package com.qrware.controller;

import com.qrware.domain.order.*;
import com.qrware.domain.user.User;
import com.qrware.domain.warehouse.Location;
import com.qrware.dto.ApiResponse;
import com.qrware.dto.OrderDTO;
import com.qrware.dto.DTOMapper;
import com.qrware.service.OrderService;
import com.qrware.repository.user.UserRepository;
import com.qrware.repository.warehouse.LocationRepository;
import com.qrware.security.util.SecurityUtils;
import com.qrware.exception.ResourceNotFoundException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<Order> orders = orderService.getAllOrders(pageable);
            Page<OrderDTO> orderDTOs = orders.map(dtoMapper::toOrderDTO);
            return ResponseEntity.ok(ApiResponse.success(orderDTOs));
        } catch (Exception e) {
            logger.error("Failed to retrieve orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve orders: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            
            User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
            if (!orderService.canUserAccessOrder(order, currentUser)) {
                logger.warn("User {} attempted to access unauthorized order {}", currentUser.getUsername(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied to this order"));
            }
            
            Hibernate.initialize(order.getOrderItems());
            order.getOrderItems().forEach(item -> {
                Hibernate.initialize(item.getInventoryItem());
                if (item.getInventoryItem() != null) {
                    logger.info("Order item {} is linked to inventory item {} with QR code {}", 
                        item.getId(), item.getInventoryItem().getId(), item.getInventoryItem().getQrCode());
                }
            });

            OrderDTO orderDTO = dtoMapper.toOrderDTO(order);
            return ResponseEntity.ok(ApiResponse.success(orderDTO));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to retrieve order {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve order: " + e.getMessage()));
        }
    }

    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByNumber(@PathVariable String orderNumber) {
        try {
            Order order = orderService.getOrderByNumber(orderNumber);
            OrderDTO orderDTO = dtoMapper.toOrderDTO(order);
            return ResponseEntity.ok(ApiResponse.success(orderDTO));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to retrieve order by number {}", orderNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve order: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            User createdBy = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
            User assignedTo = request.getAssignedToId() != null ? 
                userRepository.findById(request.getAssignedToId()).orElse(null) : null;
            Location sourceLocation = request.getSourceLocationId() != null ?
                locationRepository.findById(request.getSourceLocationId()).orElse(null) : null;
            Location destinationLocation = request.getDestinationLocationId() != null ?
                locationRepository.findById(request.getDestinationLocationId()).orElse(null) : null;

            String orderNumber = request.getOrderNumber() != null ? 
                request.getOrderNumber() : orderService.generateOrderNumber(request.getType());

            Order order = orderService.createOrder(
                orderNumber,
                request.getType(),
                request.getDescription(),
                createdBy,
                assignedTo,
                sourceLocation,
                destinationLocation,
                request.getExpectedDate(),
                request.getPriority()
            );

            OrderDTO orderDTO = dtoMapper.toOrderDTO(order);
            logger.info("Order {} created successfully", order.getOrderNumber());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderDTO, "Order created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to create order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderDTO>> startOrder(@PathVariable Long id) {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
            Order order = orderService.startOrder(id, currentUser);
            OrderDTO orderDTO = dtoMapper.toOrderDTO(order);
            logger.info("Order {} started by user {}", id, currentUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order started successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to start order {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to start order: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderDTO>> completeOrder(@PathVariable Long id) {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
            Order order = orderService.completeOrder(id, currentUser);
            OrderDTO orderDTO = dtoMapper.toOrderDTO(order);
            logger.info("Order {} completed by user {}", id, currentUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order completed successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to complete order {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to complete order: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@PathVariable Long id, 
                                                           @Valid @RequestBody CancelOrderRequest request) {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
            Order order = orderService.cancelOrder(id, currentUser, request.getReason());
            OrderDTO orderDTO = dtoMapper.toOrderDTO(order);
            logger.info("Order {} cancelled by user {}", id, currentUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order cancelled successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to cancel order {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to cancel order: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ORDER_ASSIGN')")
    public ResponseEntity<ApiResponse<OrderDTO>> assignOrder(@PathVariable Long id,
                                                           @Valid @RequestBody AssignOrderRequest request) {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
            Order order = orderService.assignOrder(id, request.getAssignedToId(), currentUser);
            OrderDTO orderDTO = dtoMapper.toOrderDTO(order);
            logger.info("Order {} assigned to user {} by {}", id, request.getAssignedToId(), currentUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order assigned successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to assign order {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to assign order: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getActiveOrders() {
        try {
            List<Order> orders = orderService.getActiveOrders();
            List<OrderDTO> orderDTOs = orders.stream()
                .map(dtoMapper::toOrderDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(orderDTOs));
        } catch (Exception e) {
            logger.error("Failed to retrieve active orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve active orders: " + e.getMessage()));
        }
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders() {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
            List<Order> orders = orderService.getOrdersByUser(currentUser);
            List<OrderDTO> orderDTOs = orders.stream()
                .map(dtoMapper::toOrderDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(orderDTOs));
        } catch (Exception e) {
            logger.error("Failed to retrieve orders for user {}", SecurityUtils.getCurrentUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve user orders: " + e.getMessage()));
        }
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOverdueOrders() {
        try {
            List<Order> orders = orderService.getOverdueOrders();
            List<OrderDTO> orderDTOs = orders.stream()
                .map(dtoMapper::toOrderDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(orderDTOs));
        } catch (Exception e) {
            logger.error("Failed to retrieve overdue orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve overdue orders: " + e.getMessage()));
        }
    }

    @GetMapping("/high-priority")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getHighPriorityOrders() {
        try {
            List<Order> orders = orderService.getHighPriorityOrders();
            List<OrderDTO> orderDTOs = orders.stream()
                .map(dtoMapper::toOrderDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(orderDTOs));
        } catch (Exception e) {
            logger.error("Failed to retrieve high priority orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve high priority orders: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> searchOrders(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<Order> orders = orderService.searchOrders(q, pageable);
            Page<OrderDTO> orderDTOs = orders.map(dtoMapper::toOrderDTO);
            return ResponseEntity.ok(ApiResponse.success(orderDTOs));
        } catch (Exception e) {
            logger.error("Failed to search orders with query: {}", q, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search orders: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics/status")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<List<StatusCountDTO>>> getOrderStatistics() {
        try {
            List<Object[]> statusCounts = orderService.getOrderCountByStatus();
            List<StatusCountDTO> statistics = statusCounts.stream()
                .map(row -> new StatusCountDTO((OrderStatus) row[0], (Long) row[1]))
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            logger.error("Failed to retrieve order statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve order statistics: " + e.getMessage()));
        }
    }

    public static class CreateOrderRequest {
        @Size(max = 50)
        private String orderNumber;

        @NotNull
        private OrderType type;

        @Size(max = 500)
        private String description;

        private Long assignedToId;
        private Long sourceLocationId;
        private Long destinationLocationId;
        private LocalDateTime expectedDate;
        private OrderPriority priority = OrderPriority.NORMAL;

        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

        public OrderType getType() { return type; }
        public void setType(OrderType type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getAssignedToId() { return assignedToId; }
        public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

        public Long getSourceLocationId() { return sourceLocationId; }
        public void setSourceLocationId(Long sourceLocationId) { this.sourceLocationId = sourceLocationId; }

        public Long getDestinationLocationId() { return destinationLocationId; }
        public void setDestinationLocationId(Long destinationLocationId) { this.destinationLocationId = destinationLocationId; }

        public LocalDateTime getExpectedDate() { return expectedDate; }
        public void setExpectedDate(LocalDateTime expectedDate) { this.expectedDate = expectedDate; }

        public OrderPriority getPriority() { return priority; }
        public void setPriority(OrderPriority priority) { this.priority = priority; }
    }

    public static class CancelOrderRequest {
        @NotBlank
        @Size(max = 1000)
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class AssignOrderRequest {
        @NotNull
        private Long assignedToId;

        public Long getAssignedToId() { return assignedToId; }
        public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }
    }

    public static class StatusCountDTO {
        private OrderStatus status;
        private Long count;

        public StatusCountDTO(OrderStatus status, Long count) {
            this.status = status;
            this.count = count;
        }

        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }

        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}