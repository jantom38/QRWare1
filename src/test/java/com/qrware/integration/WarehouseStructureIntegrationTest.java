package com.qrware.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrware.domain.product.Category;
import com.qrware.domain.product.Product;
import com.qrware.domain.user.Role;
import com.qrware.domain.user.User;
import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.LocationType;
import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.ZoneType;
import com.qrware.repository.product.CategoryRepository;
import com.qrware.repository.product.ProductRepository;
import com.qrware.repository.user.RoleRepository;
import com.qrware.repository.user.UserRepository;
import com.qrware.repository.warehouse.LocationRepository;
import com.qrware.repository.warehouse.ZoneRepository;
import com.qrware.security.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WarehouseStructureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User manager;

    @BeforeEach
    void setUp() {
        Role managerRole = roleRepository.findByName("WAREHOUSE_MANAGER")
            .orElseGet(() -> roleRepository.save(new Role("WAREHOUSE_MANAGER", "Warehouse Manager Role")));

        manager = userRepository.findByUsername("warehouse_mgr").orElseGet(() -> {
            User u = new User();
            u.setUsername("warehouse_mgr");
            u.setEmail("warehouse_mgr@qrware.com");
            u.setPassword("password123");
            u.setFirstName("Warehouse");
            u.setLastName("Manager");
            u.setRoles(new HashSet<>(Collections.singletonList(managerRole)));
            return userRepository.save(u);
        });
    }

    @Test
    @WithMockUser(username = "warehouse_mgr", authorities = {"ZONE_WRITE", "ZONE_READ", "LOCATION_WRITE", "LOCATION_READ"})
    void createZone_thenCreateLocation_ShouldSucceed() throws Exception {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(manager));
            security.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of(manager.getUsername()));

            // Use a shorter code to satisfy validation (max 20 chars)
            String zoneCode = "Z-INT-" + (System.currentTimeMillis() % 100000);
            String zoneBody = """
                {
                  "name": "Integration Zone",
                  "code": "%s",
                  "type": "STORAGE"
                }
                """.formatted(zoneCode);

            var zoneResult = mockMvc.perform(post("/api/zones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(zoneBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(zoneCode))
                .andReturn();

            // Create location under created zone
            Zone createdZone = zoneRepository.findByCode(zoneCode).orElseThrow();

            String locCode = "L-INT-" + (System.currentTimeMillis() % 100000);
            String locBody = """
                {
                  "code": "%s",
                  "name": "Integration Location",
                  "zoneId": %d,
                  "type": "SHELF",
                  "capacity": 100
                }
                """.formatted(locCode, createdZone.getId());

            mockMvc.perform(post("/api/locations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(locBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(locCode))
                .andExpect(jsonPath("$.zoneId").value(createdZone.getId().intValue()));
        }
    }

    @Test
    @WithMockUser(username = "warehouse_mgr", authorities = {"ZONE_DELETE", "ZONE_READ", "LOCATION_WRITE", "LOCATION_READ"})
    void deleteZone_ShouldReturnConflict_WhenZoneHasLocations() throws Exception {
        Zone zone = new Zone();
        zone.setName("Zone With Loc");
        String zoneCode = "ZW" + (System.currentTimeMillis() % 1_000_000_000L);
        zone.setCode(zoneCode);
        zone.setType(ZoneType.STORAGE);
        zone = zoneRepository.save(zone);

        Location loc = new Location();
        String locCode = "LZ" + (System.currentTimeMillis() % 1_000_000_000L);
        loc.setCode(locCode);
        loc.setName("Loc in Zone");
        loc.setZone(zone);
        loc.setType(LocationType.SHELF);
        loc = locationRepository.save(loc);

        zone.getLocations().add(loc);
        zoneRepository.save(zone);

        mockMvc.perform(delete("/api/zones/{id}", zone.getId()))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "warehouse_mgr", authorities = {"ZONE_WRITE", "ZONE_READ", "LOCATION_DELETE", "LOCATION_READ"})
    void deleteLocation_ShouldDeactivate_WhenNoInventory() throws Exception {
        Zone zone = new Zone();
        zone.setName("Zone");
        String zoneCode = "ZD" + (System.currentTimeMillis() % 1_000_000_000L);
        zone.setCode(zoneCode);
        zone.setType(ZoneType.STORAGE);
        zone = zoneRepository.save(zone);

        Location loc = new Location();
        String locCode = "LD" + (System.currentTimeMillis() % 1_000_000_000L);
        loc.setCode(locCode);
        loc.setName("Loc");
        loc.setZone(zone);
        loc.setType(LocationType.SHELF);
        loc = locationRepository.save(loc);

        mockMvc.perform(delete("/api/locations/{id}", loc.getId()))
            .andExpect(status().isNoContent());

        Location updated = locationRepository.findById(loc.getId()).orElseThrow();
        assertFalse(updated.getActive() != null && updated.getActive());
    }

    @Test
    void zones_ShouldReturn401_WhenNoAuth() throws Exception {
        mockMvc.perform(get("/api/zones"))
            .andExpect(status().isUnauthorized());
    }
}
