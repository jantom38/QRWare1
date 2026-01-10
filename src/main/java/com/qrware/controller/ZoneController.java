package com.qrware.controller;

import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.ZoneType;
import com.qrware.repository.warehouse.ZoneRepository;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.dto.ZoneDTO;
import com.qrware.dto.DTOMapper;

import jakarta.validation.constraints.Min;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = "*")
public class ZoneController {

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private DTOMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('ZONE_READ')")
    public ResponseEntity<Page<ZoneDTO>> getAllZones(
            Pageable pageable,
            @RequestParam(required = false) Boolean active
    ) {
        Page<Zone> zonesPage;

        if (active == null) {
            zonesPage = zoneRepository.findAll(pageable);
        } else {
            zonesPage = zoneRepository.findByActive(active, pageable);
        }

        Page<ZoneDTO> zonesDTOPage = zonesPage.map(mapper::toDTO);
        return ResponseEntity.ok(zonesDTOPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ZONE_READ')")
    public ResponseEntity<ZoneDTO> getZoneById(@PathVariable Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));
        return ResponseEntity.ok(mapper.toDTO(zone));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('ZONE_READ')")
    public ResponseEntity<ZoneDTO> getZoneByCode(@PathVariable String code) {
        Zone zone = zoneRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "code", code));
        return ResponseEntity.ok(mapper.toDTO(zone));
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasAuthority('ZONE_READ')")
    public ResponseEntity<ZoneDTO> getZoneByName(@PathVariable String name) {
        Zone zone = zoneRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "name", name));
        return ResponseEntity.ok(mapper.toDTO(zone));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('ZONE_READ')")
    public ResponseEntity<List<ZoneDTO>> getZonesByType(@PathVariable ZoneType type) {
        List<Zone> zones = zoneRepository.findByType(type);
        List<ZoneDTO> zoneDTOs = zones.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(zoneDTOs);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ZONE_READ')")
    public ResponseEntity<List<ZoneDTO>> searchZones(@RequestParam String query) {
        List<Zone> zones = zoneRepository.searchZones(query);
        List<ZoneDTO> zoneDTOs = zones.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(zoneDTOs);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ZONE_READ')")
    public ResponseEntity<List<ZoneDTO>> getActiveZones() {
        List<Zone> zones = zoneRepository.findByActiveTrue();
        List<ZoneDTO> zoneDTOs = zones.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(zoneDTOs);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ZONE_WRITE')")
    public ResponseEntity<ZoneDTO> createZone(@Valid @RequestBody CreateZoneRequest request) {
        if (zoneRepository.existsByCode(request.getCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Reason", "Zone with this code already exists")
                    .build();
        }
        if (zoneRepository.existsByName(request.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Reason", "Zone with this name already exists")
                    .build();
        }

        Zone zone = new Zone();
        zone.setName(request.getName());
        zone.setCode(request.getCode());
        zone.setDescription(request.getDescription());
        zone.setType(request.getType());

        zone.setActive(request.getActive() != null ? request.getActive() : true);
        zone.setTemperatureControlled(request.getTemperatureControlled() != null ? request.getTemperatureControlled() : false);
        zone.setTemperatureMin(request.getTemperatureMin());
        zone.setTemperatureMax(request.getTemperatureMax());
        zone.setHumidityControlled(request.getHumidityControlled() != null ? request.getHumidityControlled() : false);
        zone.setHumidityMin(request.getHumidityMin());
        zone.setHumidityMax(request.getHumidityMax());
        zone.setSecurityLevel(request.getSecurityLevel() != null ? request.getSecurityLevel() : 1);
        zone.setHazardousMaterials(request.getHazardousMaterials() != null ? request.getHazardousMaterials() : false);
        zone.setFragileItems(request.getFragileItems() != null ? request.getFragileItems() : false);
        zone.setPickingPriority(request.getPickingPriority() != null ? request.getPickingPriority() : 5);
        zone.setManager(request.getManager());
        zone.setContactInfo(request.getContactInfo());
        zone.setColor(request.getColor());

        Zone savedZone = zoneRepository.save(zone);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(savedZone));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ZONE_WRITE')")
    public ResponseEntity<ZoneDTO> updateZone(@PathVariable Long id,
                                              @Valid @RequestBody UpdateZoneRequest request) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));

        if (request.getName() != null && !request.getName().equals(zone.getName())) {
            if (zoneRepository.existsByNameAndIdNot(request.getName(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .header("X-Error-Reason", "Zone with this name already exists")
                        .build();
            }
            zone.setName(request.getName());
        }

        if (request.getCode() != null && !request.getCode().equals(zone.getCode())) {
            if (zoneRepository.existsByCodeAndIdNot(request.getCode(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .header("X-Error-Reason", "Zone with this code already exists")
                        .build();
            }
            zone.setCode(request.getCode());
        }

        if (request.getDescription() != null) zone.setDescription(request.getDescription());
        if (request.getType() != null) zone.setType(request.getType());
        if (request.getActive() != null) zone.setActive(request.getActive());
        if (request.getTemperatureControlled() != null) zone.setTemperatureControlled(request.getTemperatureControlled());
        if (request.getTemperatureMin() != null) zone.setTemperatureMin(request.getTemperatureMin());
        if (request.getTemperatureMax() != null) zone.setTemperatureMax(request.getTemperatureMax());
        if (request.getHumidityControlled() != null) zone.setHumidityControlled(request.getHumidityControlled());
        if (request.getHumidityMin() != null) zone.setHumidityMin(request.getHumidityMin());
        if (request.getHumidityMax() != null) zone.setHumidityMax(request.getHumidityMax());
        if (request.getSecurityLevel() != null) zone.setSecurityLevel(request.getSecurityLevel());
        if (request.getHazardousMaterials() != null) zone.setHazardousMaterials(request.getHazardousMaterials());
        if (request.getFragileItems() != null) zone.setFragileItems(request.getFragileItems());
        if (request.getPickingPriority() != null) zone.setPickingPriority(request.getPickingPriority());
        if (request.getManager() != null) zone.setManager(request.getManager());
        if (request.getContactInfo() != null) zone.setContactInfo(request.getContactInfo());
        if (request.getColor() != null) zone.setColor(request.getColor());

        Zone updatedZone = zoneRepository.save(zone);
        return ResponseEntity.ok(mapper.toDTO(updatedZone));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ZONE_DELETE')")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));

        if (!zone.canBeDeleted()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Reason", "Zone contains locations and cannot be deleted")
                    .build();
        }

        zone.setActive(false);
        zoneRepository.save(zone);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('ZONE_WRITE')")
    public ResponseEntity<ZoneDTO> toggleZoneActive(@PathVariable Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));

        zone.setActive(!zone.getActive());
        Zone updatedZone = zoneRepository.save(zone);

        return ResponseEntity.ok(mapper.toDTO(updatedZone));
    }


    public static class CreateZoneRequest {
        @NotBlank(message = "Nazwa strefy jest wymagana")
        @Size(max = 100)
        private String name;

        @NotBlank(message = "Kod strefy jest wymagany")
        @Size(max = 20)
        private String code;

        @Size(max = 500)
        private String description;

        @NotNull(message = "Typ strefy jest wymagany")
        private ZoneType type;

        private Boolean active;
        private Boolean temperatureControlled;
        private Integer temperatureMin;
        private Integer temperatureMax;
        private Boolean humidityControlled;
        private Integer humidityMin;
        private Integer humidityMax;

        @Min(value = 1)
        private Integer securityLevel;

        private Boolean hazardousMaterials;
        private Boolean fragileItems;

        @Min(value = 1)
        private Integer pickingPriority;

        @Size(max = 100)
        private String manager;

        @Size(max = 200)
        private String contactInfo;

        @Size(max = 7)
        private String color;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public ZoneType getType() { return type; }
        public void setType(ZoneType type) { this.type = type; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
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
        public Integer getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(Integer securityLevel) { this.securityLevel = securityLevel; }
        public Boolean getHazardousMaterials() { return hazardousMaterials; }
        public void setHazardousMaterials(Boolean hazardousMaterials) { this.hazardousMaterials = hazardousMaterials; }
        public Boolean getFragileItems() { return fragileItems; }
        public void setFragileItems(Boolean fragileItems) { this.fragileItems = fragileItems; }
        public Integer getPickingPriority() { return pickingPriority; }
        public void setPickingPriority(Integer pickingPriority) { this.pickingPriority = pickingPriority; }
        public String getManager() { return manager; }
        public void setManager(String manager) { this.manager = manager; }
        public String getContactInfo() { return contactInfo; }
        public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class UpdateZoneRequest {
        @Size(max = 100)
        private String name;

        @Size(max = 20)
        private String code;

        @Size(max = 500)
        private String description;

        private ZoneType type;
        private Boolean active;
        private Boolean temperatureControlled;
        private Integer temperatureMin;
        private Integer temperatureMax;
        private Boolean humidityControlled;
        private Integer humidityMin;
        private Integer humidityMax;

        @Min(value = 1)
        private Integer securityLevel;

        private Boolean hazardousMaterials;
        private Boolean fragileItems;

        @Min(value = 1)
        private Integer pickingPriority;

        @Size(max = 100)
        private String manager;

        @Size(max = 200)
        private String contactInfo;

        @Size(max = 7)
        private String color;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public ZoneType getType() { return type; }
        public void setType(ZoneType type) { this.type = type; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
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
        public Integer getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(Integer securityLevel) { this.securityLevel = securityLevel; }
        public Boolean getHazardousMaterials() { return hazardousMaterials; }
        public void setHazardousMaterials(Boolean hazardousMaterials) { this.hazardousMaterials = hazardousMaterials; }
        public Boolean getFragileItems() { return fragileItems; }
        public void setFragileItems(Boolean fragileItems) { this.fragileItems = fragileItems; }
        public Integer getPickingPriority() { return pickingPriority; }
        public void setPickingPriority(Integer pickingPriority) { this.pickingPriority = pickingPriority; }
        public String getManager() { return manager; }
        public void setManager(String manager) { this.manager = manager; }
        public String getContactInfo() { return contactInfo; }
        public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }
}