package com.qrware.controller;

import com.qrware.domain.order.Order;
import com.qrware.domain.order.OrderType;
import com.qrware.domain.user.User;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.OrderDTO;
import com.qrware.service.OrderService;
import com.qrware.security.util.SecurityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private DTOMapper dtoMapper;

    @InjectMocks
    private OrderController orderController;

    private static MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeAll
    static void setUpStatic() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterAll
    static void closeStatic() {
        securityUtilsMock.close();
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenFoundAndAuthorized() {
        Long id = 1L;
        Order order = new Order();
        order.setId(id);
        User user = new User();
        user.setId(1L);
        OrderDTO dto = new OrderDTO();
        dto.setId(id);

        when(orderService.getOrderById(id)).thenReturn(order);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.canUserAccessOrder(order, user)).thenReturn(true);
        when(dtoMapper.toOrderDTO(order)).thenReturn(dto);

        ResponseEntity<?> response = orderController.getOrderById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getOrderById_ShouldReturnForbidden_WhenUnauthorized() {
        Long id = 1L;
        Order order = new Order();
        order.setId(id);
        User user = new User();
        user.setId(1L);

        when(orderService.getOrderById(id)).thenReturn(order);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.canUserAccessOrder(order, user)).thenReturn(false);

        ResponseEntity<?> response = orderController.getOrderById(id);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createOrder_ShouldCreateAndReturnOrder() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.INBOUND);
        request.setDescription("Test Order");

        User user = new User();
        user.setId(1L);
        Order order = new Order();
        order.setId(1L);
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.generateOrderNumber(OrderType.INBOUND)).thenReturn("ORD-001");
        when(orderService.createOrder(anyString(), eq(OrderType.INBOUND), eq("Test Order"), eq(user), any(), any(), any(), any(), any()))
            .thenReturn(order);
        when(dtoMapper.toOrderDTO(order)).thenReturn(dto);

        ResponseEntity<?> response = orderController.createOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
