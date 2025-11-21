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
            // 1. Generuj unikalny kod systemowy (np. QR_A1B2C3D4)
            String qrCode = generateUniqueCode();

            // 2. Dobierz poziom korekcji błędów w zależności od zastosowania
            com.qrware.domain.qr.ErrorCorrectionLevel appEcLevel =
                    com.qrware.domain.qr.ErrorCorrectionLevel.recommendForUsage(type);

            // 3. Stwórz rekord w bazie
            QRCodeData qrCodeData = createQRCodeRecord(qrCode, data, type, entityType,
                    entityId, generatedBy, generationReason, appEcLevel);
            qrCodeData = qrCodeRepository.save(qrCodeData);

            // 4. Generuj obraz QR
            // WAŻNE: Kodujemy 'qrCode' (ID), a nie 'data' (opis)!
            byte[] qrImage = generateQRImage(qrCode, 300, 300, appEcLevel);

            // 5. Zapisz obraz do pliku
            String fileName = fileStorageService.storeQRCodeImage(qrImage,
                    qrCode + ".png");

            // 6. Aktualizuj rekord
            qrCodeData.setImagePath(fileName);
            qrCodeData.setFormat("PNG");
            qrCodeData.setSize(300);
            qrCodeData.setActive(true);
            qrCodeData = qrCodeRepository.save(qrCodeData);

            return CompletableFuture.completedFuture(qrCodeData);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Generuje QR kod synchronicznie
     */
    public QRCodeData generateQRCodeSync(
            String data,
            QRCodeType type,
            String entityType,
            Long entityId,
            String generatedBy,
            String generationReason) {

        try {
            // 1. Generuj unikalny kod systemowy
            String qrCode = generateUniqueCode();

            // 2. Dobierz poziom korekcji
            com.qrware.domain.qr.ErrorCorrectionLevel appEcLevel =
                    com.qrware.domain.qr.ErrorCorrectionLevel.recommendForUsage(type);

            // 3. Stwórz rekord
            QRCodeData qrCodeData = createQRCodeRecord(qrCode, data, type, entityType,
                    entityId, generatedBy, generationReason, appEcLevel);
            qrCodeData = qrCodeRepository.save(qrCodeData);

            // 4. Generuj obraz QR
            // WAŻNE: Kodujemy 'qrCode' (ID), a nie 'data' (opis)!
            byte[] qrImage = generateQRImage(qrCode, 300, 300, appEcLevel);

            // 5. Zapisz obraz
            String fileName = fileStorageService.storeQRCodeImage(qrImage,
                    qrCode + ".png");

            // 6. Aktualizuj i zwróć
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
     * Generuje obraz QR kodu (bitmapę)
     */
    private byte[] generateQRImage(String contentToEncode, int width, int height,
                                   com.qrware.domain.qr.ErrorCorrectionLevel errorCorrectionLevel)
            throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, mapErrorCorrectionLevel(errorCorrectionLevel));
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(contentToEncode, BarcodeFormat.QR_CODE, width, height, hints);

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
                                          String entityType, Long entityId, String generatedBy, String generationReason,
                                          com.qrware.domain.qr.ErrorCorrectionLevel ecLevel) {

        QRCodeData qrCodeData = new QRCodeData();
        qrCodeData.setCode(code);
        qrCodeData.setType(type);
        qrCodeData.setEntityType(entityType);
        qrCodeData.setEntityId(entityId);
        qrCodeData.setData(data);
        qrCodeData.setActive(false);
        qrCodeData.setScanCount(0L);
        qrCodeData.setErrorCorrectionLevel(ecLevel);
        qrCodeData.setGeneratedBy(generatedBy);
        qrCodeData.setGenerationReason(generationReason);

        // Ustawienie wygasania jeśli wymagane
        if (type.requiresExpiration()) {
            int hours = type.getDefaultExpirationHours();
            if (hours > 0) {
                qrCodeData.setExpirationHours(hours);
            }
        }

        return qrCodeData;
    }

    /**
     * Generuje unikalny, krótki kod systemowy
     * Format: QR_ + 8 znaków UUID (np. QR_1A2B3C4D)
     */
    private String generateUniqueCode() {
        return "QR_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Mapuje poziom korekcji błędów z domeny aplikacji na ZXing
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
                if (qrCode.getImagePath() != null) {
                    fileStorageService.deleteQRCodeImage(qrCode.getImagePath());
                }
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
        if (qrCode != null && Boolean.TRUE.equals(qrCode.getActive())) {
            qrCode.incrementScanCount();
            qrCode.setLastScanned(LocalDateTime.now());
            qrCodeRepository.save(qrCode);
        }
    }
}