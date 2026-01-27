package com.qrware.controller;

import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.ZoneType;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.ZoneDTO;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.warehouse.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoneControllerTest {

    private ZoneDTO createZoneDTO(Long id) {
        return new ZoneDTO(
            id,
            "Test Zone",
            "ZONE-" + id,
            "Test Description",
            ZoneType.STORAGE,
            true,
            false,
            null,
            null,
            false,
            null,
            null,
            1,
            false,
            false,
            1,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            0,
            0L,
            0L,
            0.0
        );
    }

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private DTOMapper mapper;

    @InjectMocks
    private ZoneController zoneController;

    @Test
    void getAllZones_ShouldReturnPagedResults() {
        Zone zone = new Zone();
        zone.setId(1L);
        zone.setCode("ZONE-001");
        
        Page<Zone> page = new PageImpl<>(List.of(zone));
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(mapper.toDTO(any(Zone.class))).thenReturn(dto);

        ResponseEntity<Page<ZoneDTO>> response = zoneController.getAllZones(Pageable.unpaged(), null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getAllZones_ShouldFilterByActive_WhenActiveParamProvided() {
        Zone zone = new Zone();
        zone.setId(1L);
        zone.setActive(true);
        
        Page<Zone> page = new PageImpl<>(List.of(zone));
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.findByActive(eq(true), any(Pageable.class))).thenReturn(page);
        when(mapper.toDTO(any(Zone.class))).thenReturn(dto);

        ResponseEntity<Page<ZoneDTO>> response = zoneController.getAllZones(Pageable.unpaged(), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(zoneRepository).findByActive(eq(true), any(Pageable.class));
    }

    @Test
    void getZoneById_ShouldReturnZone_WhenFound() {
        Long id = 1L;
        Zone zone = new Zone();
        zone.setId(id);
        zone.setCode("ZONE-001");
        
        ZoneDTO dto = createZoneDTO(id);
        
        when(zoneRepository.findById(id)).thenReturn(Optional.of(zone));
        when(mapper.toDTO(zone)).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.getZoneById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().id());
    }

    @Test
    void getZoneById_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(zoneRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> zoneController.getZoneById(id));
    }

    @Test
    void getZoneByCode_ShouldReturnZone_WhenFound() {
        String code = "ZONE-001";
        Zone zone = new Zone();
        zone.setId(1L);
        zone.setCode(code);
        
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.findByCodeIgnoreCase(code)).thenReturn(Optional.of(zone));
        when(mapper.toDTO(zone)).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.getZoneByCode(code);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getZoneByCode_ShouldThrowException_WhenNotFound() {
        String code = "INVALID";
        when(zoneRepository.findByCodeIgnoreCase(code)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> zoneController.getZoneByCode(code));
    }

    @Test
    void getZoneByName_ShouldReturnZone_WhenFound() {
        String name = "Main Warehouse";
        Zone zone = new Zone();
        zone.setId(1L);
        zone.setName(name);
        
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.findByNameIgnoreCase(name)).thenReturn(Optional.of(zone));
        when(mapper.toDTO(zone)).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.getZoneByName(name);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getZoneByName_ShouldThrowException_WhenNotFound() {
        String name = "Unknown Zone";
        when(zoneRepository.findByNameIgnoreCase(name)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> zoneController.getZoneByName(name));
    }

    @Test
    void getZonesByType_ShouldReturnFilteredList() {
        ZoneType type = ZoneType.STORAGE;
        Zone zone = new Zone();
        zone.setId(1L);
        zone.setType(type);
        
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.findByType(type)).thenReturn(List.of(zone));
        when(mapper.toDTO(any(Zone.class))).thenReturn(dto);

        ResponseEntity<List<ZoneDTO>> response = zoneController.getZonesByType(type);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void searchZones_ShouldReturnMatchingZones() {
        String query = "storage";
        Zone zone = new Zone();
        zone.setId(1L);
        zone.setName("Cold Storage");
        
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.searchZones(query)).thenReturn(List.of(zone));
        when(mapper.toDTO(any(Zone.class))).thenReturn(dto);

        ResponseEntity<List<ZoneDTO>> response = zoneController.searchZones(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getActiveZones_ShouldReturnOnlyActiveZones() {
        Zone zone = new Zone();
        zone.setId(1L);
        zone.setActive(true);
        
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.findByActiveTrue()).thenReturn(List.of(zone));
        when(mapper.toDTO(any(Zone.class))).thenReturn(dto);

        ResponseEntity<List<ZoneDTO>> response = zoneController.getActiveZones();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void createZone_ShouldCreateAndReturn_WhenValidRequest() {
        ZoneController.CreateZoneRequest request = new ZoneController.CreateZoneRequest();
        request.setCode("ZONE-NEW");
        request.setName("New Zone");
        request.setType(ZoneType.STORAGE);
        
        Zone savedZone = new Zone();
        savedZone.setId(1L);
        savedZone.setCode("ZONE-NEW");
        savedZone.setName("New Zone");
        
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.existsByCode("ZONE-NEW")).thenReturn(false);
        when(zoneRepository.existsByName("New Zone")).thenReturn(false);
        when(zoneRepository.save(any(Zone.class))).thenReturn(savedZone);
        when(mapper.toDTO(savedZone)).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.createZone(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(zoneRepository).save(any(Zone.class));
    }

    @Test
    void createZone_ShouldReturnConflict_WhenCodeExists() {
        ZoneController.CreateZoneRequest request = new ZoneController.CreateZoneRequest();
        request.setCode("EXISTING-CODE");
        request.setName("New Zone");
        request.setType(ZoneType.STORAGE);
        
        when(zoneRepository.existsByCode("EXISTING-CODE")).thenReturn(true);

        ResponseEntity<ZoneDTO> response = zoneController.createZone(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(zoneRepository, never()).save(any(Zone.class));
    }

    @Test
    void createZone_ShouldReturnConflict_WhenNameExists() {
        ZoneController.CreateZoneRequest request = new ZoneController.CreateZoneRequest();
        request.setCode("ZONE-NEW");
        request.setName("Existing Zone");
        request.setType(ZoneType.STORAGE);
        
        when(zoneRepository.existsByCode("ZONE-NEW")).thenReturn(false);
        when(zoneRepository.existsByName("Existing Zone")).thenReturn(true);

        ResponseEntity<ZoneDTO> response = zoneController.createZone(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void createZone_ShouldSetDefaultValues_WhenOptionalFieldsNotProvided() {
        ZoneController.CreateZoneRequest request = new ZoneController.CreateZoneRequest();
        request.setCode("ZONE-NEW");
        request.setName("New Zone");
        request.setType(ZoneType.STORAGE);
        
        Zone savedZone = new Zone();
        savedZone.setId(1L);
        
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.existsByCode("ZONE-NEW")).thenReturn(false);
        when(zoneRepository.existsByName("New Zone")).thenReturn(false);
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> {
            Zone zone = invocation.getArgument(0);
            assertTrue(zone.getActive());
            assertFalse(zone.getTemperatureControlled());
            assertFalse(zone.getHumidityControlled());
            assertEquals(1, zone.getSecurityLevel());
            return savedZone;
        });
        when(mapper.toDTO(savedZone)).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.createZone(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void updateZone_ShouldUpdateAndReturn_WhenFound() {
        Long id = 1L;
        ZoneController.UpdateZoneRequest request = new ZoneController.UpdateZoneRequest();
        request.setName("Updated Name");
        request.setDescription("Updated Description");
        request.setActive(false);
        
        Zone existingZone = new Zone();
        existingZone.setId(id);
        existingZone.setName("Old Name");
        existingZone.setCode("ZONE-001");
        existingZone.setActive(true);
        
        ZoneDTO dto = createZoneDTO(id);
        
        when(zoneRepository.findById(id)).thenReturn(Optional.of(existingZone));
        when(zoneRepository.existsByNameAndIdNot("Updated Name", id)).thenReturn(false);
        when(zoneRepository.save(any(Zone.class))).thenReturn(existingZone);
        when(mapper.toDTO(any(Zone.class))).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.updateZone(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", existingZone.getName());
        assertEquals("Updated Description", existingZone.getDescription());
        assertFalse(existingZone.getActive());
    }

    @Test
    void updateZone_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        ZoneController.UpdateZoneRequest request = new ZoneController.UpdateZoneRequest();
        
        when(zoneRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> zoneController.updateZone(id, request));
    }

    @Test
    void updateZone_ShouldReturnConflict_WhenNameAlreadyUsed() {
        Long id = 1L;
        ZoneController.UpdateZoneRequest request = new ZoneController.UpdateZoneRequest();
        request.setName("Used Name");
        
        Zone existingZone = new Zone();
        existingZone.setId(id);
        existingZone.setName("Old Name");
        
        when(zoneRepository.findById(id)).thenReturn(Optional.of(existingZone));
        when(zoneRepository.existsByNameAndIdNot("Used Name", id)).thenReturn(true);

        ResponseEntity<ZoneDTO> response = zoneController.updateZone(id, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void updateZone_ShouldReturnConflict_WhenCodeAlreadyUsed() {
        Long id = 1L;
        ZoneController.UpdateZoneRequest request = new ZoneController.UpdateZoneRequest();
        request.setCode("USED-CODE");
        
        Zone existingZone = new Zone();
        existingZone.setId(id);
        existingZone.setCode("OLD-CODE");
        
        when(zoneRepository.findById(id)).thenReturn(Optional.of(existingZone));
        when(zoneRepository.existsByCodeAndIdNot("USED-CODE", id)).thenReturn(true);

        ResponseEntity<ZoneDTO> response = zoneController.updateZone(id, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void deleteZone_ShouldDeactivate_WhenZoneCanBeDeleted() {
        Long id = 1L;
        Zone zone = spy(new Zone());
        zone.setId(id);
        zone.setActive(true);
        zone.setLocations(new ArrayList<>());
        
        when(zoneRepository.findById(id)).thenReturn(Optional.of(zone));
        when(zone.canBeDeleted()).thenReturn(true);
        when(zoneRepository.save(any(Zone.class))).thenReturn(zone);

        ResponseEntity<Void> response = zoneController.deleteZone(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(zone.getActive());
    }

    @Test
    void deleteZone_ShouldReturnConflict_WhenZoneHasLocations() {
        Long id = 1L;
        Zone zone = spy(new Zone());
        zone.setId(id);
        
        when(zoneRepository.findById(id)).thenReturn(Optional.of(zone));
        when(zone.canBeDeleted()).thenReturn(false);

        ResponseEntity<Void> response = zoneController.deleteZone(id);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void deleteZone_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(zoneRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> zoneController.deleteZone(id));
    }

    @Test
    void toggleZoneActive_ShouldToggleFromTrueToFalse() {
        Long id = 1L;
        Zone zone = new Zone();
        zone.setId(id);
        zone.setActive(true);
        
        ZoneDTO dto = createZoneDTO(id);
        
        when(zoneRepository.findById(id)).thenReturn(Optional.of(zone));
        when(zoneRepository.save(any(Zone.class))).thenReturn(zone);
        when(mapper.toDTO(any(Zone.class))).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.toggleZoneActive(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(zone.getActive());
    }

    @Test
    void toggleZoneActive_ShouldToggleFromFalseToTrue() {
        Long id = 1L;
        Zone zone = new Zone();
        zone.setId(id);
        zone.setActive(false);
        
        ZoneDTO dto = createZoneDTO(id);
        
        when(zoneRepository.findById(id)).thenReturn(Optional.of(zone));
        when(zoneRepository.save(any(Zone.class))).thenReturn(zone);
        when(mapper.toDTO(any(Zone.class))).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.toggleZoneActive(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(zone.getActive());
    }

    @Test
    void toggleZoneActive_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(zoneRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> zoneController.toggleZoneActive(id));
    }

    @Test
    void createZone_ShouldSetTemperatureRange_WhenTemperatureControlled() {
        ZoneController.CreateZoneRequest request = new ZoneController.CreateZoneRequest();
        request.setCode("COLD-ZONE");
        request.setName("Cold Storage");
        request.setType(ZoneType.COLD_STORAGE);
        request.setTemperatureControlled(true);
        request.setTemperatureMin(-20);
        request.setTemperatureMax(4);
        
        Zone savedZone = new Zone();
        savedZone.setId(1L);
        
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.existsByCode("COLD-ZONE")).thenReturn(false);
        when(zoneRepository.existsByName("Cold Storage")).thenReturn(false);
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> {
            Zone zone = invocation.getArgument(0);
            assertTrue(zone.getTemperatureControlled());
            assertEquals(-20, zone.getTemperatureMin());
            assertEquals(4, zone.getTemperatureMax());
            return savedZone;
        });
        when(mapper.toDTO(savedZone)).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.createZone(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createZone_ShouldSetHumidityRange_WhenHumidityControlled() {
        ZoneController.CreateZoneRequest request = new ZoneController.CreateZoneRequest();
        request.setCode("DRY-ZONE");
        request.setName("Dry Storage");
        request.setType(ZoneType.STORAGE);
        request.setHumidityControlled(true);
        request.setHumidityMin(20);
        request.setHumidityMax(40);
        
        Zone savedZone = new Zone();
        savedZone.setId(1L);
        
        ZoneDTO dto = createZoneDTO(1L);
        
        when(zoneRepository.existsByCode("DRY-ZONE")).thenReturn(false);
        when(zoneRepository.existsByName("Dry Storage")).thenReturn(false);
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> {
            Zone zone = invocation.getArgument(0);
            assertTrue(zone.getHumidityControlled());
            assertEquals(20, zone.getHumidityMin());
            assertEquals(40, zone.getHumidityMax());
            return savedZone;
        });
        when(mapper.toDTO(savedZone)).thenReturn(dto);

        ResponseEntity<ZoneDTO> response = zoneController.createZone(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
