package com.qrware.controller;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.qr.QRCodeData;
import com.qrware.domain.qr.QRCodeType;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.QRCodeDTO;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.order.OrderItemRepository;
import com.qrware.repository.qr.QRCodeDataRepository;
import com.qrware.service.FileStorageService;
import com.qrware.service.QRCodeGenerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRCodeControllerTest {

    @Mock
    private QRCodeDataRepository qrCodeRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private DTOMapper dtoMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private QRCodeGenerationService qrCodeGenerationService;

    @InjectMocks
    private QRCodeController qrCodeController;

    // ==================== GET ALL QR CODES ====================

    @Test
    void getAllQRCodes_ShouldReturnPagedResults() {
        QRCodeData qrCode = new QRCodeData();
        qrCode.setId(1L);
        qrCode.setCode("QR-001");
        
        Page<QRCodeData> page = new PageImpl<>(List.of(qrCode));
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(1L);
        
        when(qrCodeRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);
        when(dtoMapper.toDTO(any(QRCodeData.class))).thenReturn(dto);

        ResponseEntity<Page<QRCodeDTO>> response = qrCodeController.getAllQRCodes(Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    // ==================== GET BY ID ====================

    @Test
    void getQRCodeById_ShouldReturnQRCode_WhenFound() {
        Long id = 1L;
        QRCodeData qrCode = new QRCodeData();
        qrCode.setId(id);
        qrCode.setCode("QR-001");
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(id);
        
        when(qrCodeRepository.findById(id)).thenReturn(Optional.of(qrCode));
        when(dtoMapper.toDTO(qrCode)).thenReturn(dto);

        ResponseEntity<QRCodeDTO> response = qrCodeController.getQRCodeById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().getId());
    }

    @Test
    void getQRCodeById_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(qrCodeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> qrCodeController.getQRCodeById(id));
    }

    // ==================== GET BY CODE (SCAN) ====================

    @Test
    void getQRCodeByCode_ShouldReturnQRCodeAndIncrementScanCount() {
        String code = "QR-001";
        QRCodeData qrCode = new QRCodeData();
        qrCode.setId(1L);
        qrCode.setCode(code);
        qrCode.setScanCount(5L);
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(1L);
        
        when(qrCodeRepository.findByCode(code)).thenReturn(Optional.of(qrCode));
        when(qrCodeRepository.save(any(QRCodeData.class))).thenReturn(qrCode);
        when(dtoMapper.toDTO(any(QRCodeData.class))).thenReturn(dto);

        ResponseEntity<QRCodeDTO> response = qrCodeController.getQRCodeByCode(code);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6L, qrCode.getScanCount()); // Should be incremented
        verify(qrCodeRepository).save(qrCode);
    }

    @Test
    void getQRCodeByCode_ShouldThrowException_WhenCodeNotFound() {
        String code = "INVALID-CODE";
        when(qrCodeRepository.findByCode(code)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> qrCodeController.getQRCodeByCode(code));
    }

    // ==================== GET BY ENTITY ====================

    @Test
    void getQRCodeByEntity_ShouldReturnQRCode_WhenFound() {
        String entityType = "inventory_item";
        Long entityId = 1L;
        
        QRCodeData qrCode = new QRCodeData();
        qrCode.setId(1L);
        qrCode.setEntityType(entityType);
        qrCode.setEntityId(entityId);
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(1L);
        
        when(qrCodeRepository.findByEntityTypeAndEntityId(entityType, entityId)).thenReturn(Optional.of(qrCode));
        when(dtoMapper.toDTO(qrCode)).thenReturn(dto);

        ResponseEntity<QRCodeDTO> response = qrCodeController.getQRCodeByEntity(entityType, entityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getQRCodeByEntity_ShouldThrowException_WhenNotFound() {
        when(qrCodeRepository.findByEntityTypeAndEntityId(anyString(), any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> qrCodeController.getQRCodeByEntity("unknown", 999L));
    }

    // ==================== GET ACTIVE QR CODES ====================

    @Test
    void getActiveQRCodes_ShouldReturnOnlyActiveCodes() {
        QRCodeData qrCode1 = new QRCodeData();
        qrCode1.setId(1L);
        qrCode1.setActive(true);
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(1L);
        
        when(qrCodeRepository.findByActiveTrue()).thenReturn(List.of(qrCode1));
        when(dtoMapper.toDTO(any(QRCodeData.class))).thenReturn(dto);

        ResponseEntity<List<QRCodeDTO>> response = qrCodeController.getActiveQRCodes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ==================== GET BY TYPE ====================

    @Test
    void getQRCodesByType_ShouldReturnFilteredList() {
        QRCodeType type = QRCodeType.INVENTORY_ITEM;
        QRCodeData qrCode = new QRCodeData();
        qrCode.setId(1L);
        qrCode.setType(type);
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(1L);
        
        when(qrCodeRepository.findByType(type)).thenReturn(List.of(qrCode));
        when(dtoMapper.toDTO(any(QRCodeData.class))).thenReturn(dto);

        ResponseEntity<List<QRCodeDTO>> response = qrCodeController.getQRCodesByType(type);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ==================== GENERATE QR CODE ====================

    @Test
    void generateQRCode_ShouldGenerateAndReturnQRCode_WhenAutoGenerate() {
        QRCodeController.GenerateQRRequest request = new QRCodeController.GenerateQRRequest();
        request.setData("test-data");
        request.setType(QRCodeType.INVENTORY_ITEM);
        request.setEntityType("inventory_item");
        request.setEntityId(1L);
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(1L);
        
        QRCodeData generatedQR = new QRCodeData();
        generatedQR.setId(1L);
        generatedQR.setCode("AUTO-GEN-001");
        generatedQR.setData("test-data");
        generatedQR.setEntityId(1L);
        generatedQR.setEntityType("inventory_item");
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(1L);
        dto.setCode("AUTO-GEN-001");
        
        // ZMIANA: Dodano argumenty do mocka, aby pasowały do wywołania w kontrolerze
        when(qrCodeGenerationService.generateQRCodeSync(
            anyString(), any(), anyString(), any(), anyString(), any(), any()
        )).thenReturn(generatedQR);
        
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(inventoryItem));
        when(dtoMapper.toDTO(generatedQR)).thenReturn(dto);

        ResponseEntity<?> response = qrCodeController.generateQRCode(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof QRCodeDTO);
        assertEquals("AUTO-GEN-001", ((QRCodeDTO)response.getBody()).getCode());
    }

    @Test
    void generateQRCode_ShouldSaveManualCode_WhenCodeProvided() {
        QRCodeController.GenerateQRRequest request = new QRCodeController.GenerateQRRequest();
        request.setCode("MANUAL-001");
        request.setData("test-data");
        request.setType(QRCodeType.PRODUCT);
        request.setEntityType("product");
        request.setEntityId(1L);
        
        QRCodeData savedQR = new QRCodeData();
        savedQR.setId(1L);
        savedQR.setCode("MANUAL-001");
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(1L);
        dto.setCode("MANUAL-001");
        
        when(qrCodeRepository.save(any(QRCodeData.class))).thenReturn(savedQR);
        when(dtoMapper.toDTO(savedQR)).thenReturn(dto);

        ResponseEntity<?> response = qrCodeController.generateQRCode(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(qrCodeRepository).save(any(QRCodeData.class));
    }

    @Test
    void generateQRCode_ShouldReturnError_WhenGenerationFails() {
        QRCodeController.GenerateQRRequest request = new QRCodeController.GenerateQRRequest();
        request.setData("test-data");
        request.setType(QRCodeType.INVENTORY_ITEM);
        request.setEntityType("inventory_item");
        request.setEntityId(1L);
        
        // ZMIANA: Dodano argumenty do mocka, aby pasowały do wywołania w kontrolerze
        when(qrCodeGenerationService.generateQRCodeSync(
            anyString(), any(), anyString(), any(), anyString(), any(), any()
        )).thenReturn(null);

        ResponseEntity<?> response = qrCodeController.generateQRCode(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }


    @Test
    void updateQRCode_ShouldUpdateAndReturn_WhenFound() {
        Long id = 1L;
        QRCodeController.UpdateQRRequest request = new QRCodeController.UpdateQRRequest();
        request.setData("updated-data");
        request.setActive(false);
        
        QRCodeData existingQR = new QRCodeData();
        existingQR.setId(id);
        existingQR.setData("old-data");
        existingQR.setActive(true);
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(id);
        
        when(qrCodeRepository.findById(id)).thenReturn(Optional.of(existingQR));
        when(qrCodeRepository.save(any(QRCodeData.class))).thenReturn(existingQR);
        when(dtoMapper.toDTO(any(QRCodeData.class))).thenReturn(dto);

        ResponseEntity<QRCodeDTO> response = qrCodeController.updateQRCode(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("updated-data", existingQR.getData());
        assertFalse(existingQR.getActive());
    }

    @Test
    void updateQRCode_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        QRCodeController.UpdateQRRequest request = new QRCodeController.UpdateQRRequest();
        
        when(qrCodeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> qrCodeController.updateQRCode(id, request));
    }

    // ==================== DELETE QR CODE (HARD DELETE) ====================

    @Test
    void deleteQRCode_ShouldDelete_WhenFound() {
        Long id = 1L;
        QRCodeData qrCode = new QRCodeData();
        qrCode.setId(id);
        
        when(qrCodeRepository.findById(id)).thenReturn(Optional.of(qrCode));
        doNothing().when(qrCodeRepository).delete(qrCode);

        ResponseEntity<Void> response = qrCodeController.deleteQRCode(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(qrCodeRepository).delete(qrCode);
    }

    @Test
    void deleteQRCode_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(qrCodeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> qrCodeController.deleteQRCode(id));
    }

    // ==================== TOGGLE ACTIVE ====================

    @Test
    void toggleQRCodeActive_ShouldToggleFromTrueToFalse() {
        Long id = 1L;
        QRCodeData qrCode = new QRCodeData();
        qrCode.setId(id);
        qrCode.setActive(true);
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(id);
        
        when(qrCodeRepository.findById(id)).thenReturn(Optional.of(qrCode));
        when(qrCodeRepository.save(any(QRCodeData.class))).thenReturn(qrCode);
        when(dtoMapper.toDTO(any(QRCodeData.class))).thenReturn(dto);

        ResponseEntity<QRCodeDTO> response = qrCodeController.toggleQRCodeActive(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(qrCode.getActive());
    }

    @Test
    void toggleQRCodeActive_ShouldToggleFromFalseToTrue() {
        Long id = 1L;
        QRCodeData qrCode = new QRCodeData();
        qrCode.setId(id);
        qrCode.setActive(false);
        
        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(id);
        
        when(qrCodeRepository.findById(id)).thenReturn(Optional.of(qrCode));
        when(qrCodeRepository.save(any(QRCodeData.class))).thenReturn(qrCode);
        when(dtoMapper.toDTO(any(QRCodeData.class))).thenReturn(dto);

        ResponseEntity<QRCodeDTO> response = qrCodeController.toggleQRCodeActive(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(qrCode.getActive());
    }

    // ==================== STATS ====================

    @Test
    void getQRStats_ShouldReturnStatistics() {
        QRCodeData qr1 = new QRCodeData();
        qr1.setActive(true);
        qr1.setScanCount(10L);
        
        QRCodeData qr2 = new QRCodeData();
        qr2.setActive(false);
        qr2.setScanCount(5L);
        
        when(qrCodeRepository.count()).thenReturn(2L);
        when(qrCodeRepository.findByActiveTrue()).thenReturn(List.of(qr1));
        when(qrCodeRepository.findAll()).thenReturn(Arrays.asList(qr1, qr2));

        ResponseEntity<QRCodeController.QRStatsResponse> response = qrCodeController.getQRStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        QRCodeController.QRStatsResponse stats = response.getBody();
        assertEquals(2L, stats.getTotalCodes());
        assertEquals(1L, stats.getActiveCodes());
        assertEquals(1L, stats.getInactiveCodes());
        assertEquals(15L, stats.getTotalScans());
    }
}