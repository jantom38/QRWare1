package com.qrware.controller;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.MovementType;
import com.qrware.domain.product.Product;
import com.qrware.domain.warehouse.Location;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.InventoryItemDTO;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.inventory.MovementHistoryRepository;
import com.qrware.repository.product.ProductRepository;
import com.qrware.repository.warehouse.LocationRepository;
import com.qrware.service.MovementHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryItemRepository inventoryRepository;
    @Mock
    private MovementHistoryRepository movementHistoryRepository;
    @Mock
    private MovementHistoryService movementHistoryService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private DTOMapper dtoMapper;

    @InjectMocks
    private InventoryController inventoryController;

    @Test
    void getInventoryItemById_ShouldReturnItem_WhenFound() {
        Long id = 1L;
        InventoryItem item = new InventoryItem();
        item.setId(id);
        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setId(id);

        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));
        when(dtoMapper.toDTO(item)).thenReturn(dto);

        ResponseEntity<InventoryItemDTO> response = inventoryController.getInventoryItemById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().getId());
    }

    @Test
    void getInventoryItemById_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(inventoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryController.getInventoryItemById(id));
    }

    @Test
    void receiveStock_ShouldIncreaseQuantityAndCreateHistory() {
        Long id = 1L;
        int initialQty = 10;
        int addedQty = 5;
        
        InventoryItem item = new InventoryItem();
        item.setId(id);
        item.setQuantity(initialQty);
        Location location = new Location();
        item.setLocation(location);

        InventoryController.QuantityUpdateRequest request = new InventoryController.QuantityUpdateRequest();
        request.setQuantity(addedQty);
        request.setReason("Test Receipt");

        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setQuantity(initialQty + addedQty);

        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dtoMapper.toDTO(any(InventoryItem.class))).thenReturn(dto);

        ResponseEntity<InventoryItemDTO> response = inventoryController.receiveStock(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(initialQty + addedQty, item.getQuantity());
        
        verify(inventoryRepository).save(item);
        verify(movementHistoryService).createMovementHistory(
            eq(id), eq(MovementType.RECEIPT), eq(initialQty), eq(initialQty + addedQty), 
            isNull(), eq(location), eq("Test Receipt")
        );
    }

    @Test
    void issueStock_ShouldDecreaseQuantityAndCreateHistory() {
        Long id = 1L;
        int initialQty = 10;
        int issuedQty = 3;
        
        InventoryItem item = new InventoryItem();
        item.setId(id);
        item.setQuantity(initialQty);
        Location location = new Location();
        item.setLocation(location);

        InventoryController.QuantityUpdateRequest request = new InventoryController.QuantityUpdateRequest();
        request.setQuantity(issuedQty);
        request.setReason("Test Issue");

        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setQuantity(initialQty - issuedQty);

        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dtoMapper.toDTO(any(InventoryItem.class))).thenReturn(dto);

        ResponseEntity<InventoryItemDTO> response = inventoryController.issueStock(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(initialQty - issuedQty, item.getQuantity());
        
        verify(inventoryRepository).save(item);
        verify(movementHistoryService).createMovementHistory(
            eq(id), eq(MovementType.ISSUE), eq(initialQty), eq(initialQty - issuedQty), 
            eq(location), isNull(), eq("Test Issue")
        );
    }

    @Test
    void createInventoryItem_ShouldCreateAndReturnItem() {
        InventoryController.CreateInventoryRequest request = new InventoryController.CreateInventoryRequest();
        request.setProductId(1L);
        request.setLocationId(2L);
        request.setQuantity(100);

        Product product = new Product();
        product.setId(1L);
        Location location = new Location();
        location.setId(2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> {
            InventoryItem saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(inventoryRepository.findById(10L)).thenAnswer(inv -> Optional.of(inventoryRepository.save(new InventoryItem()))); 
        when(dtoMapper.toDTO(any(InventoryItem.class))).thenReturn(new InventoryItemDTO());

        ResponseEntity<InventoryItemDTO> response = inventoryController.createInventoryItem(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(inventoryRepository, atLeastOnce()).save(any(InventoryItem.class));
    }

    @Test
    void createInventoryItem_ShouldReturnError_WhenSaveFails() {
        InventoryController.CreateInventoryRequest request = new InventoryController.CreateInventoryRequest();
        request.setProductId(1L);
        request.setLocationId(2L);
        request.setQuantity(100);

        Product product = new Product();
        product.setId(1L);
        Location location = new Location();
        location.setId(2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(null);

        ResponseEntity<InventoryItemDTO> response = inventoryController.createInventoryItem(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
