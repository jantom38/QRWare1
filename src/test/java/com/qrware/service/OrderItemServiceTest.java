package com.qrware.service;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.order.OrderItem;
import com.qrware.domain.order.OrderItemStatus;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.order.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    @Test
    void getOrderItemById_ShouldReturnItem_WhenFound() {
        Long id = 1L;
        OrderItem item = new OrderItem();
        item.setId(id);
        when(orderItemRepository.findById(id)).thenReturn(Optional.of(item));

        OrderItem result = orderItemService.getOrderItemById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void getOrderItemById_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(orderItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderItemService.getOrderItemById(id));
    }

    @Test
    void linkInventoryToOrderItem_ShouldLinkAndSave() {
        Long orderItemId = 1L;
        Long inventoryItemId = 2L;
        OrderItem orderItem = new OrderItem();
        orderItem.setId(orderItemId);
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(inventoryItemId);

        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItem));
        when(inventoryItemRepository.findById(inventoryItemId)).thenReturn(Optional.of(inventoryItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItem result = orderItemService.linkInventoryToOrderItem(orderItemId, inventoryItemId);

        assertNotNull(result.getInventoryItem());
        assertEquals(inventoryItemId, result.getInventoryItem().getId());
        verify(orderItemRepository).save(orderItem);
    }

    @Test
    void pickOrderItem_ShouldUpdateStatus_WhenPending() {
        Long id = 1L;
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setStatus(OrderItemStatus.PENDING);
        
        when(orderItemRepository.findById(id)).thenReturn(Optional.of(item));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItem result = orderItemService.pickOrderItem(id);

        assertEquals(OrderItemStatus.IN_PROGRESS, result.getStatus());
        verify(orderItemRepository).save(item);
    }

    @Test
    void pickOrderItem_ShouldThrowException_WhenNotPending() {
        Long id = 1L;
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setStatus(OrderItemStatus.COMPLETED);
        
        when(orderItemRepository.findById(id)).thenReturn(Optional.of(item));

        assertThrows(IllegalStateException.class, () -> orderItemService.pickOrderItem(id));
    }
}
