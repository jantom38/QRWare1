package com.qrware.controller;

import com.qrware.domain.order.Order;
import com.qrware.domain.order.OrderType;
import com.qrware.domain.user.User;
import com.qrware.domain.warehouse.Location;
import com.qrware.dto.ApiResponse;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.OrderDTO;
import com.qrware.repository.user.UserRepository;
import com.qrware.repository.warehouse.LocationRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private DTOMapper dtoMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

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
        String orderNumber = "ORD-123";
        
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("authorizedUser");
        
        OrderDTO dto = new OrderDTO();
        dto.setId(id);
        dto.setOrderNumber(orderNumber);

        when(orderService.getOrderById(id)).thenReturn(order);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.canUserAccessOrder(order, user)).thenReturn(true);
        when(dtoMapper.toOrderDTO(order)).thenReturn(dto);

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.getOrderById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(id, response.getBody().getData().getId());
        assertEquals(orderNumber, response.getBody().getData().getOrderNumber());
    }

    @Test
    void getOrderById_ShouldReturnForbidden_WhenUnauthorized() {
        Long id = 1L;
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber("ORD-456");
        
        User user = new User();
        user.setId(1L);
        user.setUsername("unauthorizedUser");

        when(orderService.getOrderById(id)).thenReturn(order);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.canUserAccessOrder(order, user)).thenReturn(false);

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.getOrderById(id);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Access denied to this order", response.getBody().getMessage());
    }

    @Test
    void createOrder_ShouldCreateAndReturnOrder() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.INBOUND);
        request.setDescription("Test Order");

        User user = new User();
        user.setId(1L);
        user.setUsername("creatorUser");
        
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-001");
        
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        dto.setOrderNumber("ORD-001");

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.generateOrderNumber(OrderType.INBOUND)).thenReturn("ORD-001");
        when(orderService.createOrder(anyString(), eq(OrderType.INBOUND), eq("Test Order"), eq(user), any(), any(), any(), any(), any()))
            .thenReturn(order);
        when(dtoMapper.toOrderDTO(order)).thenReturn(dto);

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1L, response.getBody().getData().getId());
        assertEquals("ORD-001", response.getBody().getData().getOrderNumber());
    }

    @Test
    void createOrder_ShouldReturnError_WhenServiceReturnsNull() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.INBOUND);
        request.setDescription("Test Order");

        User user = new User();
        user.setId(1L);
        user.setUsername("creatorUser");

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.generateOrderNumber(OrderType.INBOUND)).thenReturn("ORD-001");
        when(orderService.createOrder(anyString(), eq(OrderType.INBOUND), eq("Test Order"), eq(user), any(), any(), any(), any(), any()))
            .thenReturn(null);

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to create order", response.getBody().getMessage());
    }

    @Test
    void getOrderById_ShouldReturnUnauthorized_WhenNoCurrentUser() {
        Long id = 1L;
        Order order = new Order();
        order.setId(id);

        when(orderService.getOrderById(id)).thenReturn(order);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.getOrderById(id);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void createOrder_ShouldCreateOutboundOrder() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.OUTBOUND);
        request.setDescription("Outbound Order");

        User user = new User();
        user.setId(1L);
        
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("OUT-001");
        order.setType(OrderType.OUTBOUND);
        
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        dto.setOrderNumber("OUT-001");

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.generateOrderNumber(OrderType.OUTBOUND)).thenReturn("OUT-001");
        when(orderService.createOrder(anyString(), eq(OrderType.OUTBOUND), anyString(), eq(user), any(), any(), any(), any(), any()))
            .thenReturn(order);
        when(dtoMapper.toOrderDTO(order)).thenReturn(dto);

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("OUT-001", response.getBody().getData().getOrderNumber());
    }

    @Test
    void createOrder_ShouldCreateTransferOrder() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.TRANSFER);
        request.setDescription("Transfer Order");
        request.setSourceLocationId(1L);
        request.setDestinationLocationId(2L);

        User user = new User();
        user.setId(1L);
        
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("TRF-001");
        
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.generateOrderNumber(OrderType.TRANSFER)).thenReturn("TRF-001");
        Location src = new Location();
        src.setId(1L);
        Location dst = new Location();
        dst.setId(2L);

        when(locationRepository.findById(1L)).thenReturn(Optional.of(src));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(dst));

        when(orderService.createOrder(
            anyString(),
            eq(OrderType.TRANSFER),
            anyString(),
            eq(user),
            isNull(),
            eq(src),
            eq(dst),
            isNull(),
            any(com.qrware.domain.order.OrderPriority.class)
        )).thenReturn(order);
        when(dtoMapper.toOrderDTO(order)).thenReturn(dto);

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createOrder_ShouldCreatePickOrder() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.PICK);
        request.setDescription("Pick Order");

        User user = new User();
        user.setId(1L);
        
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("PICK-001");
        
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.generateOrderNumber(OrderType.PICK)).thenReturn("PICK-001");
        when(orderService.createOrder(anyString(), eq(OrderType.PICK), anyString(), eq(user), any(), any(), any(), any(), any()))
            .thenReturn(order);
        when(dtoMapper.toOrderDTO(order)).thenReturn(dto);

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createOrder_ShouldCreateOrderWithUrgentPriority() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.OUTBOUND);
        request.setDescription("Urgent Order");
        request.setPriority(com.qrware.domain.order.OrderPriority.URGENT);

        User user = new User();
        user.setId(1L);
        
        Order order = new Order();
        order.setId(1L);
        
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.generateOrderNumber(OrderType.OUTBOUND)).thenReturn("ORD-001");
        when(orderService.createOrder(anyString(), any(), anyString(), eq(user), any(), any(), any(), any(), eq(com.qrware.domain.order.OrderPriority.URGENT)))
            .thenReturn(order);
        when(dtoMapper.toOrderDTO(order)).thenReturn(dto);

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createOrder_ShouldCreateOrderWithAssignedUser() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.PICK);
        request.setDescription("Assigned Order");
        request.setAssignedToId(2L);

        User creator = new User();
        creator.setUsername("testUser");
        creator.setId(1L);
        
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("TEST-001");
        
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(creator));
        when(orderService.generateOrderNumber(OrderType.PICK)).thenReturn("ORD-001");
        User assignee = new User();
        assignee.setId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));

        when(orderService.createOrder(
            anyString(),
            any(OrderType.class),
            anyString(),
            eq(creator),
            eq(assignee),
            isNull(),
            isNull(),
            isNull(),
            any(com.qrware.domain.order.OrderPriority.class)
        )).thenReturn(order);
        when(dtoMapper.toOrderDTO(order)).thenReturn(dto);

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createOrder_ShouldReturnUnauthorized_WhenNoCurrentUser() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.INBOUND);

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void createOrder_ShouldReturnError_WhenServiceThrowsException() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setType(OrderType.INBOUND);
        request.setDescription("Test Order");

        User user = new User();
        user.setId(1L);

        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(user));
        when(orderService.generateOrderNumber(OrderType.INBOUND)).thenReturn("ORD-001");
        when(orderService.createOrder(anyString(), any(), anyString(), eq(user), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Database error"));
    }
}
