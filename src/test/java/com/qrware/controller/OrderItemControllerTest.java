package com.qrware.controller;

import com.qrware.domain.order.OrderItem;
import com.qrware.domain.order.OrderItemStatus;
import com.qrware.dto.ApiResponse;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.OrderItemDTO;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.service.OrderItemService;
import com.qrware.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private DTOMapper dtoMapper;

    @InjectMocks
    private OrderItemController orderItemController;

    @Test
    void getOrderItemById_ShouldReturnItem_WhenFound() {
        Long id = 1L;
        OrderItem item = new OrderItem();
        item.setId(id);
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(id);

        when(orderItemService.getOrderItemById(id)).thenReturn(item);
        when(dtoMapper.toOrderItemDTO(item)).thenReturn(dto);

        ResponseEntity<ApiResponse<OrderItemDTO>> response = orderItemController.getOrderItemById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(id, response.getBody().getData().getId());
    }

    @Test
    void getOrderItemById_ShouldReturnNotFound_WhenNotFound() {
        Long id = 1L;
        when(orderItemService.getOrderItemById(id)).thenThrow(new ResourceNotFoundException("OrderItem", "id", id));

        ResponseEntity<ApiResponse<OrderItemDTO>> response = orderItemController.getOrderItemById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void addOrderItem_ShouldCreateAndReturnItem() {
        Long orderId = 1L;
        OrderItemController.CreateOrderItemRequest request = new OrderItemController.CreateOrderItemRequest();
        request.setProductId(2L);
        request.setRequestedQuantity(5);
        request.setUnitPrice(BigDecimal.TEN);

        OrderItem item = new OrderItem();
        item.setId(10L);
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(10L);

        when(orderService.addOrderItem(eq(orderId), eq(2L), eq(5), any(), any(), eq(BigDecimal.TEN), any(), any()))
                .thenReturn(item);
        when(dtoMapper.toOrderItemDTO(item)).thenReturn(dto);

        ResponseEntity<ApiResponse<OrderItemDTO>> response = orderItemController.addOrderItem(orderId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(10L, response.getBody().getData().getId());
    }

    @Test
    void addOrderItem_ShouldReturnError_WhenServiceReturnsNull() {
        Long orderId = 1L;
        OrderItemController.CreateOrderItemRequest request = new OrderItemController.CreateOrderItemRequest();
        request.setProductId(2L);
        request.setRequestedQuantity(5);

        when(orderService.addOrderItem(eq(orderId), eq(2L), eq(5), any(), any(), any(), any(), any()))
                .thenReturn(null);

        ResponseEntity<ApiResponse<OrderItemDTO>> response = orderItemController.addOrderItem(orderId, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to create order item", response.getBody().getMessage());
    }
}
