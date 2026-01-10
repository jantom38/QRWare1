package com.qrware.controller;

import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.LocationType;
import com.qrware.domain.warehouse.Zone;
import com.qrware.dto.DTOMapper;
import com.qrware.dto.LocationDTO;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.warehouse.LocationRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private DTOMapper mapper;

    @InjectMocks
    private LocationController locationController;

    // ==================== GET ALL LOCATIONS ====================

    @Test
    void getAllLocations_ShouldReturnPagedResults() {
        Location location = new Location();
        location.setId(1L);
        location.setCode("LOC-001");
        
        Page<Location> page = new PageImpl<>(List.of(location));
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        
        when(locationRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(mapper.toDTO(any(Location.class))).thenReturn(dto);

        ResponseEntity<Page<LocationDTO>> response = locationController.getAllLocations(Pageable.unpaged(), null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getAllLocations_ShouldFilterByActive_WhenActiveParamProvided() {
        Location location = new Location();
        location.setId(1L);
        location.setActive(true);
        
        Page<Location> page = new PageImpl<>(List.of(location));
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        
        when(locationRepository.findByActive(eq(true), any(Pageable.class))).thenReturn(page);
        when(mapper.toDTO(any(Location.class))).thenReturn(dto);

        ResponseEntity<Page<LocationDTO>> response = locationController.getAllLocations(Pageable.unpaged(), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(locationRepository).findByActive(eq(true), any(Pageable.class));
    }

    // ==================== GET BY ID ====================

    @Test
    void getLocationById_ShouldReturnLocation_WhenFound() {
        Long id = 1L;
        Location location = new Location();
        location.setId(id);
        location.setCode("LOC-001");
        
        LocationDTO dto = new LocationDTO();
        dto.setId(id);
        
        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(mapper.toDTO(location)).thenReturn(dto);

        ResponseEntity<LocationDTO> response = locationController.getLocationById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().getId());
    }

    @Test
    void getLocationById_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationController.getLocationById(id));
    }

    // ==================== GET BY CODE ====================

    @Test
    void getLocationByCode_ShouldReturnLocation_WhenFound() {
        String code = "LOC-001";
        Location location = new Location();
        location.setId(1L);
        location.setCode(code);
        
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        
        when(locationRepository.findByCodeIgnoreCase(code)).thenReturn(Optional.of(location));
        when(mapper.toDTO(location)).thenReturn(dto);

        ResponseEntity<LocationDTO> response = locationController.getLocationByCode(code);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getLocationByCode_ShouldThrowException_WhenNotFound() {
        String code = "INVALID";
        when(locationRepository.findByCodeIgnoreCase(code)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationController.getLocationByCode(code));
    }

    // ==================== GET BY QR CODE ====================

    @Test
    void getLocationByQrCode_ShouldReturnLocation_WhenFound() {
        String qrCode = "QR-LOC-001";
        Location location = new Location();
        location.setId(1L);
        location.setQrCode(qrCode);
        
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        
        when(locationRepository.findByQrCode(qrCode)).thenReturn(Optional.of(location));
        when(mapper.toDTO(location)).thenReturn(dto);

        ResponseEntity<LocationDTO> response = locationController.getLocationByQrCode(qrCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getLocationByQrCode_ShouldThrowException_WhenNotFound() {
        String qrCode = "INVALID-QR";
        when(locationRepository.findByQrCode(qrCode)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationController.getLocationByQrCode(qrCode));
    }

    // ==================== GET BY ZONE ====================

    @Test
    void getLocationsByZone_ShouldReturnList() {
        Long zoneId = 1L;
        Location location = new Location();
        location.setId(1L);
        
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        
        when(locationRepository.findByZoneId(zoneId)).thenReturn(List.of(location));
        when(mapper.toDTO(any(Location.class))).thenReturn(dto);

        ResponseEntity<List<LocationDTO>> response = locationController.getLocationsByZone(zoneId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ==================== SEARCH ====================

    @Test
    void searchLocations_ShouldReturnMatchingLocations() {
        String query = "warehouse";
        Location location = new Location();
        location.setId(1L);
        location.setName("Main Warehouse");
        
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        
        when(locationRepository.searchLocations(query)).thenReturn(List.of(location));
        when(mapper.toDTO(any(Location.class))).thenReturn(dto);

        ResponseEntity<List<LocationDTO>> response = locationController.searchLocations(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ==================== GET ACTIVE ====================

    @Test
    void getActiveLocations_ShouldReturnOnlyActiveLocations() {
        Location location = new Location();
        location.setId(1L);
        location.setActive(true);
        
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        
        when(locationRepository.findByActiveTrue()).thenReturn(List.of(location));
        when(mapper.toDTO(any(Location.class))).thenReturn(dto);

        ResponseEntity<List<LocationDTO>> response = locationController.getActiveLocations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ==================== CREATE LOCATION ====================

    @Test
    void createLocation_ShouldCreateAndReturn_WhenValidRequest() {
        LocationController.CreateLocationRequest request = new LocationController.CreateLocationRequest();
        request.setCode("LOC-NEW");
        request.setName("New Location");
        request.setZoneId(1L);
        request.setType(LocationType.RACK);
        
        Zone zone = new Zone();
        zone.setId(1L);
        
        Location savedLocation = new Location();
        savedLocation.setId(1L);
        savedLocation.setCode("LOC-NEW");
        
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        
        when(locationRepository.existsByCode("LOC-NEW")).thenReturn(false);
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);
        when(mapper.toDTO(savedLocation)).thenReturn(dto);

        ResponseEntity<LocationDTO> response = locationController.createLocation(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void createLocation_ShouldReturnConflict_WhenCodeExists() {
        LocationController.CreateLocationRequest request = new LocationController.CreateLocationRequest();
        request.setCode("EXISTING-CODE");
        request.setName("New Location");
        request.setZoneId(1L);
        request.setType(LocationType.RACK);
        
        when(locationRepository.existsByCode("EXISTING-CODE")).thenReturn(true);

        ResponseEntity<LocationDTO> response = locationController.createLocation(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void createLocation_ShouldReturnConflict_WhenQrCodeExists() {
        LocationController.CreateLocationRequest request = new LocationController.CreateLocationRequest();
        request.setCode("LOC-NEW");
        request.setName("New Location");
        request.setZoneId(1L);
        request.setType(LocationType.RACK);
        request.setQrCode("EXISTING-QR");
        
        when(locationRepository.existsByCode("LOC-NEW")).thenReturn(false);
        when(locationRepository.existsByQrCode("EXISTING-QR")).thenReturn(true);

        ResponseEntity<LocationDTO> response = locationController.createLocation(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void createLocation_ShouldThrowException_WhenZoneNotFound() {
        LocationController.CreateLocationRequest request = new LocationController.CreateLocationRequest();
        request.setCode("LOC-NEW");
        request.setName("New Location");
        request.setZoneId(999L);
        request.setType(LocationType.RACK);
        
        when(locationRepository.existsByCode("LOC-NEW")).thenReturn(false);
        when(zoneRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationController.createLocation(request));
    }

    // ==================== UPDATE LOCATION ====================

    @Test
    void updateLocation_ShouldUpdateAndReturn_WhenFound() {
        Long id = 1L;
        LocationController.UpdateLocationRequest request = new LocationController.UpdateLocationRequest();
        request.setName("Updated Name");
        request.setActive(false);
        
        Location existingLocation = new Location();
        existingLocation.setId(id);
        existingLocation.setName("Old Name");
        existingLocation.setActive(true);
        
        LocationDTO dto = new LocationDTO();
        dto.setId(id);
        
        when(locationRepository.findById(id)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(existingLocation);
        when(mapper.toDTO(any(Location.class))).thenReturn(dto);

        ResponseEntity<LocationDTO> response = locationController.updateLocation(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", existingLocation.getName());
        assertFalse(existingLocation.getActive());
    }

    @Test
    void updateLocation_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        LocationController.UpdateLocationRequest request = new LocationController.UpdateLocationRequest();
        
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationController.updateLocation(id, request));
    }

    @Test
    void updateLocation_ShouldReturnConflict_WhenQrCodeAlreadyUsed() {
        Long id = 1L;
        LocationController.UpdateLocationRequest request = new LocationController.UpdateLocationRequest();
        request.setQrCode("USED-QR");
        
        Location existingLocation = new Location();
        existingLocation.setId(id);
        existingLocation.setQrCode("OLD-QR");
        
        when(locationRepository.findById(id)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.existsByQrCodeAndIdNot("USED-QR", id)).thenReturn(true);

        ResponseEntity<LocationDTO> response = locationController.updateLocation(id, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // ==================== DELETE LOCATION ====================

    @Test
    void deleteLocation_ShouldDeactivate_WhenEmptyLocation() {
        Long id = 1L;
        Location location = new Location();
        location.setId(id);
        location.setActive(true);
        location.setInventoryItems(new ArrayList<>());
        
        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenReturn(location);

        ResponseEntity<Void> response = locationController.deleteLocation(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(location.getActive());
    }

    @Test
    void deleteLocation_ShouldReturnConflict_WhenLocationHasInventory() {
        Long id = 1L;
        Location location = new Location();
        location.setId(id);
        location.setInventoryItems(List.of(new com.qrware.domain.inventory.InventoryItem()));
        
        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        ResponseEntity<Void> response = locationController.deleteLocation(id);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void deleteLocation_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationController.deleteLocation(id));
    }

    // ==================== TOGGLE ACTIVE ====================

    @Test
    void toggleLocationActive_ShouldToggleStatus() {
        Long id = 1L;
        Location location = new Location();
        location.setId(id);
        location.setActive(true);
        
        LocationDTO dto = new LocationDTO();
        dto.setId(id);
        
        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenReturn(location);
        when(mapper.toDTO(any(Location.class))).thenReturn(dto);

        ResponseEntity<LocationDTO> response = locationController.toggleLocationActive(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(location.getActive());
    }

    @Test
    void toggleLocationActive_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationController.toggleLocationActive(id));
    }
}
