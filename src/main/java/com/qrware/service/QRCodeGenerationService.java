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

    // DEFINICJA SEPARATORA DANYCH
    private static final String QR_DATA_SEPARATOR = "###";

    /**
     * Generuje QR kod asynchronicznie (Hybrydowo: ID + Dane)
     */
    @Async
    public CompletableFuture<QRCodeData> generateQRCodeAsync(
            String data, // <--- To są Twoje dane (np. "Waga:20kg;Seria:ABC")
            QRCodeType type,
            String entityType,
            Long entityId,
            String generatedBy,
            String generationReason) {

        try {
            // 1. Generuj unikalny kod systemowy (to on będzie kluczem w bazie)
            String systemId = generateUniqueCode();

            // 2. Dobierz poziom korekcji (High - bo upychamy więcej danych w obrazku)
            com.qrware.domain.qr.ErrorCorrectionLevel appEcLevel =
                    com.qrware.domain.qr.ErrorCorrectionLevel.H;

            // 3. Stwórz rekord w bazie
            // W bazie zapisujemy: code=systemId, data=TwojeDane
            QRCodeData qrCodeData = createQRCodeRecord(systemId, data, type, entityType,
                    entityId, generatedBy, generationReason, appEcLevel);
            qrCodeData = qrCodeRepository.save(qrCodeData);

            // 4. PRZYGOTUJ TREŚĆ DO OBRAZKA QR
            // Łączymy ID z Twoimi danymi, aby kod był czytelny też offline
            // Jeśli 'data' jest pusta, kodujemy samo ID.
            String contentToEncode;
            if (data != null && !data.isEmpty()) {
                contentToEncode = systemId + QR_DATA_SEPARATOR + data;
            } else {
                contentToEncode = systemId;
            }

            // 5. Generuj obraz QR z połączonych danych
            byte[] qrImage = generateQRImage(contentToEncode, 300, 300, appEcLevel);

            // 6. Zapisz obraz do pliku (nazwa pliku nadal bazuje na ID)
            String fileName = fileStorageService.storeQRCodeImage(qrImage,
                    systemId + ".png");

            // 7. Aktualizuj rekord ścieżką do pliku
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
     * Generuje QR kod synchronicznie (Hybrydowo)
     */
    public QRCodeData generateQRCodeSync(
            String data,
            QRCodeType type,
            String entityType,
            Long entityId,
            String generatedBy,
            String generationReason) {

        try {
            String systemId = generateUniqueCode();
            com.qrware.domain.qr.ErrorCorrectionLevel appEcLevel =
                    com.qrware.domain.qr.ErrorCorrectionLevel.H;

            QRCodeData qrCodeData = createQRCodeRecord(systemId, data, type, entityType,
                    entityId, generatedBy, generationReason, appEcLevel);
            qrCodeData = qrCodeRepository.save(qrCodeData);

            String contentToEncode;
            if (data != null && !data.isEmpty()) {
                contentToEncode = systemId + QR_DATA_SEPARATOR + data;
            } else {
                contentToEncode = systemId;
            }

            byte[] qrImage = generateQRImage(contentToEncode, 300, 300, appEcLevel);
            String fileName = fileStorageService.storeQRCodeImage(qrImage,
                    systemId + ".png");

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

        if (type.requiresExpiration()) {
            int hours = type.getDefaultExpirationHours();
            if (hours > 0) {
                qrCodeData.setExpirationHours(hours);
            }
        }
        return qrCodeData;
    }

    private String generateUniqueCode() {
        return "QR_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

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

    public void recordScan(String code) {
        QRCodeData qrCode = qrCodeRepository.findByCode(code).orElse(null);
        if (qrCode != null && Boolean.TRUE.equals(qrCode.getActive())) {
            qrCode.incrementScanCount();
            qrCode.setLastScanned(LocalDateTime.now());
            qrCodeRepository.save(qrCode);
        }
    }
}