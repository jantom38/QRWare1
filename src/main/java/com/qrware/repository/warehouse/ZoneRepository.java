package com.qrware.repository.warehouse;

import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.ZoneType;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends BaseRepository<Zone> {

    Optional<Zone> findByName(String name);

    Optional<Zone> findByNameIgnoreCase(String name);

    Optional<Zone> findByCode(String code);

    Optional<Zone> findByCodeIgnoreCase(String code);

    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(z) > 0 THEN true ELSE false END FROM Zone z WHERE z.name = :name AND z.id != :zoneId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("zoneId") Long zoneId);

    boolean existsByCode(String code);

    @Query("SELECT CASE WHEN COUNT(z) > 0 THEN true ELSE false END FROM Zone z WHERE z.code = :code AND z.id != :zoneId")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("zoneId") Long zoneId);

    Page<Zone> findByActive(Boolean active, Pageable pageable);

    List<Zone> findByActiveTrue();

    List<Zone> findByActiveFalse();

    List<Zone> findByActiveOrderByName(Boolean active);

    List<Zone> findByType(ZoneType type);

    List<Zone> findByTypeAndActive(ZoneType type, Boolean active);

    @Query("SELECT z FROM Zone z WHERE z.type IN :types")
    List<Zone> findByTypeIn(@Param("types") List<ZoneType> types);

    @Query("SELECT z FROM Zone z WHERE z.type IN ('STORAGE', 'COLD_STORAGE', 'FREEZER', 'BULK', 'FAST_MOVING', 'SLOW_MOVING', 'OVERFLOW', 'SEASONAL', 'HIGH_VALUE', 'AUTOMATED')")
    List<Zone> findStorageZones();

    @Query("SELECT z FROM Zone z WHERE z.type IN ('RECEIVING', 'SHIPPING', 'PICKING', 'PACKING', 'STAGING', 'CROSSDOCK', 'PRODUCTION')")
    List<Zone> findOperationalZones();

    @Query("SELECT z FROM Zone z WHERE z.type IN ('QUARANTINE', 'COLD_STORAGE', 'FREEZER', 'HAZMAT', 'HIGH_SECURITY', 'RETURNS', 'DAMAGED', 'HIGH_VALUE')")
    List<Zone> findSpecialHandlingZones();

    @Query("SELECT z FROM Zone z WHERE LOWER(z.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Zone> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT z FROM Zone z WHERE LOWER(z.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Zone> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT z FROM Zone z WHERE " +
           "LOWER(z.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(z.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(z.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Zone> searchZones(@Param("searchTerm") String searchTerm);

    List<Zone> findByTemperatureControlledTrue();

    List<Zone> findByTemperatureControlledFalse();

    List<Zone> findByHumidityControlledTrue();

    List<Zone> findByHumidityControlledFalse();

    @Query("SELECT z FROM Zone z WHERE z.temperatureControlled = true OR z.humidityControlled = true")
    List<Zone> findZonesWithEnvironmentalControls();

    @Query("SELECT z FROM Zone z WHERE " +
           "z.temperatureControlled = false OR " +
           "(z.temperatureMin IS NULL OR :temperature >= z.temperatureMin) AND " +
           "(z.temperatureMax IS NULL OR :temperature <= z.temperatureMax)")
    List<Zone> findZonesForTemperature(@Param("temperature") Integer temperature);

    @Query("SELECT z FROM Zone z WHERE " +
           "z.humidityControlled = false OR " +
           "(z.humidityMin IS NULL OR :humidity >= z.humidityMin) AND " +
           "(z.humidityMax IS NULL OR :humidity <= z.humidityMax)")
    List<Zone> findZonesForHumidity(@Param("humidity") Integer humidity);

    List<Zone> findByHazardousMaterialsTrue();

    List<Zone> findByFragileItemsTrue();

    List<Zone> findBySecurityLevel(Integer securityLevel);

    @Query("SELECT z FROM Zone z WHERE z.securityLevel >= :minLevel")
    List<Zone> findBySecurityLevelGreaterThanEqual(@Param("minLevel") Integer minLevel);

    @Query("SELECT z FROM Zone z WHERE z.securityLevel <= :maxLevel")
    List<Zone> findBySecurityLevelLessThanEqual(@Param("maxLevel") Integer maxLevel);

    @Query("SELECT z FROM Zone z WHERE z.securityLevel >= 3")
    List<Zone> findHighSecurityZones();

    List<Zone> findByPickingPriority(Integer pickingPriority);

    @Query("SELECT z FROM Zone z WHERE z.pickingPriority BETWEEN :minPriority AND :maxPriority ORDER BY z.pickingPriority ASC")
    List<Zone> findByPickingPriorityBetween(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);

    @Query("SELECT z FROM Zone z WHERE LOWER(z.manager) LIKE LOWER(CONCAT('%', :manager, '%'))")
    List<Zone> findByManagerContainingIgnoreCase(@Param("manager") String manager);

    @Query("SELECT z FROM Zone z WHERE z.manager IS NOT NULL AND z.manager != ''")
    List<Zone> findZonesWithManager();

    @Query("SELECT z FROM Zone z WHERE z.manager IS NULL OR z.manager = ''")
    List<Zone> findZonesWithoutManager();

    List<Zone> findByColor(String color);

    @Query("SELECT DISTINCT z FROM Zone z WHERE SIZE(z.locations) > 0")
    List<Zone> findZonesWithLocations();

    @Query("SELECT z FROM Zone z WHERE SIZE(z.locations) = 0")
    List<Zone> findZonesWithoutLocations();

    @Query("SELECT z FROM Zone z WHERE SIZE(z.locations) = 0")
    List<Zone> findDeletableZones();

    long countByType(ZoneType type);

    long countByActiveTrue();

    long countByActiveFalse();

    long countByTemperatureControlledTrue();

    long countByHumidityControlledTrue();

    @Query("SELECT COUNT(DISTINCT z) FROM Zone z WHERE SIZE(z.locations) > 0")
    long countZonesWithLocations();

    @Query("SELECT COUNT(z) FROM Zone z WHERE SIZE(z.locations) = 0")
    long countZonesWithoutLocations();

    long countBySecurityLevel(Integer securityLevel);

    @Query("SELECT " +
           "COUNT(z) as totalZones, " +
           "SUM(CASE WHEN z.active = true THEN 1 ELSE 0 END) as activeZones, " +
           "SUM(CASE WHEN z.temperatureControlled = true THEN 1 ELSE 0 END) as temperatureControlledZones, " +
           "SUM(CASE WHEN z.humidityControlled = true THEN 1 ELSE 0 END) as humidityControlledZones, " +
           "SUM(CASE WHEN z.hazardousMaterials = true THEN 1 ELSE 0 END) as hazmatZones, " +
           "SUM(CASE WHEN SIZE(z.locations) > 0 THEN 1 ELSE 0 END) as zonesWithLocations " +
           "FROM Zone z")
    Object[] getZoneStatistics();

    @Query("SELECT z, SIZE(z.locations) as locationCount FROM Zone z ORDER BY SIZE(z.locations) DESC")
    List<Object[]> getZonesWithLocationCount();

    @Query("SELECT z.type, COUNT(z) as zoneCount, " +
           "SUM(CASE WHEN z.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "AVG(SIZE(z.locations)) as avgLocationCount " +
           "FROM Zone z GROUP BY z.type ORDER BY z.type")
    List<Object[]> getZoneStatsByType();

    @Query("SELECT z FROM Zone z WHERE z.createdBy = :username")
    List<Zone> findCreatedBy(@Param("username") String username);

    @Query("SELECT z FROM Zone z ORDER BY z.createdAt DESC")
    List<Zone> findMostRecentlyCreated();

    @Query("SELECT z FROM Zone z WHERE z.active = true ORDER BY z.pickingPriority ASC, z.name ASC")
    List<Zone> findActiveZonesOrderedByPickingPriority();

    @Query("SELECT DISTINCT z.manager FROM Zone z WHERE z.manager IS NOT NULL AND z.manager != '' ORDER BY z.manager")
    List<String> findAllManagers();

    @Query("SELECT DISTINCT z.color FROM Zone z WHERE z.color IS NOT NULL ORDER BY z.color")
    List<String> findAllColors();

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

    @Query("SELECT DISTINCT z FROM Zone z JOIN z.locations l WHERE " +
           "z.active = true AND l.active = true AND l.receivable = true AND " +
           "SIZE(l.inventoryItems) < COALESCE(l.capacityItems, 999999)")
    List<Zone> findZonesWithAvailableCapacity();

    @Query("SELECT DISTINCT z FROM Zone z JOIN z.locations l WHERE " +
           "z.active = true AND l.active = true AND l.pickable = true AND " +
           "SIZE(l.inventoryItems) > 0")
    List<Zone> findZonesForPicking();
}