package com.qrware.service;

import com.qrware.domain.qr.QRCodeData;
import com.qrware.domain.qr.QRCodeType;
import com.qrware.repository.qr.QRCodeDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRCodeGenerationServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private QRCodeDataRepository qrCodeRepository;

    @InjectMocks
    private QRCodeGenerationService qrCodeGenerationService;

    @Test
    void generateQRCodeSync_ShouldGenerateAndSave() {
        String data = "test-data";
        QRCodeType type = QRCodeType.INVENTORY_ITEM;
        
        when(qrCodeRepository.save(any(QRCodeData.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorageService.storeQRCodeImage(any(byte[].class), anyString())).thenReturn("qr_code.png");

        QRCodeData result = qrCodeGenerationService.generateQRCodeSync(
            data, type, "InventoryItem", 1L, "system", "Test"
        );

        assertNotNull(result);
        assertNotNull(result.getCode());
        assertEquals(data, result.getData());
        assertEquals("qr_code.png", result.getImagePath());
        assertTrue(result.getActive());
        verify(qrCodeRepository, times(2)).save(any(QRCodeData.class));
        verify(fileStorageService).storeQRCodeImage(any(byte[].class), anyString());
    }

    @Test
    void deleteQRCode_ShouldDeleteFileAndRecord_WhenFound() {
        Long id = 1L;
        QRCodeData qrCode = new QRCodeData();
        qrCode.setId(id);
        qrCode.setImagePath("qr_code.png");
        
        when(qrCodeRepository.findById(id)).thenReturn(Optional.of(qrCode));
        when(fileStorageService.deleteQRCodeImage("qr_code.png")).thenReturn(true);

        boolean result = qrCodeGenerationService.deleteQRCode(id);

        assertTrue(result);
        verify(fileStorageService).deleteQRCodeImage("qr_code.png");
        verify(qrCodeRepository).delete(qrCode);
    }

    @Test
    void deleteQRCode_ShouldReturnFalse_WhenNotFound() {
        Long id = 1L;
        when(qrCodeRepository.findById(id)).thenReturn(Optional.empty());

        boolean result = qrCodeGenerationService.deleteQRCode(id);

        assertFalse(result);
        verify(fileStorageService, never()).deleteQRCodeImage(anyString());
        verify(qrCodeRepository, never()).delete(any(QRCodeData.class));
    }
}
