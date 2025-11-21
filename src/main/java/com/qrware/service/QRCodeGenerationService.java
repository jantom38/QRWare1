package com.qrware.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.qrware.domain.qr.QRCodeData;
import com.qrware.domain.qr.QRCodeType;
import com.qrware.repository.qr.QRCodeDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class QRCodeGenerationService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private QRCodeDataRepository qrCodeRepository;

    /**
     * Generuje QR kod asynchronicznie
     */
    @Async
    public CompletableFuture<QRCodeData> generateQRCodeAsync(
            String data,
            QRCodeType type,
            String entityType,
            Long entityId,
            String generatedBy,
            String generationReason) {
        
        try {
            // Generuj unikalny kod QR
            String qrCode = generateUniqueCode();
            
            // Stwórz rekord w bazie (w stanie "GENERATING")
            QRCodeData qrCodeData = createQRCodeRecord(qrCode, data, type, entityType, 
                    entityId, generatedBy, generationReason);
            qrCodeData = qrCodeRepository.save(qrCodeData);

            // Generuj obraz QR
            byte[] qrImage = generateQRImage(data, 300, 300, 
                    com.qrware.domain.qr.ErrorCorrectionLevel.M);
            
            // Zapisz obraz do pliku
            String fileName = fileStorageService.storeQRCodeImage(qrImage, 
                    qrCode + ".png");
            
            // Aktualizuj rekord z ścieżką do pliku
            qrCodeData.setImagePath(fileName);
            qrCodeData.setFormat("PNG");
            qrCodeData.setSize(300);
            qrCodeData.setActive(true);
            qrCodeData = qrCodeRepository.save(qrCodeData);

            return CompletableFuture.completedFuture(qrCodeData);
            
        } catch (Exception e) {
            // W przypadku błędu, oznacz QR jako nieaktywny
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Generuje QR kod synchronicznie (dla prostych przypadków)
     */
    public QRCodeData generateQRCodeSync(
            String data,
            QRCodeType type,
            String entityType,
            Long entityId,
            String generatedBy,
            String generationReason) {
        
        try {
            String qrCode = generateUniqueCode();
            
            QRCodeData qrCodeData = createQRCodeRecord(qrCode, data, type, entityType, 
                    entityId, generatedBy, generationReason);
            qrCodeData = qrCodeRepository.save(qrCodeData);

            byte[] qrImage = generateQRImage(data, 300, 300, 
                    com.qrware.domain.qr.ErrorCorrectionLevel.M);
            
            String fileName = fileStorageService.storeQRCodeImage(qrImage, 
                    qrCode + ".png");
            
            qrCodeData.setImagePath(fileName);
            qrCodeData.setFormat("PNG");
            qrCodeData.setSize(300);
            qrCodeData.setActive(true);
            
            return qrCodeRepository.save(qrCodeData);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Generuje obraz QR kodu
     */
    private byte[] generateQRImage(String data, int width, int height, 
            com.qrware.domain.qr.ErrorCorrectionLevel errorCorrectionLevel) 
            throws WriterException, IOException {
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, mapErrorCorrectionLevel(errorCorrectionLevel));
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Tworzy rekord QR w bazie danych
     */
    private QRCodeData createQRCodeRecord(String code, String data, QRCodeType type,
            String entityType, Long entityId, String generatedBy, String generationReason) {
        
        QRCodeData qrCodeData = new QRCodeData();
        qrCodeData.setCode(code);
        qrCodeData.setType(type);
        qrCodeData.setEntityType(entityType);
        qrCodeData.setEntityId(entityId);
        qrCodeData.setData(data);
        qrCodeData.setActive(false); // Będzie aktywny po wygenerowaniu obrazu
        qrCodeData.setScanCount(0L);
        qrCodeData.setErrorCorrectionLevel(com.qrware.domain.qr.ErrorCorrectionLevel.M);
        qrCodeData.setGeneratedBy(generatedBy);
        qrCodeData.setGenerationReason(generationReason);
        
        return qrCodeData;
    }

    /**
     * Generuje unikalny kod QR
     */
    private String generateUniqueCode() {
        String prefix = "QR";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "_" + timestamp + "_" + uuid;
    }

    /**
     * Mapuje poziom korekcji błędów
     */
    private ErrorCorrectionLevel mapErrorCorrectionLevel(
            com.qrware.domain.qr.ErrorCorrectionLevel level) {
        switch (level) {
            case L: return ErrorCorrectionLevel.L;
            case M: return ErrorCorrectionLevel.M;
            case Q: return ErrorCorrectionLevel.Q;
            case H: return ErrorCorrectionLevel.H;
            default: return ErrorCorrectionLevel.M;
        }
    }

    /**
     * Usuwa QR kod i powiązany plik
     */
    public boolean deleteQRCode(Long qrCodeId) {
        try {
            QRCodeData qrCode = qrCodeRepository.findById(qrCodeId).orElse(null);
            if (qrCode != null) {
                // Usuń plik obrazu
                if (qrCode.getImagePath() != null) {
                    fileStorageService.deleteQRCodeImage(qrCode.getImagePath());
                }
                
                // Usuń rekord z bazy
                qrCodeRepository.delete(qrCode);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Aktualizuje liczbę skanowań
     */
    public void recordScan(String code) {
        QRCodeData qrCode = qrCodeRepository.findByCode(code).orElse(null);
        if (qrCode != null && qrCode.getActive()) {
            qrCode.setScanCount(qrCode.getScanCount() + 1);
            qrCode.setLastScanned(LocalDateTime.now());
            qrCodeRepository.save(qrCode);
        }
    }
}