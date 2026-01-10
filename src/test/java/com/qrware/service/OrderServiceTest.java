package com.qrware.service;

import com.qrware.domain.inventory.InventoryItem;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private MovementHistoryService movementHistoryService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_ShouldCreateAndReturnOrder() {
        String orderNumber = "ORD-001";
        User user = new User();
        user.setId(1L);
        
        when(orderRepository.existsByOrderNumber(orderNumber)).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(orderNumber, OrderType.INBOUND, "Test Order", user, null, null, null, LocalDateTime.now(), OrderPriority.NORMAL);

        assertNotNull(result);
        assertEquals(orderNumber, result.getOrderNumber());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(statusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenOrderNumberExists() {
        String orderNumber = "ORD-001";
        when(orderRepository.existsByOrderNumber(orderNumber)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
            orderService.createOrder(orderNumber, OrderType.INBOUND, "Test", new User(), null, null, null, null, null)
        );
    }

    @Test
    void addOrderItem_ShouldAddAndReturnOrderItem() {
        Long orderId = 1L;
        Long productId = 1L;
        Order order = new Order();
        order.setId(orderId);
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        
        when(orderRepository.findByIdWithDetails(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.findMaxLineNumberByOrder(order)).thenReturn(Optional.of(0));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItem result = orderService.addOrderItem(orderId, productId, 10, null, null, BigDecimal.TEN, "Notes", false);

        assertNotNull(result);
        assertEquals(product, result.getProduct());
        assertEquals(10, result.getRequestedQuantity());
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenFound() {
        Long id = 1L;
        Order order = new Order();
        order.setId(id);
        when(orderRepository.findByIdWithDetails(id)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void getOrderById_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(orderRepository.findByIdWithDetails(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(id));
    }
}
