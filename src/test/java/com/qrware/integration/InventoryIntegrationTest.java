package com.qrware.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrware.controller.InventoryController;
import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.InventoryStatus;
import com.qrware.domain.inventory.MovementType;
import com.qrware.domain.product.Category;
import com.qrware.domain.product.Product;
import com.qrware.domain.user.Role;
import com.qrware.domain.user.User;
import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.LocationType;
import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.ZoneType;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.inventory.MovementHistoryRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryItemRepository inventoryRepository;

    @Autowired
    private MovementHistoryRepository movementHistoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Product testProduct;
    private Location testLocation;
    private Zone testZone;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        Role warehouseManagerRole = roleRepository.findByName("WAREHOUSE_MANAGER")
                .orElseGet(() -> roleRepository.save(new Role("WAREHOUSE_MANAGER", "Warehouse Manager Role")));

        testUser = userRepository.findByUsername("testuser").orElseGet(() -> {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setPassword("password123");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setRoles(new HashSet<>(Collections.singletonList(warehouseManagerRole)));
            return userRepository.save(user);
        });

        testZone = zoneRepository.findByCode("ZONE-INV").orElseGet(() -> {
            Zone zone = new Zone();
            zone.setName("Inventory Zone");
            zone.setCode("ZONE-INV");
            zone.setType(ZoneType.STORAGE);
            return zoneRepository.save(zone);
        });

        testLocation = locationRepository.findByCode("LOC-INV-001").orElseGet(() -> {
            Location loc = new Location();
            loc.setName("Inventory Location");
            loc.setCode("LOC-INV-001");
            loc.setZone(testZone);
            loc.setType(LocationType.SHELF);
            return locationRepository.save(loc);
        });

        testCategory = categoryRepository.findByCode("CAT-INV").orElseGet(() -> {
            Category cat = new Category();
            cat.setName("Test Category");
            cat.setCode("CAT-INV");
            return categoryRepository.save(cat);
        });

        testProduct = productRepository.findBySku("PROD-INV-001").orElseGet(() -> {
            Product prod = new Product();
            prod.setName("Test Product");
            prod.setSku("PROD-INV-001");
            prod.setCategory(testCategory);
            prod.setPrice(new BigDecimal("10.00"));
            prod.setUnitOfMeasure("PIECE");
            return productRepository.save(prod);
        });
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"INVENTORY_WRITE", "INVENTORY_READ"})
    void createInventoryItem_ShouldCreateNewItem() throws Exception {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(testUser));
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of(testUser.getUsername()));

            InventoryController.CreateInventoryRequest request = new InventoryController.CreateInventoryRequest();
            request.setProductId(testProduct.getId());
            request.setLocationId(testLocation.getId());
            request.setQuantity(100);
            request.setStatus(InventoryStatus.AVAILABLE);
            request.setQrCode("QR-INV-001");
            request.setReceivedDate(LocalDate.now());

            mockMvc.perform(post("/api/inventory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.product.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.location.code").value(testLocation.getCode()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"INVENTORY_WRITE", "INVENTORY_READ"})
    void receiveStock_ShouldIncreaseQuantityAndCreateHistory() throws Exception {
        InventoryItem item = new InventoryItem();
        item.setProduct(testProduct);
        item.setLocation(testLocation);
        item.setQuantity(50);
        item.setStatus(InventoryStatus.AVAILABLE);
        item.setQrCode("QR-INV-002");
        item.setReceivedDate(LocalDate.now());
        InventoryItem savedItem = inventoryRepository.save(item);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(testUser));
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of(testUser.getUsername()));

            InventoryController.QuantityUpdateRequest request = new InventoryController.QuantityUpdateRequest();
            request.setQuantity(20);
            request.setReason("Restock");

            mockMvc.perform(post("/api/inventory/" + savedItem.getId() + "/receive")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(70));

            assertTrue(movementHistoryRepository.findAll().stream()
                .anyMatch(h -> h.getInventoryItem().getId().equals(savedItem.getId()) && 
                               h.getMovementType() == MovementType.RECEIPT && 
                               h.getQuantityChanged() == 20));
        }
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"INVENTORY_WRITE", "INVENTORY_READ"})
    void issueStock_ShouldDecreaseQuantityAndCreateHistory() throws Exception {
        InventoryItem item = new InventoryItem();
        item.setProduct(testProduct);
        item.setLocation(testLocation);
        item.setQuantity(50);
        item.setStatus(InventoryStatus.AVAILABLE);
        item.setQrCode("QR-INV-003");
        item.setReceivedDate(LocalDate.now());
        InventoryItem savedItem = inventoryRepository.save(item);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(testUser));
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of(testUser.getUsername()));

            InventoryController.QuantityUpdateRequest request = new InventoryController.QuantityUpdateRequest();
            request.setQuantity(10);
            request.setReason("Shipment");

            mockMvc.perform(post("/api/inventory/" + savedItem.getId() + "/issue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(40));

            assertTrue(movementHistoryRepository.findAll().stream()
                .anyMatch(h -> h.getInventoryItem().getId().equals(savedItem.getId()) &&
                               h.getMovementType() == MovementType.ISSUE));
        }
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"INVENTORY_READ"})
    void getInventoryByQRCode_ShouldReturnItem() throws Exception {
        InventoryItem item = new InventoryItem();
        item.setProduct(testProduct);
        item.setLocation(testLocation);
        item.setQuantity(10);
        item.setStatus(InventoryStatus.AVAILABLE);
        item.setQrCode("QR-INV-SEARCH");
        item.setReceivedDate(LocalDate.now());
        inventoryRepository.save(item);

        mockMvc.perform(get("/api/inventory/qr/QR-INV-SEARCH"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(item.getId()))
            .andExpect(jsonPath("$.qrCode").value("QR-INV-SEARCH"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"INVENTORY_READ"})
    void searchInventory_ShouldReturnMatchingItems() throws Exception {
        InventoryItem item = new InventoryItem();
        item.setProduct(testProduct);
        item.setLocation(testLocation);
        item.setQuantity(5);
        item.setStatus(InventoryStatus.AVAILABLE);
        item.setQrCode("QR-INV-QUERY");
        item.setBatchNumber("BATCH-123");
        item.setReceivedDate(LocalDate.now());
        inventoryRepository.save(item);

        mockMvc.perform(get("/api/inventory/search")
                .param("query", "PROD-INV-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].product.sku").value("PROD-INV-001"));
    }
}
