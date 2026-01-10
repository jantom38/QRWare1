package com.qrware.service;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.MovementHistory;
import com.qrware.domain.inventory.MovementType;
import com.qrware.domain.warehouse.Location;
import com.qrware.dto.DTOMapper;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.inventory.MovementHistoryRepository;
import com.qrware.security.util.SecurityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementHistoryServiceTest {

    @Mock
    private MovementHistoryRepository movementHistoryRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private DTOMapper dtoMapper;

    @InjectMocks
    private MovementHistoryService movementHistoryService;

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
    void createMovementHistory_ShouldCreateAndSave() {
        Long inventoryItemId = 1L;
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(inventoryItemId);
        
        when(inventoryItemRepository.findById(inventoryItemId)).thenReturn(Optional.of(inventoryItem));
        when(movementHistoryRepository.save(any(MovementHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        securityUtilsMock.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of("testuser"));

        MovementHistory result = movementHistoryService.createMovementHistory(
            inventoryItemId, MovementType.ADJUSTMENT, 10, 15, null, null, "Test Reason"
        );

        assertNotNull(result);
        assertEquals(inventoryItem, result.getInventoryItem());
        assertEquals(MovementType.ADJUSTMENT, result.getMovementType());
        assertEquals(5, result.getQuantityChanged());
        assertEquals("testuser", result.getUserId());
        verify(movementHistoryRepository).save(any(MovementHistory.class));
    }

    @Test
    void approveMovement_ShouldApprove_WhenNotApproved() {
        Long movementId = 1L;
        MovementHistory movement = new MovementHistory();
        movement.setId(movementId);
        movement.setApproved(false);
        
        when(movementHistoryRepository.findById(movementId)).thenReturn(Optional.of(movement));
        when(movementHistoryRepository.save(any(MovementHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        securityUtilsMock.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of("admin"));

        MovementHistory result = movementHistoryService.approveMovement(movementId, "Approved");

        assertTrue(result.getApproved());
        verify(movementHistoryRepository).save(movement);
    }

    @Test
    void approveMovement_ShouldThrowException_WhenAlreadyApproved() {
        Long movementId = 1L;
        MovementHistory movement = new MovementHistory();
        movement.setId(movementId);
        movement.setApproved(true);
        
        when(movementHistoryRepository.findById(movementId)).thenReturn(Optional.of(movement));

        assertThrows(IllegalStateException.class, () -> movementHistoryService.approveMovement(movementId, "Approved"));
    }
}
