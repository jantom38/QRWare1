package com.qrware.repository.qr;

import com.qrware.domain.qr.QRCodeData;
import com.qrware.domain.qr.QRCodeType;
import com.qrware.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeDataRepository extends BaseRepository<QRCodeData> {

    Optional<QRCodeData> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END FROM QRCodeData q WHERE q.code = :code AND q.id != :qrCodeId")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("qrCodeId") Long qrCodeId);

    List<QRCodeData> findByType(QRCodeType type);

    List<QRCodeData> findByTypeAndActive(QRCodeType type, Boolean active);

    Optional<QRCodeData> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<QRCodeData> findByEntityType(String entityType);

    List<QRCodeData> findByEntityId(Long entityId);

    List<QRCodeData> findByActiveTrue();
    
    Page<QRCodeData> findByActiveTrue(Pageable pageable);

    List<QRCodeData> findByActiveFalse();

    @Query("SELECT q FROM QRCodeData q WHERE q.expiresAt IS NOT NULL AND q.expiresAt < CURRENT_TIMESTAMP")
    List<QRCodeData> findExpiredQRCodes();

    @Query("SELECT q FROM QRCodeData q WHERE q.expiresAt IS NOT NULL AND q.expiresAt BETWEEN CURRENT_TIMESTAMP AND :expiryDate")
    List<QRCodeData> findQRCodesExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate);

    @Query("SELECT q FROM QRCodeData q WHERE q.expiresAt IS NOT NULL AND q.expiresAt BETWEEN CURRENT_TIMESTAMP AND :endDate")
    List<QRCodeData> findQRCodesExpiringWithinHours(@Param("endDate") LocalDateTime endDate);

    @Query("SELECT q FROM QRCodeData q WHERE q.active = true AND (q.expiresAt IS NULL OR q.expiresAt > CURRENT_TIMESTAMP)")
    List<QRCodeData> findValidQRCodes();

    @Query("SELECT q FROM QRCodeData q WHERE q.lastScanned IS NULL")
    List<QRCodeData> findNeverScannedQRCodes();

    @Query("SELECT q FROM QRCodeData q WHERE q.lastScanned IS NULL OR q.lastScanned < :date")
    List<QRCodeData> findQRCodesNotScannedSince(@Param("date") LocalDateTime date);

    @Query("SELECT q FROM QRCodeData q WHERE q.lastScanned >= :date")
    List<QRCodeData> findQRCodesScannedSince(@Param("date") LocalDateTime date);

    @Query("SELECT q FROM QRCodeData q WHERE q.scanCount >= :threshold ORDER BY q.scanCount DESC")
    List<QRCodeData> findFrequentlyScannedQRCodes(@Param("threshold") Long threshold);

    @Query("SELECT q FROM QRCodeData q WHERE q.scanCount BETWEEN :minCount AND :maxCount ORDER BY q.scanCount DESC")
    List<QRCodeData> findByScanCountBetween(@Param("minCount") Long minCount, @Param("maxCount") Long maxCount);

    List<QRCodeData> findByFormat(String format);

    List<QRCodeData> findBySize(Integer size);

    List<QRCodeData> findByGeneratedBy(String generatedBy);

    @Query("SELECT q FROM QRCodeData q WHERE LOWER(q.generationReason) LIKE LOWER(CONCAT('%', :reason, '%'))")
    List<QRCodeData> findByGenerationReasonContaining(@Param("reason") String reason);

    @Query("SELECT q FROM QRCodeData q WHERE LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<QRCodeData> findByDescriptionContaining(@Param("keyword") String keyword);

    @Query("SELECT q FROM QRCodeData q WHERE " +
           "LOWER(q.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(q.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(q.entityType) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<QRCodeData> searchQRCodes(@Param("searchTerm") String searchTerm);

    @Query("SELECT q FROM QRCodeData q WHERE q.customData IS NOT NULL AND q.customData != ''")
    List<QRCodeData> findQRCodesWithCustomData();

    @Query("SELECT q FROM QRCodeData q WHERE q.metadata IS NOT NULL AND q.metadata != ''")
    List<QRCodeData> findQRCodesWithMetadata();

    @Query("SELECT q FROM QRCodeData q WHERE q.imagePath IS NOT NULL AND q.imagePath != ''")
    List<QRCodeData> findQRCodesWithImagePath();

    @Query("SELECT q FROM QRCodeData q WHERE q.imagePath IS NULL OR q.imagePath = ''")
    List<QRCodeData> findQRCodesWithoutImagePath();

    @Query("SELECT q FROM QRCodeData q WHERE q.createdAt BETWEEN :startDate AND :endDate ORDER BY q.createdAt DESC")
    List<QRCodeData> findByCreationDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT q FROM QRCodeData q WHERE q.createdAt >= :date ORDER BY q.createdAt DESC")
    List<QRCodeData> findCreatedAfter(@Param("date") LocalDateTime date);

    long countByType(QRCodeType type);

    long countByActiveTrue();

    long countByActiveFalse();

    @Query("SELECT COUNT(q) FROM QRCodeData q WHERE q.expiresAt IS NOT NULL AND q.expiresAt < CURRENT_TIMESTAMP")
    long countExpiredQRCodes();

    @Query("SELECT COUNT(q) FROM QRCodeData q WHERE q.expiresAt IS NOT NULL AND q.expiresAt BETWEEN CURRENT_TIMESTAMP AND :expiryDate")
    long countQRCodesExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate);

    @Query("SELECT COUNT(q) FROM QRCodeData q WHERE q.active = true AND (q.expiresAt IS NULL OR q.expiresAt > CURRENT_TIMESTAMP)")
    long countValidQRCodes();

    @Query("SELECT COUNT(q) FROM QRCodeData q WHERE q.lastScanned IS NULL")
    long countNeverScannedQRCodes();

    long countByEntityType(String entityType);

    @Query("SELECT SUM(q.scanCount) FROM QRCodeData q")
    Long getTotalScanCount();

    @Query("SELECT " +
           "COUNT(q) as totalQRCodes, " +
           "SUM(CASE WHEN q.active = true THEN 1 ELSE 0 END) as activeQRCodes, " +
           "SUM(CASE WHEN q.expiresAt IS NOT NULL AND q.expiresAt < CURRENT_TIMESTAMP THEN 1 ELSE 0 END) as expiredQRCodes, " +
           "SUM(CASE WHEN q.lastScanned IS NULL THEN 1 ELSE 0 END) as neverScannedQRCodes, " +
           "SUM(q.scanCount) as totalScans, " +
           "COUNT(DISTINCT q.entityType) as uniqueEntityTypes " +
           "FROM QRCodeData q")
    Object[] getQRCodeStatistics();

    @Query("SELECT q.type, COUNT(q), " +
           "SUM(CASE WHEN q.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(q.scanCount) as totalScans, " +
           "AVG(q.scanCount) as avgScans " +
           "FROM QRCodeData q GROUP BY q.type ORDER BY q.type")
    List<Object[]> getQRCodeStatsByType();

    @Query("SELECT q.entityType, COUNT(q), " +
           "SUM(CASE WHEN q.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(q.scanCount) as totalScans, " +
           "AVG(q.scanCount) as avgScans " +
           "FROM QRCodeData q GROUP BY q.entityType ORDER BY q.entityType")
    List<Object[]> getQRCodeStatsByEntityType();

    @Query("SELECT DATE(q.lastScanned), COUNT(q) " +
           "FROM QRCodeData q " +
           "WHERE q.lastScanned BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(q.lastScanned) ORDER BY DATE(q.lastScanned)")
    List<Object[]> getQRCodeUsageByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT q FROM QRCodeData q ORDER BY q.scanCount DESC")
    List<QRCodeData> getMostScannedQRCodes();

    @Query("SELECT q FROM QRCodeData q WHERE q.lastScanned IS NOT NULL ORDER BY q.lastScanned DESC")
    List<QRCodeData> getRecentlyScannedQRCodes();

    @Query("SELECT q FROM QRCodeData q WHERE q.createdBy = :username")
    List<QRCodeData> findCreatedBy(@Param("username") String username);

    @Query("SELECT q FROM QRCodeData q ORDER BY q.createdAt DESC")
    List<QRCodeData> findMostRecentlyCreated();

    @Query("SELECT q FROM QRCodeData q WHERE " +
           "q.active = false AND " +
           "q.createdAt < :cutoffDate AND " +
           "(q.lastScanned IS NULL OR q.lastScanned < :cutoffDate)")
    List<QRCodeData> findQRCodesForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT q FROM QRCodeData q WHERE EXISTS " +
           "(SELECT q2 FROM QRCodeData q2 WHERE q2.id != q.id AND q2.entityType = q.entityType AND q2.entityId = q.entityId)")
    List<QRCodeData> findDuplicateQRCodes();

    @Query("SELECT q FROM QRCodeData q WHERE " +
           "(q.entityType = 'PRODUCT' AND NOT EXISTS (SELECT p FROM Product p WHERE p.id = q.entityId)) OR " +
           "(q.entityType = 'LOCATION' AND NOT EXISTS (SELECT l FROM Location l WHERE l.id = q.entityId)) OR " +
           "(q.entityType = 'INVENTORY_ITEM' AND NOT EXISTS (SELECT i FROM InventoryItem i WHERE i.id = q.entityId))")
    List<QRCodeData> findOrphanedQRCodes();

    @Query("SELECT DISTINCT q.entityType FROM QRCodeData q ORDER BY q.entityType")
    List<String> findAllEntityTypes();

    @Query("SELECT DISTINCT q.format FROM QRCodeData q WHERE q.format IS NOT NULL ORDER BY q.format")
    List<String> findAllFormats();

    @Query("SELECT DISTINCT q.size FROM QRCodeData q WHERE q.size IS NOT NULL ORDER BY q.size")
    List<Integer> findAllSizes();

    @Modifying
    @Query("UPDATE QRCodeData q SET q.scanCount = q.scanCount + 1, q.lastScanned = CURRENT_TIMESTAMP WHERE q.id = :qrCodeId")
    void updateScanStatistics(@Param("qrCodeId") Long qrCodeId);

    @Modifying
    @Query("UPDATE QRCodeData q SET q.active = false WHERE q.expiresAt IS NOT NULL AND q.expiresAt < CURRENT_TIMESTAMP")
    int deactivateExpiredQRCodes();

    @Modifying
    @Query("UPDATE QRCodeData q SET q.scanCount = 0, q.lastScanned = NULL WHERE q.id = :qrCodeId")
    void resetScanCount(@Param("qrCodeId") Long qrCodeId);

    @Query("SELECT q FROM QRCodeData q WHERE CONCAT(q.entityType, ':', q.entityId) = :entityReference")
    List<QRCodeData> findByEntityReference(@Param("entityReference") String entityReference);

    @Query(
            value = "SELECT q.scan_count * 1.0 / (DATEDIFF('SECOND', q.created_at, CURRENT_TIMESTAMP) / 3600.0) " +
                    "FROM qr_code_data q WHERE q.id = :qrCodeId AND q.created_at IS NOT NULL",
            nativeQuery = true
    )
    Double getScanVelocity(@Param("qrCodeId") Long qrCodeId);
}