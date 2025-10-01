package com.qrware.repository.warehouse;

import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.ZoneType;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Zone entity operations
 */
@Repository
public interface ZoneRepository extends BaseRepository<Zone> {

    /**
     * Find zone by name
     */
    Optional<Zone> findByName(String name);

    /**
     * Find zone by name ignoring case
     */
    Optional<Zone> findByNameIgnoreCase(String name);

    /**
     * Find zone by code
     */
    Optional<Zone> findByCode(String code);

    /**
     * Find zone by code ignoring case
     */
    Optional<Zone> findByCodeIgnoreCase(String code);

    /**
     * Check if name exists
     */
    boolean existsByName(String name);

    /**
     * Check if name exists excluding current zone
     */
    @Query("SELECT CASE WHEN COUNT(z) > 0 THEN true ELSE false END FROM Zone z WHERE z.name = :name AND z.id != :zoneId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("zoneId") Long zoneId);

    /**
     * Check if code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if code exists excluding current zone
     */
    @Query("SELECT CASE WHEN COUNT(z) > 0 THEN true ELSE false END FROM Zone z WHERE z.code = :code AND z.id != :zoneId")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("zoneId") Long zoneId);

    /**
     * Find all active zones
     */
    List<Zone> findByActiveTrue();

    /**
     * Find all inactive zones
     */
    List<Zone> findByActiveFalse();

    /**
     * Find zones by active status ordered by name
     */
    List<Zone> findByActiveOrderByName(Boolean active);

    /**
     * Find zones by type
     */
    List<Zone> findByType(ZoneType type);

    /**
     * Find zones by type and active status
     */
    List<Zone> findByTypeAndActive(ZoneType type, Boolean active);

    /**
     * Find zones by multiple types
     */
    @Query("SELECT z FROM Zone z WHERE z.type IN :types")
    List<Zone> findByTypeIn(@Param("types") List<ZoneType> types);

    /**
     * Find storage zones
     */
    @Query("SELECT z FROM Zone z WHERE z.type IN ('STORAGE', 'COLD_STORAGE', 'FREEZER', 'BULK', 'FAST_MOVING', 'SLOW_MOVING', 'OVERFLOW', 'SEASONAL', 'HIGH_VALUE', 'AUTOMATED')")
    List<Zone> findStorageZones();

    /**
     * Find operational zones
     */
    @Query("SELECT z FROM Zone z WHERE z.type IN ('RECEIVING', 'SHIPPING', 'PICKING', 'PACKING', 'STAGING', 'CROSSDOCK', 'PRODUCTION')")
    List<Zone> findOperationalZones();

    /**
     * Find special handling zones
     */
    @Query("SELECT z FROM Zone z WHERE z.type IN ('QUARANTINE', 'COLD_STORAGE', 'FREEZER', 'HAZMAT', 'HIGH_SECURITY', 'RETURNS', 'DAMAGED', 'HIGH_VALUE')")
    List<Zone> findSpecialHandlingZones();

    /**
     * Find zones by name containing keyword
     */
    @Query("SELECT z FROM Zone z WHERE LOWER(z.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Zone> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Find zones by description containing keyword
     */
    @Query("SELECT z FROM Zone z WHERE LOWER(z.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Zone> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Search zones by name, code, or description
     */
    @Query("SELECT z FROM Zone z WHERE " +
           "LOWER(z.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(z.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(z.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Zone> searchZones(@Param("searchTerm") String searchTerm);

    /**
     * Find temperature controlled zones
     */
    List<Zone> findByTemperatureControlledTrue();

    /**
     * Find zones without temperature control
     */
    List<Zone> findByTemperatureControlledFalse();

    /**
     * Find humidity controlled zones
     */
    List<Zone> findByHumidityControlledTrue();

    /**
     * Find zones without humidity control
     */
    List<Zone> findByHumidityControlledFalse();

    /**
     * Find zones with environmental controls
     */
    @Query("SELECT z FROM Zone z WHERE z.temperatureControlled = true OR z.humidityControlled = true")
    List<Zone> findZonesWithEnvironmentalControls();

    /**
     * Find zones for specific temperature
     */
    @Query("SELECT z FROM Zone z WHERE " +
           "z.temperatureControlled = false OR " +
           "(z.temperatureMin IS NULL OR :temperature >= z.temperatureMin) AND " +
           "(z.temperatureMax IS NULL OR :temperature <= z.temperatureMax)")
    List<Zone> findZonesForTemperature(@Param("temperature") Integer temperature);

    /**
     * Find zones for specific humidity
     */
    @Query("SELECT z FROM Zone z WHERE " +
           "z.humidityControlled = false OR " +
           "(z.humidityMin IS NULL OR :humidity >= z.humidityMin) AND " +
           "(z.humidityMax IS NULL OR :humidity <= z.humidityMax)")
    List<Zone> findZonesForHumidity(@Param("humidity") Integer humidity);

    /**
     * Find zones for hazardous materials
     */
    List<Zone> findByHazardousMaterialsTrue();

    /**
     * Find zones for fragile items
     */
    List<Zone> findByFragileItemsTrue();

    /**
     * Find zones by security level
     */
    List<Zone> findBySecurityLevel(Integer securityLevel);

    /**
     * Find zones with minimum security level
     */
    @Query("SELECT z FROM Zone z WHERE z.securityLevel >= :minLevel")
    List<Zone> findBySecurityLevelGreaterThanEqual(@Param("minLevel") Integer minLevel);

    /**
     * Find zones with maximum security level
     */
    @Query("SELECT z FROM Zone z WHERE z.securityLevel <= :maxLevel")
    List<Zone> findBySecurityLevelLessThanEqual(@Param("maxLevel") Integer maxLevel);

    /**
     * Find high security zones
     */
    @Query("SELECT z FROM Zone z WHERE z.securityLevel >= 3")
    List<Zone> findHighSecurityZones();

    /**
     * Find zones by picking priority
     */
    List<Zone> findByPickingPriority(Integer pickingPriority);

    /**
     * Find zones by picking priority range
     */
    @Query("SELECT z FROM Zone z WHERE z.pickingPriority BETWEEN :minPriority AND :maxPriority ORDER BY z.pickingPriority ASC")
    List<Zone> findByPickingPriorityBetween(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);

    /**
     * Find zones by manager
     */
    @Query("SELECT z FROM Zone z WHERE LOWER(z.manager) LIKE LOWER(CONCAT('%', :manager, '%'))")
    List<Zone> findByManagerContainingIgnoreCase(@Param("manager") String manager);

    /**
     * Find zones with manager assigned
     */
    @Query("SELECT z FROM Zone z WHERE z.manager IS NOT NULL AND z.manager != ''")
    List<Zone> findZonesWithManager();

    /**
     * Find zones without manager
     */
    @Query("SELECT z FROM Zone z WHERE z.manager IS NULL OR z.manager = ''")
    List<Zone> findZonesWithoutManager();

    /**
     * Find zones by color
     */
    List<Zone> findByColor(String color);

    /**
     * Find zones with locations
     */
    @Query("SELECT DISTINCT z FROM Zone z WHERE SIZE(z.locations) > 0")
    List<Zone> findZonesWithLocations();

    /**
     * Find zones without locations
     */
    @Query("SELECT z FROM Zone z WHERE SIZE(z.locations) = 0")
    List<Zone> findZonesWithoutLocations();

    /**
     * Find zones that can be deleted (no locations)
     */
    @Query("SELECT z FROM Zone z WHERE SIZE(z.locations) = 0")
    List<Zone> findDeletableZones();

    /**
     * Count zones by type
     */
    long countByType(ZoneType type);

    /**
     * Count active zones
     */
    long countByActiveTrue();

    /**
     * Count inactive zones
     */
    long countByActiveFalse();

    /**
     * Count temperature controlled zones
     */
    long countByTemperatureControlledTrue();

    /**
     * Count humidity controlled zones
     */
    long countByHumidityControlledTrue();

    /**
     * Count zones with locations
     */
    @Query("SELECT COUNT(DISTINCT z) FROM Zone z WHERE SIZE(z.locations) > 0")
    long countZonesWithLocations();

    /**
     * Count zones without locations
     */
    @Query("SELECT COUNT(z) FROM Zone z WHERE SIZE(z.locations) = 0")
    long countZonesWithoutLocations();

    /**
     * Count zones by security level
     */
    long countBySecurityLevel(Integer securityLevel);

    /**
     * Get zone statistics
     */
    @Query("SELECT " +
           "COUNT(z) as totalZones, " +
           "SUM(CASE WHEN z.active = true THEN 1 ELSE 0 END) as activeZones, " +
           "SUM(CASE WHEN z.temperatureControlled = true THEN 1 ELSE 0 END) as temperatureControlledZones, " +
           "SUM(CASE WHEN z.humidityControlled = true THEN 1 ELSE 0 END) as humidityControlledZones, " +
           "SUM(CASE WHEN z.hazardousMaterials = true THEN 1 ELSE 0 END) as hazmatZones, " +
           "SUM(CASE WHEN SIZE(z.locations) > 0 THEN 1 ELSE 0 END) as zonesWithLocations " +
           "FROM Zone z")
    Object[] getZoneStatistics();

    /**
     * Get zones with location count
     */
    @Query("SELECT z, SIZE(z.locations) as locationCount FROM Zone z ORDER BY SIZE(z.locations) DESC")
    List<Object[]> getZonesWithLocationCount();

    /**
     * Get zone statistics by type
     */
    @Query("SELECT z.type, COUNT(z) as zoneCount, " +
           "SUM(CASE WHEN z.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "AVG(SIZE(z.locations)) as avgLocationCount " +
           "FROM Zone z GROUP BY z.type ORDER BY z.type")
    List<Object[]> getZoneStatsByType();

    /**
     * Find zones created by user
     */
    @Query("SELECT z FROM Zone z WHERE z.createdBy = :username")
    List<Zone> findCreatedBy(@Param("username") String username);

    /**
     * Find most recently created zones
     */
    @Query("SELECT z FROM Zone z ORDER BY z.createdAt DESC")
    List<Zone> findMostRecentlyCreated();

    /**
     * Find zones ordered by picking priority
     */
    @Query("SELECT z FROM Zone z WHERE z.active = true ORDER BY z.pickingPriority ASC, z.name ASC")
    List<Zone> findActiveZonesOrderedByPickingPriority();

    /**
     * Find all unique managers
     */
    @Query("SELECT DISTINCT z.manager FROM Zone z WHERE z.manager IS NOT NULL AND z.manager != '' ORDER BY z.manager")
    List<String> findAllManagers();

    /**
     * Find all unique colors
     */
    @Query("SELECT DISTINCT z.color FROM Zone z WHERE z.color IS NOT NULL ORDER BY z.color")
    List<String> findAllColors();

    /**
     * Find zones suitable for product characteristics
     */
    @Query("SELECT z FROM Zone z WHERE " +
           "z.active = true AND " +
           "(:requiresTemperatureControl = false OR z.temperatureControlled = true) AND " +
           "(:requiresHumidityControl = false OR z.humidityControlled = true) AND " +
           "(:requiresHazmatHandling = false OR z.hazardousMaterials = true) AND " +
           "(:requiresFragileHandling = false OR z.fragileItems = true) AND " +
           "z.securityLevel >= :minSecurityLevel")
    List<Zone> findSuitableZones(@Param("requiresTemperatureControl") Boolean requiresTemperatureControl,
                                @Param("requiresHumidityControl") Boolean requiresHumidityControl,
                                @Param("requiresHazmatHandling") Boolean requiresHazmatHandling,
                                @Param("requiresFragileHandling") Boolean requiresFragileHandling,
                                @Param("minSecurityLevel") Integer minSecurityLevel);

    /**
     * Find zones with available capacity
     */
    @Query("SELECT DISTINCT z FROM Zone z JOIN z.locations l WHERE " +
           "z.active = true AND l.active = true AND l.receivable = true AND " +
           "SIZE(l.inventoryItems) < COALESCE(l.capacityItems, 999999)")
    List<Zone> findZonesWithAvailableCapacity();

    /**
     * Find zones for picking operations
     */
    @Query("SELECT DISTINCT z FROM Zone z JOIN z.locations l WHERE " +
           "z.active = true AND l.active = true AND l.pickable = true AND " +
           "SIZE(l.inventoryItems) > 0")
    List<Zone> findZonesForPicking();
}