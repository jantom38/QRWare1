package com.qrware.domain.order;

import com.qrware.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTest {

    private Order order;

    @Mock
    private User mockUser;

    @Mock
    private OrderItem mockItem1;

    @Mock
    private OrderItem mockItem2;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderNumber("ORD-001");
        order.setOrderItems(new ArrayList<>());
    }

    @Test
    @DisplayName("Should allow transition from CREATED to ASSIGNED")
    void canTransitionTo_CreatedToAssigned_ReturnsTrue() {
        order.setStatus(OrderStatus.CREATED);
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.ASSIGNED));
    }

    @Test
    @DisplayName("Should allow transition from CREATED to IN_PROGRESS")
    void canTransitionTo_CreatedToInProgress_ReturnsTrue() {
        order.setStatus(OrderStatus.CREATED);
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("Should allow transition from CREATED to CANCELLED")
    void canTransitionTo_CreatedToCancelled_ReturnsTrue() {
        order.setStatus(OrderStatus.CREATED);
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("Should NOT allow transition from CANCELLED to IN_PROGRESS")
    void canTransitionTo_CancelledToInProgress_ReturnsFalse() {
        order.setStatus(OrderStatus.CANCELLED);
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("Should NOT allow transition from COMPLETED to IN_PROGRESS")
    void canTransitionTo_CompletedToInProgress_ReturnsFalse() {
        order.setStatus(OrderStatus.COMPLETED);
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("Should allow transition from IN_PROGRESS to PARTIALLY_COMPLETED")
    void canTransitionTo_InProgressToPartiallyCompleted_ReturnsTrue() {
        order.setStatus(OrderStatus.IN_PROGRESS);
        assertTrue(OrderStatus.IN_PROGRESS.canTransitionTo(OrderStatus.PARTIALLY_COMPLETED));
    }

    @Test
    @DisplayName("Should calculate completion percentage correctly")
    void getCompletionPercentage_CalculatesCorrectly() {
        order.setTotalItems(10);
        order.setCompletedItems(5);
        assertEquals(50.0, order.getCompletionPercentage(), 0.01);
    }

    @Test
    @DisplayName("Should return 0% when total items is 0")
    void getCompletionPercentage_TotalItemsZero_ReturnsZero() {
        order.setTotalItems(0);
        order.setCompletedItems(0);
        assertEquals(0.0, order.getCompletionPercentage(), 0.01);
    }

    @Test
    @DisplayName("Should return 0% when total items is null")
    void getCompletionPercentage_TotalItemsNull_ReturnsZero() {
        order.setTotalItems(null);
        order.setCompletedItems(0);
        assertEquals(0.0, order.getCompletionPercentage(), 0.01);
    }

    @Test
    @DisplayName("Should update progress based on order items")
    void updateProgress_UpdatesCorrectly() {
        when(mockItem1.getCompletedQuantity()).thenReturn(10);
        when(mockItem1.getTotalValue()).thenReturn(new BigDecimal("100.00"));
        
        when(mockItem2.getCompletedQuantity()).thenReturn(5);
        when(mockItem2.getTotalValue()).thenReturn(new BigDecimal("50.00"));

        order.getOrderItems().add(mockItem1);
        order.getOrderItems().add(mockItem2);

        order.updateProgress();

        assertEquals(2, order.getTotalItems());
        assertEquals(2, order.getCompletedItems()); 
        assertEquals(new BigDecimal("150.00"), order.getEstimatedValue());
    }

    @Test
    @DisplayName("Should be startable when status is CREATED")
    void canBeStarted_StatusCreated_ReturnsTrue() {
        order.setStatus(OrderStatus.CREATED);
        assertTrue(order.canBeStarted());
    }

    @Test
    @DisplayName("Should be startable when status is ASSIGNED")
    void canBeStarted_StatusAssigned_ReturnsTrue() {
        order.setStatus(OrderStatus.ASSIGNED);
        assertTrue(order.canBeStarted());
    }

    @Test
    @DisplayName("Should NOT be startable when status is IN_PROGRESS")
    void canBeStarted_StatusInProgress_ReturnsFalse() {
        order.setStatus(OrderStatus.IN_PROGRESS);
        assertFalse(order.canBeStarted());
    }

    @Test
    @DisplayName("Should be cancellable when status is IN_PROGRESS")
    void canBeCancelled_StatusInProgress_ReturnsTrue() {
        order.setStatus(OrderStatus.IN_PROGRESS);
        assertTrue(order.canBeCancelled());
    }

    @Test
    @DisplayName("Should NOT be cancellable when status is COMPLETED")
    void canBeCancelled_StatusCompleted_ReturnsFalse() {
        order.setStatus(OrderStatus.COMPLETED);
        assertFalse(order.canBeCancelled());
    }

    @Test
    @DisplayName("Should NOT be cancellable when status is CANCELLED")
    void canBeCancelled_StatusCancelled_ReturnsFalse() {
        order.setStatus(OrderStatus.CANCELLED);
        assertFalse(order.canBeCancelled());
    }

    @Test
    @DisplayName("Should be completable when all items are completed")
    void canBeCompleted_AllItemsCompleted_ReturnsTrue() {
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setTotalItems(5);
        order.setCompletedItems(5);
        assertTrue(order.canBeCompleted());
    }

    @Test
    @DisplayName("Should NOT be completable when items mismatch")
    void canBeCompleted_ItemsMismatch_ReturnsFalse() {
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setTotalItems(5);
        order.setCompletedItems(4);
        assertFalse(order.canBeCompleted());
    }
}
