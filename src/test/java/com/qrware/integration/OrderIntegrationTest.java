package com.qrware.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrware.domain.order.Order;
import com.qrware.domain.order.OrderPriority;
import com.qrware.domain.order.OrderStatus;
import com.qrware.domain.order.OrderType;
import com.qrware.domain.user.Role;
import com.qrware.domain.user.User;
import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.LocationType;
import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.ZoneType;
import com.qrware.repository.order.OrderRepository;
import com.qrware.repository.user.RoleRepository;
import com.qrware.repository.user.UserRepository;
import com.qrware.repository.warehouse.LocationRepository;
import com.qrware.repository.warehouse.ZoneRepository;
import com.qrware.security.util.SecurityUtils;
import org.hamcrest.Matchers;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Location sourceLocation;
    private Location destinationLocation;
    private Role warehouseManagerRole;
    private Role warehouseWorkerRole;
    private Zone testZone;

    @BeforeEach
    void setUp() {
        warehouseManagerRole = roleRepository.findByName("WAREHOUSE_MANAGER")
                .orElseGet(() -> roleRepository.save(new Role("WAREHOUSE_MANAGER", "Warehouse Manager Role")));
        
        warehouseWorkerRole = roleRepository.findByName("WAREHOUSE_WORKER")
                .orElseGet(() -> roleRepository.save(new Role("WAREHOUSE_WORKER", "Warehouse Worker Role")));

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

        testZone = zoneRepository.findByCode("ZONE-001").orElseGet(() -> {
            Zone zone = new Zone();
            zone.setName("Test Zone");
            zone.setCode("ZONE-001");
            zone.setType(ZoneType.STORAGE);
            return zoneRepository.save(zone);
        });

        sourceLocation = locationRepository.findByCode("SRC-001").orElseGet(() -> {
            Location loc = new Location();
            loc.setName("Source Location");
            loc.setCode("SRC-001");
            loc.setZone(testZone);
            loc.setType(LocationType.SHELF);
            return locationRepository.save(loc);
        });

        destinationLocation = locationRepository.findByCode("DST-001").orElseGet(() -> {
            Location loc = new Location();
            loc.setName("Destination Location");
            loc.setCode("DST-001");
            loc.setZone(testZone);
            loc.setType(LocationType.SHELF);
            return locationRepository.save(loc);
        });
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_READ", "ORDER_WRITE"})
    void createOrder_ShouldCreateNewOrder() throws Exception {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(testUser));
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of(testUser.getUsername()));

            String requestBody = """
                {
                    "type": "INBOUND",
                    "description": "Test Inbound Order",
                    "priority": "NORMAL",
                    "sourceLocationId": %d,
                    "destinationLocationId": %d
                }
                """.formatted(sourceLocation.getId(), destinationLocation.getId());

            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("INBOUND"))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.orderNumber").exists());
        }
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ORDER_READ"})
    void getOrderById_ShouldReturnOrder() throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin Role")));
        
        User adminUser = userRepository.findByUsername("admin").orElseGet(() -> {
            User user = new User();
            user.setUsername("admin");
            user.setEmail("admin@example.com");
            user.setPassword("password123");
            user.setFirstName("Admin");
            user.setLastName("User");
            user.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
            return userRepository.save(user);
        });

        Order order = new Order();
        order.setOrderNumber("ORD-TEST-001");
        order.setType(OrderType.INBOUND);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatorUser(testUser);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(adminUser));

            mockMvc.perform(get("/api/orders/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(order.getId()))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-TEST-001"));
        }
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_READ"})
    void getOrderByNumber_ShouldReturnOrder() throws Exception {
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-002");
        order.setType(OrderType.OUTBOUND);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatorUser(testUser);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        mockMvc.perform(get("/api/orders/number/" + order.getOrderNumber()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orderNumber").value("ORD-TEST-002"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_WRITE"})
    void startOrder_ShouldUpdateStatus() throws Exception {
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-003");
        order.setType(OrderType.PICK);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatorUser(testUser);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(testUser));

            mockMvc.perform(put("/api/orders/" + order.getId() + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
        }
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_WRITE"})
    void completeOrder_ShouldUpdateStatus() throws Exception {
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-004");
        order.setType(OrderType.PICK);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setCreatorUser(testUser);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalItems(5);
        order.setCompletedItems(5);
        order = orderRepository.save(order);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(testUser));

            mockMvc.perform(put("/api/orders/" + order.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_WRITE"})
    void cancelOrder_ShouldUpdateStatus() throws Exception {
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-005");
        order.setType(OrderType.PICK);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatorUser(testUser);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        String requestBody = """
            {
                "reason": "Test cancellation"
            }
            """;

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(testUser));

            mockMvc.perform(put("/api/orders/" + order.getId() + "/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_ASSIGN"})
    void assignOrder_ShouldUpdateAssignedUser() throws Exception {
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-006");
        order.setType(OrderType.PICK);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatorUser(testUser);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        User assignee = userRepository.findByUsername("assignee").orElseGet(() -> {
            User user = new User();
            user.setUsername("assignee");
            user.setEmail("assignee@example.com");
            user.setPassword("password123");
            user.setFirstName("Assignee");
            user.setLastName("User");
            user.setRoles(new HashSet<>(Collections.singletonList(warehouseWorkerRole)));
            return userRepository.save(user);
        });

        String requestBody = """
            {
                "assignedToId": %d
            }
            """.formatted(assignee.getId());

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(testUser));

            mockMvc.perform(put("/api/orders/" + order.getId() + "/assign")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedToId").value(assignee.getId()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_READ"})
    void searchOrders_ShouldReturnMatchingOrders() throws Exception {
        Order order1 = new Order();
        order1.setOrderNumber("SEARCH-001");
        order1.setType(OrderType.INBOUND);
        order1.setStatus(OrderStatus.CREATED);
        order1.setCreatorUser(testUser);
        order1.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setOrderNumber("SEARCH-002");
        order2.setType(OrderType.OUTBOUND);
        order2.setStatus(OrderStatus.CREATED);
        order2.setCreatorUser(testUser);
        order2.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order2);

        mockMvc.perform(get("/api/orders/search")
                .param("q", "SEARCH-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].orderNumber").value("SEARCH-001"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_READ"})
    void getAllOrders_ShouldReturnPageOfOrders() throws Exception {
        for (int i = 0; i < 3; i++) {
            Order order = new Order();
            order.setOrderNumber("ALL-" + i);
            order.setType(OrderType.INBOUND);
            order.setStatus(OrderStatus.CREATED);
            order.setCreatorUser(testUser);
            order.setCreatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.totalElements").value(Matchers.greaterThanOrEqualTo(3)));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_READ"})
    void getActiveOrders_ShouldReturnActiveOrdersOnly() throws Exception {
        Order active = new Order();
        active.setOrderNumber("ACTIVE-001");
        active.setType(OrderType.INBOUND);
        active.setStatus(OrderStatus.IN_PROGRESS);
        active.setCreatorUser(testUser);
        active.setCreatedAt(LocalDateTime.now());
        orderRepository.save(active);

        Order completed = new Order();
        completed.setOrderNumber("COMPLETED-001");
        completed.setType(OrderType.INBOUND);
        completed.setStatus(OrderStatus.COMPLETED);
        completed.setCreatorUser(testUser);
        completed.setCreatedAt(LocalDateTime.now());
        completed.setTotalItems(1);
        completed.setCompletedItems(1);
        orderRepository.save(completed);

        mockMvc.perform(get("/api/orders/active"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[?(@.orderNumber == 'ACTIVE-001')]").exists())
            .andExpect(jsonPath("$.data[?(@.orderNumber == 'COMPLETED-001')]").doesNotExist());
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_READ"})
    void getMyOrders_ShouldReturnUserOrders() throws Exception {
        Order myOrder = new Order();
        myOrder.setOrderNumber("MY-001");
        myOrder.setType(OrderType.INBOUND);
        myOrder.setStatus(OrderStatus.CREATED);
        myOrder.setCreatorUser(testUser);
        myOrder.setAssignedTo(testUser);
        myOrder.setCreatedAt(LocalDateTime.now());
        orderRepository.save(myOrder);

        User otherUser = userRepository.findByUsername("other").orElseGet(() -> {
            User user = new User();
            user.setUsername("other");
            user.setEmail("other@example.com");
            user.setPassword("password123");
            user.setFirstName("Other");
            user.setLastName("User");
            user.setRoles(new HashSet<>(Collections.singletonList(warehouseWorkerRole)));
            return userRepository.save(user);
        });

        Order otherOrder = new Order();
        otherOrder.setOrderNumber("OTHER-001");
        otherOrder.setType(OrderType.INBOUND);
        otherOrder.setStatus(OrderStatus.CREATED);
        otherOrder.setCreatorUser(otherUser);
        otherOrder.setAssignedTo(otherUser);
        otherOrder.setCreatedAt(LocalDateTime.now());
        orderRepository.save(otherOrder);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(testUser));
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of(testUser.getUsername()));

            mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.orderNumber == 'MY-001')]").exists())
                .andExpect(jsonPath("$.data[?(@.orderNumber == 'OTHER-001')]").doesNotExist());
        }
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_READ"})
    void getOverdueOrders_ShouldReturnOverdueOrders() throws Exception {
        Order overdue = new Order();
        overdue.setOrderNumber("OVERDUE-001");
        overdue.setType(OrderType.INBOUND);
        overdue.setStatus(OrderStatus.IN_PROGRESS);
        overdue.setCreatorUser(testUser);
        overdue.setCreatedAt(LocalDateTime.now().minusDays(5));
        overdue.setExpectedDate(LocalDateTime.now().minusDays(1));
        orderRepository.save(overdue);

        Order future = new Order();
        future.setOrderNumber("FUTURE-001");
        future.setType(OrderType.INBOUND);
        future.setStatus(OrderStatus.IN_PROGRESS);
        future.setCreatorUser(testUser);
        future.setCreatedAt(LocalDateTime.now());
        future.setExpectedDate(LocalDateTime.now().plusDays(1));
        orderRepository.save(future);

        mockMvc.perform(get("/api/orders/overdue"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[?(@.orderNumber == 'OVERDUE-001')]").exists())
            .andExpect(jsonPath("$.data[?(@.orderNumber == 'FUTURE-001')]").doesNotExist());
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_READ"})
    void getHighPriorityOrders_ShouldReturnHighPriorityOrders() throws Exception {
        Order urgent = new Order();
        urgent.setOrderNumber("URGENT-001");
        urgent.setType(OrderType.INBOUND);
        urgent.setStatus(OrderStatus.CREATED);
        urgent.setCreatorUser(testUser);
        urgent.setCreatedAt(LocalDateTime.now());
        urgent.setPriority(OrderPriority.URGENT);
        orderRepository.save(urgent);

        Order normal = new Order();
        normal.setOrderNumber("NORMAL-001");
        normal.setType(OrderType.INBOUND);
        normal.setStatus(OrderStatus.CREATED);
        normal.setCreatorUser(testUser);
        normal.setCreatedAt(LocalDateTime.now());
        normal.setPriority(OrderPriority.NORMAL);
        orderRepository.save(normal);

        mockMvc.perform(get("/api/orders/high-priority"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[?(@.orderNumber == 'URGENT-001')]").exists())
            .andExpect(jsonPath("$.data[?(@.orderNumber == 'NORMAL-001')]").doesNotExist());
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ORDER_READ"})
    void getOrderStatistics_ShouldReturnStats() throws Exception {
        Order createdOrder = new Order();
        createdOrder.setOrderNumber("STAT-001");
        createdOrder.setType(OrderType.INBOUND);
        createdOrder.setStatus(OrderStatus.CREATED);
        createdOrder.setCreatorUser(testUser);
        createdOrder.setCreatedAt(LocalDateTime.now());
        orderRepository.save(createdOrder);

        Order completed = new Order();
        completed.setOrderNumber("STAT-002");
        completed.setType(OrderType.INBOUND);
        completed.setStatus(OrderStatus.COMPLETED);
        completed.setCreatorUser(testUser);
        completed.setCreatedAt(LocalDateTime.now());
        completed.setTotalItems(1);
        completed.setCompletedItems(1);
        orderRepository.save(completed);

        mockMvc.perform(get("/api/orders/statistics/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[?(@.status == 'CREATED')].count").exists())
            .andExpect(jsonPath("$.data[?(@.status == 'COMPLETED')].count").exists());
    }
}
