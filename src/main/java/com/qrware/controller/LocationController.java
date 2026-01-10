package com.qrware.controller;

import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.LocationType;
import com.qrware.repository.warehouse.LocationRepository;
import com.qrware.repository.warehouse.ZoneRepository;
import com.qrware.exception.ResourceNotFoundException;

import com.qrware.dto.LocationDTO;
import com.qrware.dto.DTOMapper;

import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private DTOMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<Page<LocationDTO>> getAllLocations(
            Pageable pageable,
            @RequestParam(required = false) Boolean active
    ) {
        Page<Location> locationsPage;

        if (active == null) {
            locationsPage = locationRepository.findAll(pageable);
        } else {
            locationsPage = locationRepository.findByActive(active, pageable);
        }

        Page<LocationDTO> locationsDTOPage = locationsPage.map(mapper::toDTO);
        return ResponseEntity.ok(locationsDTOPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));

        return ResponseEntity.ok(mapper.toDTO(location));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<LocationDTO> getLocationByCode(@PathVariable String code) {
        Location location = locationRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "code", code));

        return ResponseEntity.ok(mapper.toDTO(location));
    }

    @GetMapping("/qrcode/{qrCode}")
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<LocationDTO> getLocationByQrCode(@PathVariable String qrCode) {
        Location location = locationRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "qrCode", qrCode));

        return ResponseEntity.ok(mapper.toDTO(location));
    }

    @GetMapping("/zone/{zoneId}")
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<List<LocationDTO>> getLocationsByZone(@PathVariable Long zoneId) {
        List<Location> locations = locationRepository.findByZoneId(zoneId);

        List<LocationDTO> locationDTOs = locations.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locationDTOs);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<List<LocationDTO>> searchLocations(@RequestParam String query) {
        List<Location> locations = locationRepository.searchLocations(query);

        List<LocationDTO> locationDTOs = locations.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locationDTOs);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<List<LocationDTO>> getActiveLocations() {
        List<Location> locations = locationRepository.findByActiveTrue();

        List<LocationDTO> locationDTOs = locations.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locationDTOs);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody CreateLocationRequest request) {
        if (locationRepository.existsByCode(request.getCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (request.getQrCode() != null && !request.getQrCode().isEmpty() && locationRepository.existsByQrCode(request.getQrCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", request.getZoneId()));

        Location location = new Location();
        location.setCode(request.getCode());
        location.setName(request.getName());
        location.setZone(zone);
        location.setDescription(request.getDescription());
        location.setType(request.getType());
        location.setAisle(request.getAisle());
        location.setRack(request.getRack());
        location.setShelf(request.getShelf());
        location.setBin(request.getBin());
        location.setCapacityVolume(request.getCapacityVolume());
        location.setCapacityWeight(request.getCapacityWeight());
        location.setCapacityItems(request.getCapacityItems());
        location.setTemperatureControlled(request.getTemperatureControlled() != null ? request.getTemperatureControlled() : false);
        location.setTemperatureMin(request.getTemperatureMin());
        location.setTemperatureMax(request.getTemperatureMax());
        location.setHumidityControlled(request.getHumidityControlled() != null ? request.getHumidityControlled() : false);
        location.setHumidityMin(request.getHumidityMin());
        location.setHumidityMax(request.getHumidityMax());
        location.setHazardousMaterials(request.getHazardousMaterials() != null ? request.getHazardousMaterials() : false);
        location.setFragileItems(request.getFragileItems() != null ? request.getFragileItems() : false);
        location.setSecurityLevel(request.getSecurityLevel() != null ? request.getSecurityLevel() : 1);
        location.setActive(request.getActive() != null ? request.getActive() : true);
        location.setPickable(request.getPickable() != null ? request.getPickable() : true);
        location.setReceivable(request.getReceivable() != null ? request.getReceivable() : true);
        location.setQrCode(request.getQrCode());
        location.setBarcode(request.getBarcode());
        location.setxCoordinate(request.getXCoordinate());
        location.setyCoordinate(request.GetYCoordinate());
        location.setzCoordinate(request.GetZCoordinate());


        Location savedLocation = locationRepository.save(location);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(savedLocation));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateLocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));

        if (request.getQrCode() != null && !request.getQrCode().equals(location.getQrCode())) {
            if (!request.getQrCode().isEmpty() && locationRepository.existsByQrCodeAndIdNot(request.getQrCode(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            location.setQrCode(request.getQrCode());
        }

        if (request.getName() != null) location.setName(request.getName());
        if (request.getDescription() != null) location.setDescription(request.getDescription());
        if (request.getType() != null) location.setType(request.getType());
        if (request.getAisle() != null) location.setAisle(request.getAisle());
        if (request.getRack() != null) location.setRack(request.getRack());
        if (request.getShelf() != null) location.setShelf(request.getShelf());
        if (request.getBin() != null) location.setBin(request.getBin());
        if (request.getCapacityVolume() != null) location.setCapacityVolume(request.getCapacityVolume());
        if (request.getCapacityWeight() != null) location.setCapacityWeight(request.getCapacityWeight());
        if (request.getCapacityItems() != null) location.setCapacityItems(request.getCapacityItems());
        if (request.getTemperatureControlled() != null) location.setTemperatureControlled(request.getTemperatureControlled());
        if (request.getTemperatureMin() != null) location.setTemperatureMin(request.getTemperatureMin());
        if (request.getTemperatureMax() != null) location.setTemperatureMax(request.getTemperatureMax());
        if (request.getHumidityControlled() != null) location.setHumidityControlled(request.getHumidityControlled());
        if (request.getHumidityMin() != null) location.setHumidityMin(request.getHumidityMin());
        if (request.getHumidityMax() != null) location.setHumidityMax(request.getHumidityMax());
        if (request.getHazardousMaterials() != null) location.setHazardousMaterials(request.getHazardousMaterials());
        if (request.getFragileItems() != null) location.setFragileItems(request.getFragileItems());
        if (request.getSecurityLevel() != null) location.setSecurityLevel(request.getSecurityLevel());
        if (request.getActive() != null) location.setActive(request.getActive());
        if (request.getPickable() != null) location.setPickable(request.getPickable());
        if (request.getReceivable() != null) location.setReceivable(request.getReceivable());
        if (request.getBarcode() != null) location.setBarcode(request.getBarcode());
        if (request.getXCoordinate() != null) location.setxCoordinate(request.getXCoordinate());
        if (request.getYCoordinate() != null) location.setyCoordinate(request.getYCoordinate());
        if (request.getZCoordinate() != null) location.setzCoordinate(request.getZCoordinate());

        if (request.getZoneId() != null) {
            Zone zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", request.getZoneId()));
            location.setZone(zone);
        }

        Location updatedLocation = locationRepository.save(location);

        return ResponseEntity.ok(mapper.toDTO(updatedLocation));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_DELETE')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));

        if (location.getInventoryItems() != null && !location.getInventoryItems().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Reason", "Location is not empty")
                    .build();
        }

        location.setActive(false);
        locationRepository.save(location);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    public ResponseEntity<LocationDTO> toggleLocationActive(@PathVariable Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));

        location.setActive(!location.getActive());
        Location updatedLocation = locationRepository.save(location);

        return ResponseEntity.ok(mapper.toDTO(updatedLocation));
    }


    public static class CreateLocationRequest {
        @NotBlank(message = "Kod lokalizacji jest wymagany")
        @Size(max = 50)
        private String code;

        @NotBlank(message = "Nazwa lokalizacji jest wymagana")
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        @NotNull(message = "ID strefy jest wymagane")
        private Long zoneId;

        @NotNull(message = "Typ lokalizacji jest wymagany")
        private LocationType type;

        @Size(max = 10)
        private String aisle;
        @Size(max = 10)
        private String rack;
        @Size(max = 10)
        private String shelf;

        @Size(max = 10)
        private String bin;

        private BigDecimal capacityVolume;
        private BigDecimal capacityWeight;
        private Integer capacityItems;
        private Boolean temperatureControlled;
        private Integer temperatureMin;
        private Integer temperatureMax;
        private Boolean humidityControlled;
        private Integer humidityMin;
        private Integer humidityMax;
        private Boolean hazardousMaterials;
        private Boolean fragileItems;
        private Integer securityLevel;
        private Boolean active;
        private Boolean pickable;
        private Boolean receivable;

        @Size(max = 100)
        private String qrCode;

        @Size(max = 50)
        private String barcode;

        private BigDecimal xCoordinate;
        private BigDecimal yCoordinate;
        private BigDecimal zCoordinate;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getZoneId() { return zoneId; }
        public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
        public LocationType getType() { return type; }
        public void setType(LocationType type) { this.type = type; }
        public String getAisle() { return aisle; }
        public void setAisle(String aisle) { this.aisle = aisle; }
        public String getRack() { return rack; }
        public void setRack(String rack) { this.rack = rack; }
        public String getShelf() { return shelf; }
        public void setShelf(String shelf) { this.shelf = shelf; }
        public String getBin() { return bin; }
        public void setBin(String bin) { this.bin = bin; }
        public BigDecimal getCapacityVolume() { return capacityVolume; }
        public void setCapacityVolume(BigDecimal capacityVolume) { this.capacityVolume = capacityVolume; }
        public BigDecimal getCapacityWeight() { return capacityWeight; }
        public void setCapacityWeight(BigDecimal capacityWeight) { this.capacityWeight = capacityWeight; }
        public Integer getCapacityItems() { return capacityItems; }
        public void setCapacityItems(Integer capacityItems) { this.capacityItems = capacityItems; }
        public Boolean getTemperatureControlled() { return temperatureControlled; }
        public void setTemperatureControlled(Boolean temperatureControlled) { this.temperatureControlled = temperatureControlled; }
        public Integer getTemperatureMin() { return temperatureMin; }
        public void setTemperatureMin(Integer temperatureMin) { this.temperatureMin = temperatureMin; }
        public Integer getTemperatureMax() { return temperatureMax; }
        public void setTemperatureMax(Integer temperatureMax) { this.temperatureMax = temperatureMax; }
        public Boolean getHumidityControlled() { return humidityControlled; }
        public void setHumidityControlled(Boolean humidityControlled) { this.humidityControlled = humidityControlled; }
        public Integer getHumidityMin() { return humidityMin; }
        public void setHumidityMin(Integer humidityMin) { this.humidityMin = humidityMin; }
        public Integer getHumidityMax() { return humidityMax; }
        public void setHumidityMax(Integer humidityMax) { this.humidityMax = humidityMax; }
        public Boolean getHazardousMaterials() { return hazardousMaterials; }
        public void setHazardousMaterials(Boolean hazardousMaterials) { this.hazardousMaterials = hazardousMaterials; }
        public Boolean getFragileItems() { return fragileItems; }
        public void setFragileItems(Boolean fragileItems) { this.fragileItems = fragileItems; }
        public Integer getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(Integer securityLevel) { this.securityLevel = securityLevel; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public Boolean getPickable() { return pickable; }
        public void setPickable(Boolean pickable) { this.pickable = pickable; }
        public Boolean getReceivable() { return receivable; }
        public void setReceivable(Boolean receivable) { this.receivable = receivable; }
        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }
        public BigDecimal getXCoordinate() { return xCoordinate; }
        public void setXCoordinate(BigDecimal xCoordinate) { this.xCoordinate = xCoordinate; }
        public BigDecimal GetYCoordinate() { return yCoordinate; }
        public void setYCoordinate(BigDecimal yCoordinate) { this.yCoordinate = yCoordinate; }
        public BigDecimal GetZCoordinate() { return zCoordinate; }
        public void setZCoordinate(BigDecimal zCoordinate) { this.zCoordinate = zCoordinate; }
    }

    public static class UpdateLocationRequest {
        @Size(max = 100)
        private String name;
        @Size(max = 500)
        private String description;
        private Long zoneId;
        private LocationType type;
        @Size(max = 10)
        private String aisle;
        @Size(max = 10)
        private String rack;
        @Size(max = 10)
        private String shelf;
        @Size(max = 10)
        private String bin;
        private BigDecimal capacityVolume;
        private BigDecimal capacityWeight;
        private Integer capacityItems;
        private Boolean temperatureControlled;
        private Integer temperatureMin;
        private Integer temperatureMax;
        private Boolean humidityControlled;
        private Integer humidityMin;
        private Integer humidityMax;
        private Boolean hazardousMaterials;
        private Boolean fragileItems;
        private Integer securityLevel;
        private Boolean active;
        private Boolean pickable;
        private Boolean receivable;
        @Size(max = 100)
        private String qrCode;
        @Size(max = 50)
        private String barcode;
        private BigDecimal xCoordinate;
        private BigDecimal yCoordinate;
        private BigDecimal zCoordinate;


        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getZoneId() { return zoneId; }
        public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
        public LocationType getType() { return type; }
        public void setType(LocationType type) { this.type = type; }
        public String getAisle() { return aisle; }
        public void setAisle(String aisle) { this.aisle = aisle; }
        public String getRack() { return rack; }
        public void setRack(String rack) { this.rack = rack; }
        public String getShelf() { return shelf; }
        public void setShelf(String shelf) { this.shelf = shelf; }
        public String getBin() { return bin; }
        public void setBin(String bin) { this.bin = bin; }
        public BigDecimal getCapacityVolume() { return capacityVolume; }
        public void setCapacityVolume(BigDecimal capacityVolume) { this.capacityVolume = capacityVolume; }
        public BigDecimal getCapacityWeight() { return capacityWeight; }
        public void setCapacityWeight(BigDecimal capacityWeight) { this.capacityWeight = capacityWeight; }
        public Integer getCapacityItems() { return capacityItems; }
        public void setCapacityItems(Integer capacityItems) { this.capacityItems = capacityItems; }
        public Boolean getTemperatureControlled() { return temperatureControlled; }
        public void setTemperatureControlled(Boolean temperatureControlled) { this.temperatureControlled = temperatureControlled; }
        public Integer getTemperatureMin() { return temperatureMin; }
        public void setTemperatureMin(Integer temperatureMin) { this.temperatureMin = temperatureMin; }
        public Integer getTemperatureMax() { return temperatureMax; }
        public void setTemperatureMax(Integer temperatureMax) { this.temperatureMax = temperatureMax; }
        public Boolean getHumidityControlled() { return humidityControlled; }
        public void setHumidityControlled(Boolean humidityControlled) { this.humidityControlled = humidityControlled; }
        public Integer getHumidityMin() { return humidityMin; }
        public void setHumidityMin(Integer humidityMin) { this.humidityMin = humidityMin; }
        public Integer getHumidityMax() { return humidityMax; }
        public void setHumidityMax(Integer humidityMax) { this.humidityMax = humidityMax; }
        public Boolean getHazardousMaterials() { return hazardousMaterials; }
        public void setHazardousMaterials(Boolean hazardousMaterials) { this.hazardousMaterials = hazardousMaterials; }
        public Boolean getFragileItems() { return fragileItems; }
        public void setFragileItems(Boolean fragileItems) { this.fragileItems = fragileItems; }
        public Integer getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(Integer securityLevel) { this.securityLevel = securityLevel; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public Boolean getPickable() { return pickable; }
        public void setPickable(Boolean pickable) { this.pickable = pickable; }
        public Boolean getReceivable() { return receivable; }
        public void setReceivable(Boolean receivable) { this.receivable = receivable; }
        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }
        public BigDecimal getXCoordinate() { return xCoordinate; }
        public void setXCoordinate(BigDecimal xCoordinate) { this.xCoordinate = xCoordinate; }
        public BigDecimal getYCoordinate() { return yCoordinate; }
        public void setYCoordinate(BigDecimal yCoordinate) { this.yCoordinate = yCoordinate; }
        public BigDecimal getZCoordinate() { return zCoordinate; }
        public void setzCoordinate(BigDecimal zCoordinate) { this.zCoordinate = zCoordinate; }
    }
}