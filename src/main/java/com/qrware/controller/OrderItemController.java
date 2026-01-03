package com.qrware.controller;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.order.*;
import com.qrware.dto.ApiResponse;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.InventoryItemDTO;
import com.qrware.dto.OrderItemDTO;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.service.OrderItemService;
import com.qrware.service.OrderService;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order-items")
@CrossOrigin(origins = "*")
public class OrderItemController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private DTOMapper dtoMapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<OrderItemDTO>> getOrderItemById(@PathVariable Long id) {
        try {
            OrderItem orderItem = orderItemService.getOrderItemById(id);
            OrderItemDTO orderItemDTO = dtoMapper.toOrderItemDTO(orderItem);
            return ResponseEntity.ok(ApiResponse.success(orderItemDTO));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve order item: " + e.getMessage()));
        }
    }

    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderItemDTO>> addOrderItem(@PathVariable Long orderId, 
                                                                @Valid @RequestBody CreateOrderItemRequest request) {
        try {
            OrderItem orderItem = orderService.addOrderItem(
                orderId,
                request.getProductId(),
                request.getRequestedQuantity(),
                request.getSourceLocationId(),
                request.getDestinationLocationId(),
                request.getUnitPrice(),
                request.getNotes(),
                request.getRequiresExactInventory()
            );

            OrderItemDTO orderItemDTO = dtoMapper.toOrderItemDTO(orderItem);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderItemDTO, "Order item added successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to add order item: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/pick")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderItemDTO>> pickOrderItem(@PathVariable Long id) {
        try {
            OrderItem orderItem = orderItemService.pickOrderItem(id);
            OrderItemDTO orderItemDTO = dtoMapper.toOrderItemDTO(orderItem);
            return ResponseEntity.ok(ApiResponse.success(orderItemDTO, "Order item picked successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to pick order item: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderItemDTO>> completeOrderItem(@PathVariable Long id,
                                                                     @Valid @RequestBody CompleteOrderItemRequest request) {
        try {
            OrderItem orderItem = orderService.completeOrderItem(
                id, 
                request.getCompletedQuantity(), 
                request.getCompletionNotes(),
                request.getQrCodeData()
            );

            OrderItemDTO orderItemDTO = dtoMapper.toOrderItemDTO(orderItem);
            return ResponseEntity.ok(ApiResponse.success(orderItemDTO, "Order item completed successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to complete order item: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderItemDTO>> cancelOrderItem(@PathVariable Long id,
                                                                   @Valid @RequestBody CancelOrderRequest request) {
        try {
            OrderItem orderItem = orderItemService.cancelOrderItem(id, request.getReason());
            OrderItemDTO orderItemDTO = dtoMapper.toOrderItemDTO(orderItem);
            return ResponseEntity.ok(ApiResponse.success(orderItemDTO, "Order item cancelled successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to cancel order item: " + e.getMessage()));
        }
    }

    @PostMapping("/scan-qr")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<ApiResponse<Object>> processOrderItemScan(@Valid @RequestBody ScanQRRequest request) {
        try {
            Optional<Object> resultOptional = orderService.processQRScan(request.getQrCodeData(), request.getOrderId());

            if (resultOptional.isPresent()) {
                Object result = resultOptional.get();
                Object dto;

                if (result instanceof OrderItem) {
                    dto = dtoMapper.toOrderItemDTO((OrderItem) result);
                } else if (result instanceof InventoryItem) {
                    dto = dtoMapper.toInventoryItemDTO((InventoryItem) result);
                } else {
                    dto = result;
                }
                
                return ResponseEntity.ok(ApiResponse.success(dto, "QR code processed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No object found for QR code: " + request.getQrCodeData()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process QR code: " + e.getMessage()));
        }
    }

    @PostMapping("/scan-generic")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<ApiResponse<Object>> scanQRCode(@Valid @RequestBody GenericScanRequest request) {
        try {
            Optional<Object> resultOptional = orderService.processQRScan(request.getQrCodeData());

            if (resultOptional.isPresent()) {
                Object result = resultOptional.get();
                Object dto;

                if (result instanceof OrderItem) {
                    dto = dtoMapper.toOrderItemDTO((OrderItem) result);
                } else if (result instanceof InventoryItem) {
                    dto = dtoMapper.toInventoryItemDTO((InventoryItem) result);
                } else {
                    dto = result;
                }
                
                return ResponseEntity.ok(ApiResponse.success(dto, "QR code processed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No object found for QR code: " + request.getQrCodeData()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process QR code: " + e.getMessage()));
        }
    }

    @GetMapping("/qr/{qrCodeData}")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<ApiResponse<OrderItemDTO>> getOrderItemByQR(@PathVariable String qrCodeData) {
        try {
            Optional<OrderItem> orderItem = orderService.findOrderItemByQRCode(qrCodeData);
            if (orderItem.isPresent()) {
                OrderItemDTO orderItemDTO = dtoMapper.toOrderItemDTO(orderItem.get());
                return ResponseEntity.ok(ApiResponse.success(orderItemDTO));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No order item found for QR code: " + qrCodeData));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to find order item by QR code: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/batch")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderItemDTO>> setBatchNumber(@PathVariable Long id,
                                                                  @Valid @RequestBody SetBatchRequest request) {
        try {
            OrderItem orderItem = orderItemService.setBatchNumber(id, request.getBatchNumber());
            OrderItemDTO orderItemDTO = dtoMapper.toOrderItemDTO(orderItem);
            return ResponseEntity.ok(ApiResponse.success(orderItemDTO, "Batch number set successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to set batch number: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/serial")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ResponseEntity<ApiResponse<OrderItemDTO>> setSerialNumber(@PathVariable Long id,
                                                                   @Valid @RequestBody SetSerialRequest request) {
        try {
            OrderItem orderItem = orderItemService.setSerialNumber(id, request.getSerialNumber());
            OrderItemDTO orderItemDTO = dtoMapper.toOrderItemDTO(orderItem);
            return ResponseEntity.ok(ApiResponse.success(orderItemDTO, "Serial number set successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to set serial number: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<List<OrderItemDTO>>> getActiveOrderItems() {
        try {
            List<OrderItem> orderItems = orderItemService.getActiveOrderItems();
            List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(dtoMapper::toOrderItemDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(orderItemDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve active order items: " + e.getMessage()));
        }
    }

    @GetMapping("/pending-qr")
    @PreAuthorize("hasAuthority('QR_SCAN')")
    public ResponseEntity<ApiResponse<List<OrderItemDTO>>> getItemsRequiringQRScan() {
        try {
            List<OrderItem> orderItems = orderItemService.getItemsRequiringQRScan();
            List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(dtoMapper::toOrderItemDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(orderItemDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve items requiring QR scan: " + e.getMessage()));
        }
    }

    @GetMapping("/partially-completed")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<List<OrderItemDTO>>> getPartiallyCompletedItems() {
        try {
            List<OrderItem> orderItems = orderItemService.getPartiallyCompletedItems();
            List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(dtoMapper::toOrderItemDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(orderItemDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve partially completed items: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<Page<OrderItemDTO>>> searchOrderItems(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<OrderItem> orderItems = orderItemService.searchOrderItems(q, pageable);
            Page<OrderItemDTO> orderItemDTOs = orderItems.map(dtoMapper::toOrderItemDTO);
            return ResponseEntity.ok(ApiResponse.success(orderItemDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search order items: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics/status")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<ApiResponse<List<ItemStatusCountDTO>>> getOrderItemStatistics() {
        try {
            List<Object[]> statusCounts = orderItemService.getOrderItemCountByStatus();
            List<ItemStatusCountDTO> statistics = statusCounts.stream()
                .map(row -> new ItemStatusCountDTO((OrderItemStatus) row[0], (Long) row[1]))
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve order item statistics: " + e.getMessage()));
        }
    }

    public static class CreateOrderItemRequest {
        @NotNull
        private Long productId;

        @NotNull
        @Min(1)
        private Integer requestedQuantity;

        private Long sourceLocationId;
        private Long destinationLocationId;
        private BigDecimal unitPrice;

        @Size(max = 500)
        private String notes;

        private Boolean requiresExactInventory = true;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getRequestedQuantity() { return requestedQuantity; }
        public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }
        public Long getSourceLocationId() { return sourceLocationId; }
        public void setSourceLocationId(Long sourceLocationId) { this.sourceLocationId = sourceLocationId; }
        public Long getDestinationLocationId() { return destinationLocationId; }
        public void setDestinationLocationId(Long destinationLocationId) { this.destinationLocationId = destinationLocationId; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Boolean getRequiresExactInventory() { return requiresExactInventory; }
        public void setRequiresExactInventory(Boolean requiresExactInventory) { this.requiresExactInventory = requiresExactInventory; }
    }

    public static class CompleteOrderItemRequest {
        @NotNull
        @Min(0)
        private Integer completedQuantity;

        @Size(max = 500)
        private String completionNotes;

        @Size(max = 200)
        private String qrCodeData;

        public Integer getCompletedQuantity() { return completedQuantity; }
        public void setCompletedQuantity(Integer completedQuantity) { this.completedQuantity = completedQuantity; }
        public String getCompletionNotes() { return completionNotes; }
        public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }
        public String getQrCodeData() { return qrCodeData; }
        public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    }

    public static class CancelOrderRequest {
        @NotNull
        @Size(max = 500)
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class ScanQRRequest {
        @NotNull
        @Size(max = 200)
        private String qrCodeData;
        
        private Long orderId;

        public String getQrCodeData() { return qrCodeData; }
        public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
    }

    public static class GenericScanRequest {
        @NotNull
        @Size(max = 200)
        private String qrCodeData;

        public String getQrCodeData() { return qrCodeData; }
        public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    }

    public static class SetBatchRequest {
        @NotNull
        @Size(max = 200)
        private String batchNumber;

        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    }

    public static class SetSerialRequest {
        @NotNull
        @Size(max = 200)
        private String serialNumber;

        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    }

    public static class ItemStatusCountDTO {
        private OrderItemStatus status;
        private Long count;

        public ItemStatusCountDTO(OrderItemStatus status, Long count) {
            this.status = status;
            this.count = count;
        }

        public OrderItemStatus getStatus() { return status; }
        public void setStatus(OrderItemStatus status) { this.status = status; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}