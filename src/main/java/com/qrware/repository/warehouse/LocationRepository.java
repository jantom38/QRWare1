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

@Repository
public interface LocationRepository extends BaseRepository<Location> {

    Optional<Location> findByCode(String code);

    Optional<Location> findByCodeIgnoreCase(String code);

    Optional<Location> findByQrCode(String qrCode);

    Optional<Location> findByBarcode(String barcode);

    boolean existsByCode(String code);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Location l WHERE l.code = :code AND l.id != :locationId")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("locationId") Long locationId);

    boolean existsByQrCode(String qrCode);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Location l WHERE l.qrCode = :qrCode AND l.id != :locationId")
    boolean existsByQrCodeAndIdNot(@Param("qrCode") String qrCode, @Param("locationId") Long locationId);

    List<Location> findByActiveTrue();

    List<Location> findByActiveFalse();

    @Query("SELECT l FROM Location l WHERE l.zone.id = :zoneId")
    List<Location> findByZoneId(@Param("zoneId") Long zoneId);

    Page<Location> findByActive(Boolean active, Pageable pageable);

    @Query("SELECT l FROM Location l WHERE l.zone.id = :zoneId AND l.active = true")
    List<Location> findActiveByZoneId(@Param("zoneId") Long zoneId);

    @Query("SELECT l FROM Location l WHERE l.zone.code = :zoneCode")
    List<Location> findByZoneCode(@Param("zoneCode") String zoneCode);

    @Query("SELECT l FROM Location l WHERE l.zone.name = :zoneName")
    List<Location> findByZoneName(@Param("zoneName") String zoneName);

    List<Location> findByType(LocationType type);

    List<Location> findByTypeAndActive(LocationType type, Boolean active);

    @Query("SELECT l FROM Location l WHERE l.type IN :types")
    List<Location> findByTypeIn(@Param("types") List<LocationType> types);

    List<Location> findByAisle(String aisle);

    List<Location> findByRack(String rack);

    List<Location> findByShelf(String shelf);

    List<Location> findByBin(String bin);

    @Query("SELECT l FROM Location l WHERE l.aisle = :aisle AND l.rack = :rack AND l.shelf = :shelf AND l.bin = :bin")
    Optional<Location> findByCoordinates(@Param("aisle") String aisle, @Param("rack") String rack, 
                                        @Param("shelf") String shelf, @Param("bin") String bin);

    @Query("SELECT l FROM Location l WHERE l.aisle = :aisle AND l.rack = :rack")
    List<Location> findByAisleAndRack(@Param("aisle") String aisle, @Param("rack") String rack);

    @Query("SELECT l FROM Location l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Location> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT l FROM Location l WHERE LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Location> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT l FROM Location l WHERE " +
           "LOWER(l.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Location> searchLocations(@Param("searchTerm") String searchTerm);

    List<Location> findByPickableTrue();

    List<Location> findByReceivableTrue();

    @Query("SELECT l FROM Location l WHERE l.active = true AND l.receivable = true AND SIZE(l.inventoryItems) < COALESCE(l.capacityItems, 999999)")
    List<Location> findAvailableLocations();

    @Query("SELECT l FROM Location l WHERE SIZE(l.inventoryItems) = 0")
    List<Location> findEmptyLocations();

    @Query("SELECT l FROM Location l WHERE SIZE(l.inventoryItems) > 0")
    List<Location> findOccupiedLocations();

    @Query("SELECT l FROM Location l WHERE l.capacityItems IS NOT NULL AND SIZE(l.inventoryItems) >= l.capacityItems")
    List<Location> findFullLocations();

    List<Location> findByTemperatureControlledTrue();

    List<Location> findByHumidityControlledTrue();

    @Query("SELECT l FROM Location l WHERE l.temperatureControlled = true OR l.humidityControlled = true")
    List<Location> findLocationsWithEnvironmentalControls();

    @Query("SELECT l FROM Location l WHERE " +
           "l.temperatureControlled = false OR " +
           "(l.temperatureMin IS NULL OR :temperature >= l.temperatureMin) AND " +
           "(l.temperatureMax IS NULL OR :temperature <= l.temperatureMax)")
    List<Location> findLocationsForTemperature(@Param("temperature") Integer temperature);

    @Query("SELECT l FROM Location l WHERE " +
           "l.humidityControlled = false OR " +
           "(l.humidityMin IS NULL OR :humidity >= l.humidityMin) AND " +
           "(l.humidityMax IS NULL OR :humidity <= l.humidityMax)")
    List<Location> findLocationsForHumidity(@Param("humidity") Integer humidity);

    List<Location> findByHazardousMaterialsTrue();

    List<Location> findByFragileItemsTrue();

    List<Location> findBySecurityLevel(Integer securityLevel);

    @Query("SELECT l FROM Location l WHERE l.securityLevel >= :minLevel")
    List<Location> findBySecurityLevelGreaterThanEqual(@Param("minLevel") Integer minLevel);

    @Query("SELECT l FROM Location l WHERE l.securityLevel >= 3")
    List<Location> findHighSecurityLocations();

    @Query("SELECT l FROM Location l WHERE l.capacityVolume IS NOT NULL OR l.capacityWeight IS NOT NULL OR l.capacityItems IS NOT NULL")
    List<Location> findLocationsWithCapacityConstraints();

    @Query("SELECT l FROM Location l WHERE l.capacityVolume IS NOT NULL AND l.capacityVolume > " +
           "(SELECT COALESCE(SUM(CASE WHEN i.product.dimensionsLength IS NOT NULL AND i.product.dimensionsWidth IS NOT NULL AND i.product.dimensionsHeight IS NOT NULL " +
           "THEN i.product.dimensionsLength * i.product.dimensionsWidth * i.product.dimensionsHeight * i.quantity ELSE 0 END), 0) FROM InventoryItem i WHERE i.location = l)")
    List<Location> findLocationsWithAvailableVolumeCapacity();

    @Query("SELECT l FROM Location l WHERE l.capacityWeight IS NOT NULL AND l.capacityWeight > " +
           "(SELECT COALESCE(SUM(i.product.weight * i.quantity), 0) FROM InventoryItem i WHERE i.location = l AND i.product.weight IS NOT NULL)")
    List<Location> findLocationsWithAvailableWeightCapacity();

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

    @Query("SELECT l FROM Location l WHERE l.xCoordinate IS NOT NULL AND l.yCoordinate IS NOT NULL " +
           "ORDER BY SQRT(POWER(l.xCoordinate - :x, 2) + POWER(l.yCoordinate - :y, 2)) ASC")
    List<Location> findNearestLocations(@Param("x") BigDecimal x, @Param("y") BigDecimal y);

    @Query("SELECT COUNT(l) FROM Location l WHERE l.zone.id = :zoneId")
    long countByZoneId(@Param("zoneId") Long zoneId);

    @Query("SELECT COUNT(l) FROM Location l WHERE l.zone.id = :zoneId AND l.active = true")
    long countActiveByZoneId(@Param("zoneId") Long zoneId);

    long countByType(LocationType type);

    long countByActiveTrue();

    long countByActiveFalse();

    long countByPickableTrue();

    long countByReceivableTrue();

    @Query("SELECT COUNT(l) FROM Location l WHERE SIZE(l.inventoryItems) = 0")
    long countEmptyLocations();

    @Query("SELECT COUNT(l) FROM Location l WHERE SIZE(l.inventoryItems) > 0")
    long countOccupiedLocations();

    @Query("SELECT COUNT(l) FROM Location l WHERE l.capacityItems IS NOT NULL AND SIZE(l.inventoryItems) >= l.capacityItems")
    long countFullLocations();

    @Query("SELECT " +
           "COUNT(l) as totalLocations, " +
           "SUM(CASE WHEN l.active = true THEN 1 ELSE 0 END) as activeLocations, " +
           "SUM(CASE WHEN l.pickable = true THEN 1 ELSE 0 END) as pickableLocations, " +
           "SUM(CASE WHEN l.receivable = true THEN 1 ELSE 0 END) as receivableLocations, " +
           "SUM(CASE WHEN SIZE(l.inventoryItems) = 0 THEN 1 ELSE 0 END) as emptyLocations, " +
           "SUM(CASE WHEN SIZE(l.inventoryItems) > 0 THEN 1 ELSE 0 END) as occupiedLocations " +
           "FROM Location l")
    Object[] getLocationStatistics();

    @Query("SELECT l, SIZE(l.inventoryItems) as inventoryCount FROM Location l ORDER BY SIZE(l.inventoryItems) DESC")
    List<Object[]> getLocationsWithInventoryCount();

    @Query("SELECT l.zone.name, COUNT(l) as locationCount, " +
           "SUM(CASE WHEN l.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN SIZE(l.inventoryItems) = 0 THEN 1 ELSE 0 END) as emptyCount, " +
           "AVG(SIZE(l.inventoryItems)) as avgInventoryCount " +
           "FROM Location l GROUP BY l.zone.name ORDER BY l.zone.name")
    List<Object[]> getLocationStatsByZone();

    @Query("SELECT l.type, COUNT(l) as locationCount, " +
           "SUM(CASE WHEN l.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN SIZE(l.inventoryItems) = 0 THEN 1 ELSE 0 END) as emptyCount, " +
           "AVG(SIZE(l.inventoryItems)) as avgInventoryCount " +
           "FROM Location l GROUP BY l.type ORDER BY l.type")
    List<Object[]> getLocationStatsByType();

    @Query("SELECT DISTINCT l FROM Location l JOIN FETCH l.zone")
    List<Location> findAllWithZone();

    @Query("SELECT l FROM Location l WHERE l.createdBy = :username")
    List<Location> findCreatedBy(@Param("username") String username);

    @Query("SELECT l FROM Location l ORDER BY l.createdAt DESC")
    List<Location> findMostRecentlyCreated();

    @Query("SELECT DISTINCT l.aisle FROM Location l WHERE l.aisle IS NOT NULL ORDER BY l.aisle")
    List<String> findAllAisles();

    @Query("SELECT DISTINCT l.rack FROM Location l WHERE l.rack IS NOT NULL ORDER BY l.rack")
    List<String> findAllRacks();

    @Query("SELECT DISTINCT l.shelf FROM Location l WHERE l.shelf IS NOT NULL ORDER BY l.shelf")
    List<String> findAllShelves();

    @Query("SELECT DISTINCT l.bin FROM Location l WHERE l.bin IS NOT NULL ORDER BY l.bin")
    List<String> findAllBins();

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

    @Query("SELECT l FROM Location l WHERE " +
           "l.active = true AND l.receivable = true AND " +
           "(:requiredVolume IS NULL OR l.capacityVolume IS NULL OR l.capacityVolume >= :requiredVolume) AND " +
           "(:requiredWeight IS NULL OR l.capacityWeight IS NULL OR l.capacityWeight >= :requiredWeight) AND " +
           "SIZE(l.inventoryItems) < COALESCE(l.capacityItems, 999999)")
    List<Location> findLocationsWithCapacity(@Param("requiredVolume") BigDecimal requiredVolume,
                                            @Param("requiredWeight") BigDecimal requiredWeight);

    @Query("SELECT l FROM Location l WHERE " +
           "l.active = true AND l.pickable = true AND " +
           "SIZE(l.inventoryItems) > 0 " +
           "ORDER BY l.zone.pickingPriority ASC, l.code ASC")
    List<Location> findPickingLocationsByPriority();
}