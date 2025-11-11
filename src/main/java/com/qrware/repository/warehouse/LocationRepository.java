package com.qrware.repository.warehouse;

import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.LocationType;
import com.qrware.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Location entity operations
 */
@Repository
public interface LocationRepository extends BaseRepository<Location> {

    /**
     * Find location by code
     */
    Optional<Location> findByCode(String code);

    /**
     * Find location by code ignoring case
     */
    Optional<Location> findByCodeIgnoreCase(String code);

    /**
     * Find location by QR code
     */
    Optional<Location> findByQrCode(String qrCode);

    /**
     * Find location by barcode
     */
    Optional<Location> findByBarcode(String barcode);

    /**
     * Check if code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if code exists excluding current location
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Location l WHERE l.code = :code AND l.id != :locationId")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("locationId") Long locationId);

    /**
     * Check if QR code exists
     */
    boolean existsByQrCode(String qrCode);

    /**
     * Check if QR code exists excluding current location
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Location l WHERE l.qrCode = :qrCode AND l.id != :locationId")
    boolean existsByQrCodeAndIdNot(@Param("qrCode") String qrCode, @Param("locationId") Long locationId);

    /**
     * Find all active locations
     */
    List<Location> findByActiveTrue();

    /**
     * Find all inactive locations
     */
    List<Location> findByActiveFalse();

    /**
     * Find locations by zone ID
     */
    @Query("SELECT l FROM Location l WHERE l.zone.id = :zoneId")
    List<Location> findByZoneId(@Param("zoneId") Long zoneId);

    Page<Location> findByActive(Boolean active, Pageable pageable);
    /**
     * Find active locations by zone ID
     */
    @Query("SELECT l FROM Location l WHERE l.zone.id = :zoneId AND l.active = true")
    List<Location> findActiveByZoneId(@Param("zoneId") Long zoneId);

    /**
     * Find locations by zone code
     */
    @Query("SELECT l FROM Location l WHERE l.zone.code = :zoneCode")
    List<Location> findByZoneCode(@Param("zoneCode") String zoneCode);

    /**
     * Find locations by zone name
     */
    @Query("SELECT l FROM Location l WHERE l.zone.name = :zoneName")
    List<Location> findByZoneName(@Param("zoneName") String zoneName);

    /**
     * Find locations by type
     */
    List<Location> findByType(LocationType type);

    /**
     * Find locations by type and active status
     */
    List<Location> findByTypeAndActive(LocationType type, Boolean active);

    /**
     * Find locations by multiple types
     */
    @Query("SELECT l FROM Location l WHERE l.type IN :types")
    List<Location> findByTypeIn(@Param("types") List<LocationType> types);

    /**
     * Find locations by aisle
     */
    List<Location> findByAisle(String aisle);

    /**
     * Find locations by rack
     */
    List<Location> findByRack(String rack);

    /**
     * Find locations by shelf
     */
    List<Location> findByShelf(String shelf);

    /**
     * Find locations by bin
     */
    List<Location> findByBin(String bin);

    /**
     * Find locations by coordinates
     */
    @Query("SELECT l FROM Location l WHERE l.aisle = :aisle AND l.rack = :rack AND l.shelf = :shelf AND l.bin = :bin")
    Optional<Location> findByCoordinates(@Param("aisle") String aisle, @Param("rack") String rack, 
                                        @Param("shelf") String shelf, @Param("bin") String bin);

    /**
     * Find locations by partial coordinates (aisle and rack)
     */
    @Query("SELECT l FROM Location l WHERE l.aisle = :aisle AND l.rack = :rack")
    List<Location> findByAisleAndRack(@Param("aisle") String aisle, @Param("rack") String rack);

    /**
     * Find locations by name containing keyword
     */
    @Query("SELECT l FROM Location l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Location> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Find locations by description containing keyword
     */
    @Query("SELECT l FROM Location l WHERE LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Location> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Search locations by code, name, or description
     */
    @Query("SELECT l FROM Location l WHERE " +
           "LOWER(l.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Location> searchLocations(@Param("searchTerm") String searchTerm);

    /**
     * Find pickable locations
     */
    List<Location> findByPickableTrue();

    /**
     * Find receivable locations
     */
    List<Location> findByReceivableTrue();

    /**
     * Find available locations (active, receivable, not full)
     */
    @Query("SELECT l FROM Location l WHERE l.active = true AND l.receivable = true AND SIZE(l.inventoryItems) < COALESCE(l.capacityItems, 999999)")
    List<Location> findAvailableLocations();

    /**
     * Find empty locations
     */
    @Query("SELECT l FROM Location l WHERE SIZE(l.inventoryItems) = 0")
    List<Location> findEmptyLocations();

    /**
     * Find occupied locations
     */
    @Query("SELECT l FROM Location l WHERE SIZE(l.inventoryItems) > 0")
    List<Location> findOccupiedLocations();

    /**
     * Find full locations
     */
    @Query("SELECT l FROM Location l WHERE l.capacityItems IS NOT NULL AND SIZE(l.inventoryItems) >= l.capacityItems")
    List<Location> findFullLocations();

    /**
     * Find temperature controlled locations
     */
    List<Location> findByTemperatureControlledTrue();

    /**
     * Find humidity controlled locations
     */
    List<Location> findByHumidityControlledTrue();

    /**
     * Find locations with environmental controls
     */
    @Query("SELECT l FROM Location l WHERE l.temperatureControlled = true OR l.humidityControlled = true")
    List<Location> findLocationsWithEnvironmentalControls();

    /**
     * Find locations for specific temperature
     */
    @Query("SELECT l FROM Location l WHERE " +
           "l.temperatureControlled = false OR " +
           "(l.temperatureMin IS NULL OR :temperature >= l.temperatureMin) AND " +
           "(l.temperatureMax IS NULL OR :temperature <= l.temperatureMax)")
    List<Location> findLocationsForTemperature(@Param("temperature") Integer temperature);

    /**
     * Find locations for specific humidity
     */
    @Query("SELECT l FROM Location l WHERE " +
           "l.humidityControlled = false OR " +
           "(l.humidityMin IS NULL OR :humidity >= l.humidityMin) AND " +
           "(l.humidityMax IS NULL OR :humidity <= l.humidityMax)")
    List<Location> findLocationsForHumidity(@Param("humidity") Integer humidity);

    /**
     * Find locations for hazardous materials
     */
    List<Location> findByHazardousMaterialsTrue();

    /**
     * Find locations for fragile items
     */
    List<Location> findByFragileItemsTrue();

    /**
     * Find locations by security level
     */
    List<Location> findBySecurityLevel(Integer securityLevel);

    /**
     * Find locations with minimum security level
     */
    @Query("SELECT l FROM Location l WHERE l.securityLevel >= :minLevel")
    List<Location> findBySecurityLevelGreaterThanEqual(@Param("minLevel") Integer minLevel);

    /**
     * Find high security locations
     */
    @Query("SELECT l FROM Location l WHERE l.securityLevel >= 3")
    List<Location> findHighSecurityLocations();

    /**
     * Find locations with capacity constraints
     */
    @Query("SELECT l FROM Location l WHERE l.capacityVolume IS NOT NULL OR l.capacityWeight IS NOT NULL OR l.capacityItems IS NOT NULL")
    List<Location> findLocationsWithCapacityConstraints();

    /**
     * Find locations with available volume capacity
     */
    @Query("SELECT l FROM Location l WHERE l.capacityVolume IS NOT NULL AND l.capacityVolume > " +
           "(SELECT COALESCE(SUM(CASE WHEN i.product.dimensionsLength IS NOT NULL AND i.product.dimensionsWidth IS NOT NULL AND i.product.dimensionsHeight IS NOT NULL " +
           "THEN i.product.dimensionsLength * i.product.dimensionsWidth * i.product.dimensionsHeight * i.quantity ELSE 0 END), 0) FROM InventoryItem i WHERE i.location = l)")
    List<Location> findLocationsWithAvailableVolumeCapacity();

    /**
     * Find locations with available weight capacity
     */
    @Query("SELECT l FROM Location l WHERE l.capacityWeight IS NOT NULL AND l.capacityWeight > " +
           "(SELECT COALESCE(SUM(i.product.weight * i.quantity), 0) FROM InventoryItem i WHERE i.location = l AND i.product.weight IS NOT NULL)")
    List<Location> findLocationsWithAvailableWeightCapacity();

    /**
     * Find locations by coordinates range
     */
    @Query("SELECT l FROM Location l WHERE " +
           "(:minX IS NULL OR l.xCoordinate >= :minX) AND " +
           "(:maxX IS NULL OR l.xCoordinate <= :maxX) AND " +
           "(:minY IS NULL OR l.yCoordinate >= :minY) AND " +
           "(:maxY IS NULL OR l.yCoordinate <= :maxY) AND " +
           "(:minZ IS NULL OR l.zCoordinate >= :minZ) AND " +
           "(:maxZ IS NULL OR l.zCoordinate <= :maxZ)")
    List<Location> findByCoordinateRange(@Param("minX") BigDecimal minX, @Param("maxX") BigDecimal maxX,
                                        @Param("minY") BigDecimal minY, @Param("maxY") BigDecimal maxY,
                                        @Param("minZ") BigDecimal minZ, @Param("maxZ") BigDecimal maxZ);

    /**
     * Find nearest locations to given coordinates
     */
    @Query("SELECT l FROM Location l WHERE l.xCoordinate IS NOT NULL AND l.yCoordinate IS NOT NULL " +
           "ORDER BY SQRT(POWER(l.xCoordinate - :x, 2) + POWER(l.yCoordinate - :y, 2)) ASC")
    List<Location> findNearestLocations(@Param("x") BigDecimal x, @Param("y") BigDecimal y);

    /**
     * Count locations by zone
     */
    @Query("SELECT COUNT(l) FROM Location l WHERE l.zone.id = :zoneId")
    long countByZoneId(@Param("zoneId") Long zoneId);

    /**
     * Count active locations by zone
     */
    @Query("SELECT COUNT(l) FROM Location l WHERE l.zone.id = :zoneId AND l.active = true")
    long countActiveByZoneId(@Param("zoneId") Long zoneId);

    /**
     * Count locations by type
     */
    long countByType(LocationType type);

    /**
     * Count active locations
     */
    long countByActiveTrue();

    /**
     * Count inactive locations
     */
    long countByActiveFalse();

    /**
     * Count pickable locations
     */
    long countByPickableTrue();

    /**
     * Count receivable locations
     */
    long countByReceivableTrue();

    /**
     * Count empty locations
     */
    @Query("SELECT COUNT(l) FROM Location l WHERE SIZE(l.inventoryItems) = 0")
    long countEmptyLocations();

    /**
     * Count occupied locations
     */
    @Query("SELECT COUNT(l) FROM Location l WHERE SIZE(l.inventoryItems) > 0")
    long countOccupiedLocations();

    /**
     * Count full locations
     */
    @Query("SELECT COUNT(l) FROM Location l WHERE l.capacityItems IS NOT NULL AND SIZE(l.inventoryItems) >= l.capacityItems")
    long countFullLocations();

    /**
     * Get location statistics
     */
    @Query("SELECT " +
           "COUNT(l) as totalLocations, " +
           "SUM(CASE WHEN l.active = true THEN 1 ELSE 0 END) as activeLocations, " +
           "SUM(CASE WHEN l.pickable = true THEN 1 ELSE 0 END) as pickableLocations, " +
           "SUM(CASE WHEN l.receivable = true THEN 1 ELSE 0 END) as receivableLocations, " +
           "SUM(CASE WHEN SIZE(l.inventoryItems) = 0 THEN 1 ELSE 0 END) as emptyLocations, " +
           "SUM(CASE WHEN SIZE(l.inventoryItems) > 0 THEN 1 ELSE 0 END) as occupiedLocations " +
           "FROM Location l")
    Object[] getLocationStatistics();

    /**
     * Get locations with inventory count
     */
    @Query("SELECT l, SIZE(l.inventoryItems) as inventoryCount FROM Location l ORDER BY SIZE(l.inventoryItems) DESC")
    List<Object[]> getLocationsWithInventoryCount();

    /**
     * Get location statistics by zone
     */
    @Query("SELECT l.zone.name, COUNT(l) as locationCount, " +
           "SUM(CASE WHEN l.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN SIZE(l.inventoryItems) = 0 THEN 1 ELSE 0 END) as emptyCount, " +
           "AVG(SIZE(l.inventoryItems)) as avgInventoryCount " +
           "FROM Location l GROUP BY l.zone.name ORDER BY l.zone.name")
    List<Object[]> getLocationStatsByZone();

    /**
     * Get location statistics by type
     */
    @Query("SELECT l.type, COUNT(l) as locationCount, " +
           "SUM(CASE WHEN l.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN SIZE(l.inventoryItems) = 0 THEN 1 ELSE 0 END) as emptyCount, " +
           "AVG(SIZE(l.inventoryItems)) as avgInventoryCount " +
           "FROM Location l GROUP BY l.type ORDER BY l.type")
    List<Object[]> getLocationStatsByType();

    /**
     * Find locations created by user
     */
    @Query("SELECT l FROM Location l WHERE l.createdBy = :username")
    List<Location> findCreatedBy(@Param("username") String username);

    /**
     * Find most recently created locations
     */
    @Query("SELECT l FROM Location l ORDER BY l.createdAt DESC")
    List<Location> findMostRecentlyCreated();

    /**
     * Find all unique aisles
     */
    @Query("SELECT DISTINCT l.aisle FROM Location l WHERE l.aisle IS NOT NULL ORDER BY l.aisle")
    List<String> findAllAisles();

    /**
     * Find all unique racks
     */
    @Query("SELECT DISTINCT l.rack FROM Location l WHERE l.rack IS NOT NULL ORDER BY l.rack")
    List<String> findAllRacks();

    /**
     * Find all unique shelves
     */
    @Query("SELECT DISTINCT l.shelf FROM Location l WHERE l.shelf IS NOT NULL ORDER BY l.shelf")
    List<String> findAllShelves();

    /**
     * Find all unique bins
     */
    @Query("SELECT DISTINCT l.bin FROM Location l WHERE l.bin IS NOT NULL ORDER BY l.bin")
    List<String> findAllBins();

    /**
     * Find suitable locations for product requirements
     */
    @Query("SELECT l FROM Location l WHERE " +
           "l.active = true AND l.receivable = true AND " +
           "(:requiresTemperatureControl = false OR l.temperatureControlled = true) AND " +
           "(:requiresHumidityControl = false OR l.humidityControlled = true) AND " +
           "(:requiresHazmatHandling = false OR l.hazardousMaterials = true) AND " +
           "(:requiresFragileHandling = false OR l.fragileItems = true) AND " +
           "l.securityLevel >= :minSecurityLevel AND " +
           "SIZE(l.inventoryItems) < COALESCE(l.capacityItems, 999999)")
    List<Location> findSuitableLocations(@Param("requiresTemperatureControl") Boolean requiresTemperatureControl,
                                        @Param("requiresHumidityControl") Boolean requiresHumidityControl,
                                        @Param("requiresHazmatHandling") Boolean requiresHazmatHandling,
                                        @Param("requiresFragileHandling") Boolean requiresFragileHandling,
                                        @Param("minSecurityLevel") Integer minSecurityLevel);

    /**
     * Find locations with specific capacity for volume and weight
     */
    @Query("SELECT l FROM Location l WHERE " +
           "l.active = true AND l.receivable = true AND " +
           "(:requiredVolume IS NULL OR l.capacityVolume IS NULL OR l.capacityVolume >= :requiredVolume) AND " +
           "(:requiredWeight IS NULL OR l.capacityWeight IS NULL OR l.capacityWeight >= :requiredWeight) AND " +
           "SIZE(l.inventoryItems) < COALESCE(l.capacityItems, 999999)")
    List<Location> findLocationsWithCapacity(@Param("requiredVolume") BigDecimal requiredVolume,
                                            @Param("requiredWeight") BigDecimal requiredWeight);

    /**
     * Find locations for picking by zone priority
     */
    @Query("SELECT l FROM Location l WHERE " +
           "l.active = true AND l.pickable = true AND " +
           "SIZE(l.inventoryItems) > 0 " +
           "ORDER BY l.zone.pickingPriority ASC, l.code ASC")
    List<Location> findPickingLocationsByPriority();
}