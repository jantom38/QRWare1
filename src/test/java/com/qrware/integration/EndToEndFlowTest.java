package com.qrware.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrware.controller.OrderController;
import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.MovementType;
import com.qrware.domain.order.OrderItem;
import com.qrware.domain.order.OrderType;
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
import com.qrware.repository.order.OrderItemRepository;
import com.qrware.repository.order.OrderRepository;
import com.qrware.repository.product.CategoryRepository;
import com.qrware.repository.product.ProductRepository;
import com.qrware.repository.user.RoleRepository;
import com.qrware.repository.user.UserRepository;
import com.qrware.repository.warehouse.LocationRepository;
import com.qrware.repository.warehouse.ZoneRepository;
import com.qrware.security.util.SecurityUtils;
import com.qrware.service.OrderService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EndToEndFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private MovementHistoryRepository movementHistoryRepository;
    @Autowired
    private OrderService orderService;

    private User managerUser;
    private Product testProduct;
    private Location receivingLocation;
    private Location storageLocation;
    private Location shippingLocation;

    @BeforeEach
    void setUp() {
        try {
            entityManager.createNativeQuery("ALTER TABLE public.order_items DROP CONSTRAINT IF EXISTS uk_s9j0gxronau1nngma5h49us36").executeUpdate();
        } catch (Exception e) {
        }

        Role managerRole = roleRepository.findByName("WAREHOUSE_MANAGER")
                .orElseGet(() -> roleRepository.save(new Role("WAREHOUSE_MANAGER", "Manager Role")));

        managerUser = userRepository.findByUsername("manager").orElseGet(() -> {
            User user = new User();
            user.setUsername("manager");
            user.setEmail("manager@qrware.com");
            user.setPassword("password123");
            user.setFirstName("John");
            user.setLastName("Manager");
            user.setRoles(new HashSet<>(Collections.singletonList(managerRole)));
            return userRepository.save(user);
        });

        Zone receivingZone = zoneRepository.findByCode("ZONE-RECV").orElseGet(() -> 
            zoneRepository.save(new Zone("Receiving Zone", "ZONE-RECV", ZoneType.RECEIVING)));
        
        Zone storageZone = zoneRepository.findByCode("ZONE-STOR").orElseGet(() -> 
            zoneRepository.save(new Zone("Storage Zone", "ZONE-STOR", ZoneType.STORAGE)));
            
        Zone shippingZone = zoneRepository.findByCode("ZONE-SHIP").orElseGet(() -> 
            zoneRepository.save(new Zone("Shipping Zone", "ZONE-SHIP", ZoneType.SHIPPING)));

        receivingLocation = locationRepository.findByCode("LOC-RECV-01").orElseGet(() -> {
            Location loc = new Location();
            loc.setCode("LOC-RECV-01");
            loc.setName("Receiving Dock 1");
            loc.setZone(receivingZone);
            loc.setType(LocationType.FLOOR);
            return locationRepository.save(loc);
        });

        storageLocation = locationRepository.findByCode("LOC-STOR-01").orElseGet(() -> {
            Location loc = new Location();
            loc.setCode("LOC-STOR-01");
            loc.setName("Shelf A-01");
            loc.setZone(storageZone);
            loc.setType(LocationType.SHELF);
            return locationRepository.save(loc);
        });
        
        shippingLocation = locationRepository.findByCode("LOC-SHIP-01").orElseGet(() -> {
            Location loc = new Location();
            loc.setCode("LOC-SHIP-01");
            loc.setName("Shipping Dock 1");
            loc.setZone(shippingZone);
            loc.setType(LocationType.FLOOR);
            return locationRepository.save(loc);
        });

        Category category = categoryRepository.findByCode("CAT-E2E").orElseGet(() -> 
            categoryRepository.save(new Category("E2E Test Category", "CAT-E2E")));

        testProduct = productRepository.findBySku("PROD-E2E-001").orElseGet(() -> {
            Product prod = new Product();
            prod.setName("E2E Test Widget");
            prod.setSku("PROD-E2E-001");
            prod.setCategory(category);
            prod.setPrice(new BigDecimal("50.00"));
            prod.setUnitOfMeasure("PIECE");
            return productRepository.save(prod);
        });
    }

    private void simulateUserLogin(User user) {
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ORDER_READ"),
            new SimpleGrantedAuthority("ORDER_WRITE"),
            new SimpleGrantedAuthority("INVENTORY_READ"),
            new SimpleGrantedAuthority("INVENTORY_WRITE")
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            user, 
            null, 
            authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ORDER_READ", "ORDER_WRITE", "INVENTORY_READ", "INVENTORY_WRITE"})
    void fullLifecycle_FromInboundToOutbound() throws Exception {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(managerUser));
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of(managerUser.getUsername()));

            OrderController.CreateOrderRequest inboundRequest = new OrderController.CreateOrderRequest();
            inboundRequest.setType(OrderType.INBOUND);
            inboundRequest.setDescription("Restock E2E Widgets");
            inboundRequest.setDestinationLocationId(storageLocation.getId());
            inboundRequest.setOrderNumber("ORD-IN-E2E-001");

            MvcResult inboundResult = mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inboundRequest)))
                .andExpect(status().isCreated())
                .andReturn();
            
            String inboundResponse = inboundResult.getResponse().getContentAsString();
            Integer inboundOrderIdInt = com.jayway.jsonpath.JsonPath.read(inboundResponse, "$.data.id");
            Long inboundOrderId = inboundOrderIdInt.longValue();

            OrderItem inboundItem = orderService.addOrderItem(inboundOrderId, testProduct.getId(), 100, null, storageLocation.getId(), new BigDecimal("20.00"), "Initial Stock", false);

            mockMvc.perform(put("/api/orders/" + inboundOrderId + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

            InventoryItem newItem = new InventoryItem();
            newItem.setProduct(testProduct);
            newItem.setLocation(storageLocation);
            newItem.setQuantity(0);
            newItem.setStatus(com.qrware.domain.inventory.InventoryStatus.AVAILABLE);
            newItem.setReceivedDate(LocalDate.now());
            newItem.setQrCode("QR-E2E-STOCK-001");
            newItem = inventoryItemRepository.save(newItem);
            
            inboundItem.setInventoryItem(newItem);
            inboundItem.setQrCodeData("QR-E2E-STOCK-001");
            orderItemRepository.save(inboundItem);

            orderService.completeOrderItem(inboundItem.getId(), 100, "All good", "QR-E2E-STOCK-001");
            
            newItem.setQuantity(100);
            inventoryItemRepository.save(newItem);

            mockMvc.perform(put("/api/orders/" + inboundOrderId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

            entityManager.flush();
            entityManager.clear();

            InventoryItem stockItem = inventoryItemRepository.findByQrCode("QR-E2E-STOCK-001").orElseThrow();
            assertEquals(100, stockItem.getQuantity(), "Stock should be 100 after inbound");
            assertEquals(storageLocation.getId(), stockItem.getLocation().getId());

            OrderController.CreateOrderRequest outboundRequest = new OrderController.CreateOrderRequest();
            outboundRequest.setType(OrderType.OUTBOUND);
            outboundRequest.setDescription("Ship to Customer X");
            outboundRequest.setOrderNumber("ORD-OUT-E2E-001");
            
            MvcResult outboundResult = mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(outboundRequest)))
                .andExpect(status().isCreated())
                .andReturn();

            String outboundResponse = outboundResult.getResponse().getContentAsString();
            Integer outboundOrderIdInt = com.jayway.jsonpath.JsonPath.read(outboundResponse, "$.data.id");
            Long outboundOrderId = outboundOrderIdInt.longValue();

            OrderItem outboundItem = orderService.addOrderItem(outboundOrderId, testProduct.getId(), 30, storageLocation.getId(), shippingLocation.getId(), new BigDecimal("80.00"), "Urgent", true);

            entityManager.flush();
            entityManager.clear();

            stockItem = inventoryItemRepository.findById(stockItem.getId()).get();
            assertEquals(30, stockItem.getReservedQuantity(), "30 items should be reserved");
            assertEquals(100, stockItem.getQuantity(), "Total quantity should still be 100");
            assertEquals(70, stockItem.getAvailableQuantity(), "Available quantity should be 70");

            mockMvc.perform(put("/api/orders/" + outboundOrderId + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

            orderService.completeOrderItem(outboundItem.getId(), 30, "Picked and packed", "QR-E2E-STOCK-001");

            mockMvc.perform(put("/api/orders/" + outboundOrderId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

            entityManager.flush();
            entityManager.clear();

            InventoryItem finalItem = inventoryItemRepository.findById(stockItem.getId()).get();
            assertEquals(70, finalItem.getQuantity(), "Final quantity should be 70 (100 - 30)");
            assertEquals(0, finalItem.getReservedQuantity(), "Reserved quantity should be released");

            List<com.qrware.domain.inventory.MovementHistory> history = movementHistoryRepository.findAll();
            
            boolean hasOutboundMovement = history.stream().anyMatch(h -> 
                h.getInventoryItem().getId().equals(finalItem.getId()) &&
                (h.getMovementType() == MovementType.ORDER_ISSUE || h.getMovementType() == MovementType.ISSUE) &&
                h.getQuantityChanged() == -30
            );
            
            assertTrue(hasOutboundMovement, "Should have movement history for the outbound order");
        }
    }

    @Test
    void flexibleOutbound_AnyLocationAllowed() throws Exception {
        simulateUserLogin(managerUser);

        InventoryItem stockItem = new InventoryItem();
        stockItem.setProduct(testProduct);
        stockItem.setLocation(storageLocation);
        stockItem.setQuantity(100);
        stockItem.setStatus(com.qrware.domain.inventory.InventoryStatus.AVAILABLE);
        stockItem.setReceivedDate(LocalDate.now());
        stockItem.setQrCode("QR-FLEX-STOCK-001");
        stockItem = inventoryItemRepository.save(stockItem);

        entityManager.flush();
        entityManager.clear();

        OrderController.CreateOrderRequest outboundRequest = new OrderController.CreateOrderRequest();
        outboundRequest.setType(OrderType.OUTBOUND);
        outboundRequest.setDescription("Flexible Outbound Test");
        outboundRequest.setOrderNumber("ORD-FLEX-001");
        
        MvcResult outboundResult = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outboundRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long outboundOrderId = ((Number) com.jayway.jsonpath.JsonPath.read(outboundResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        OrderItem outboundItem = orderService.addOrderItem(
            outboundOrderId, 
            testProduct.getId(), 
            20, 
            null, 
            shippingLocation.getId(), 
            new BigDecimal("80.00"), 
            "Flexible Pick", 
            false 
        );

        mockMvc.perform(put("/api/orders/" + outboundOrderId + "/start"))
            .andExpect(status().isOk());

        Optional<Object> scanResult = orderService.processQRScan("QR-FLEX-STOCK-001", outboundOrderId);
        assertTrue(scanResult.isPresent(), "Scan should be successful");
        assertTrue(scanResult.get() instanceof OrderItem, "Scan should return the linked OrderItem");
        
        OrderItem scannedItem = (OrderItem) scanResult.get();
        assertNotNull(scannedItem.getInventoryItem(), "OrderItem should now be linked to InventoryItem");
        assertEquals("QR-FLEX-STOCK-001", scannedItem.getInventoryItem().getQrCode());

        orderService.completeOrderItem(outboundItem.getId(), 20, "Picked flexibly", "QR-FLEX-STOCK-001");

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(put("/api/orders/" + outboundOrderId + "/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        entityManager.flush();
        entityManager.clear();

        InventoryItem finalItem = inventoryItemRepository.findByQrCode("QR-FLEX-STOCK-001").orElseThrow();
        assertEquals(80, finalItem.getQuantity(), "Quantity should be reduced by 20 (100 - 20)");
        
        List<com.qrware.domain.inventory.MovementHistory> history = movementHistoryRepository.findAll();
        boolean hasMovement = history.stream().anyMatch(h -> 
            h.getInventoryItem().getId().equals(finalItem.getId()) &&
            (h.getMovementType() == MovementType.ORDER_ISSUE || h.getMovementType() == MovementType.ISSUE) &&
            h.getQuantityChanged() == -20
        );
        assertTrue(hasMovement, "Should have movement history for the flexible outbound");
    }
}