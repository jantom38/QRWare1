package com.qrware.config;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.InventoryStatus;
import com.qrware.domain.order.*;
import com.qrware.domain.product.Category;
import com.qrware.domain.product.Product;
import com.qrware.domain.user.Permission;
import com.qrware.domain.user.Role;
import com.qrware.domain.user.User;
import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.ZoneType;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.order.OrderItemRepository;
import com.qrware.repository.order.OrderRepository;
import com.qrware.repository.product.CategoryRepository;
import com.qrware.repository.product.ProductRepository;
import com.qrware.repository.user.PermissionRepository;
import com.qrware.repository.user.RoleRepository;
import com.qrware.repository.user.UserRepository;
import com.qrware.repository.warehouse.LocationRepository;
import com.qrware.repository.warehouse.ZoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private InventoryItemRepository inventoryItemRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private ZoneRepository zoneRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Initializing default data...");

        createPermissions();
        createRoles();
        createUsers();
        createSampleData();
        createSampleOrders();
        createTestOrderForAdmin();
        
        logger.info("Default data initialization completed");
    }

    private void createPermissions() {
        createPermissionIfNotExists("USER_READ", "Read user information", "USER", "READ");
        createPermissionIfNotExists("USER_UPDATE", "Update user profile", "USER", "UPDATE");
        createPermissionIfNotExists("PRODUCT_READ", "Read product information", "PRODUCT", "READ");
        createPermissionIfNotExists("PRODUCT_WRITE", "Create new products", "PRODUCT", "CREATE");
        createPermissionIfNotExists("PRODUCT_UPDATE", "Update product information", "PRODUCT", "UPDATE");
        createPermissionIfNotExists("PRODUCT_DELETE", "Delete products", "PRODUCT", "DELETE");
        createPermissionIfNotExists("INVENTORY_READ", "Read inventory information", "INVENTORY", "READ");
        createPermissionIfNotExists("INVENTORY_WRITE", "Create inventory items", "INVENTORY", "CREATE");
        createPermissionIfNotExists("INVENTORY_UPDATE", "Update inventory", "INVENTORY", "UPDATE");
        createPermissionIfNotExists("INVENTORY_DELETE", "Delete inventory items", "INVENTORY", "DELETE");
        createPermissionIfNotExists("LOCATION_READ", "Read Locations", "LOCATION","READ");
        createPermissionIfNotExists("LOCATION_WRITE", "Write Locations", "LOCATION","WRITE");
        createPermissionIfNotExists("ZONE_READ", "Write Locations", "ZONE","READ");
        createPermissionIfNotExists("ZONE_WRITE", "Write Locations", "ZONE","WRITE");
        createPermissionIfNotExists("QR_SCAN", "Scan QR codes", "QR", "SCAN");
        createPermissionIfNotExists("QR_GENERATE", "Generate QR codes", "QR", "GENERATE");
        createPermissionIfNotExists("ORDER_READ", "Read order information", "ORDER", "READ");
        createPermissionIfNotExists("ORDER_WRITE", "Create and update orders", "ORDER", "WRITE");
        createPermissionIfNotExists("ORDER_ASSIGN", "Assign orders to users", "ORDER", "ASSIGN");
        createPermissionIfNotExists("ORDER_DELETE", "Delete orders", "ORDER", "DELETE");
        createPermissionIfNotExists("MOVEMENT_READ", "Read movement history", "MOVEMENT", "READ");
        createPermissionIfNotExists("MOVEMENT_WRITE", "Create movement entries", "MOVEMENT", "WRITE");
        createPermissionIfNotExists("ADMIN_FULL", "Full administrative access", "ADMIN", "ALL");
    }

    private void createRoles() {
        if (!roleRepository.existsByName("USER")) {
            Role userRole = new Role("USER", "Basic user with read access");
            userRole.setPermissions(new HashSet<>(List.of(
                getPermission("USER_READ"), getPermission("USER_UPDATE"),
                getPermission("PRODUCT_READ"), getPermission("INVENTORY_READ")
            )));
            roleRepository.save(userRole);
        }
        if (!roleRepository.existsByName("WAREHOUSE_WORKER")) {
            Role workerRole = new Role("WAREHOUSE_WORKER", "Warehouse worker with operational access");
            workerRole.setPermissions(new HashSet<>(List.of(
                getPermission("USER_READ"), getPermission("USER_UPDATE"),
                getPermission("PRODUCT_READ"), getPermission("INVENTORY_READ"),
                getPermission("INVENTORY_WRITE"), getPermission("INVENTORY_UPDATE"),
                getPermission("QR_SCAN"), getPermission("ORDER_READ"),
                getPermission("ORDER_WRITE"), getPermission("MOVEMENT_READ")
            )));
            roleRepository.save(workerRole);
        }
        if (!roleRepository.existsByName("WAREHOUSE_MANAGER")) {
            Role managerRole = new Role("WAREHOUSE_MANAGER", "Warehouse manager with full warehouse access");
            managerRole.setPermissions(new HashSet<>(List.of(
                getPermission("USER_READ"), getPermission("USER_UPDATE"),
                getPermission("PRODUCT_READ"), getPermission("PRODUCT_WRITE"),
                getPermission("PRODUCT_UPDATE"), getPermission("INVENTORY_READ"),
                getPermission("INVENTORY_WRITE"), getPermission("INVENTORY_UPDATE"),
                getPermission("INVENTORY_DELETE"), getPermission("QR_SCAN"),
                getPermission("QR_GENERATE"), getPermission("ORDER_READ"),
                getPermission("ORDER_WRITE"), getPermission("ORDER_ASSIGN"),
                getPermission("ORDER_DELETE"), getPermission("MOVEMENT_READ"),
                getPermission("MOVEMENT_WRITE")
            )));
            roleRepository.save(managerRole);
        }
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role("ADMIN", "System administrator with full access");
            adminRole.setPermissions(new HashSet<>(permissionRepository.findAll()));
            roleRepository.save(adminRole);
        }
    }

    private void createUsers() {
        String defaultPassword = passwordEncoder.encode("password");
        createUserIfNotExists("admin", "admin@qrware.com", "Admin", "User", getRole("ADMIN"), defaultPassword);
        createUserIfNotExists("manager", "manager@qrware.com", "Manager", "Test", getRole("WAREHOUSE_MANAGER"), defaultPassword);
        createUserIfNotExists("worker", "worker@qrware.com", "Worker", "Test", getRole("WAREHOUSE_WORKER"), defaultPassword);
        createUserIfNotExists("user", "user@qrware.com", "Basic", "User", getRole("USER"), defaultPassword);
    }

    private void createUserIfNotExists(String username, String email, String firstName, String lastName, Role role, String encodedPassword) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setEmail(email);
            user.setEmailVerified(true);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRoles(Collections.singleton(role));
            user.setActive(true);
            userRepository.save(user);
            logger.info("Created {} user (pass: password)", username.toUpperCase());
        }
    }

    private void createPermissionIfNotExists(String name, String description, String resource, String action) {
        if (!permissionRepository.existsByName(name)) {
            permissionRepository.save(new Permission(name, description, resource, action));
        }
    }

    private Permission getPermission(String name) {
        return permissionRepository.findByName(name).orElseThrow(() -> new RuntimeException("Permission not found: " + name));
    }

    private Role getRole(String name) {
        return roleRepository.findByName(name).orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }

    private void createSampleData() {
        if (productRepository.count() > 0) return;
        logger.info("Creating extensive sample data...");
        Category electronics = createCategoryIfNotExists("ELEC", "Elektronika", "Urządzenia elektroniczne");
        Zone[] zones = createExtensiveZones();
        Location[] locations = createExtensiveLocations(zones);
        Product[] products = createExtensiveProducts(new Category[]{electronics});
        createExtensiveInventory(products, locations);
    }

    private Zone[] createExtensiveZones() {
        return new Zone[]{
            createZoneIfNotExists("A-MAIN", "Główna strefa A", "Elektronika", ZoneType.STORAGE),
            createZoneIfNotExists("B-BULK", "Strefa B - Bulk", "Duże gabaryty", ZoneType.STORAGE),
            createZoneIfNotExists("R-NORTH", "Przyjęcie Północ", "Główny dock", ZoneType.RECEIVING),
            createZoneIfNotExists("S-EAST", "Wysyłka Wschód", "Główna wysyłka", ZoneType.SHIPPING)
        };
    }

    private Location[] createExtensiveLocations(Zone[] zones) {
        java.util.List<Location> locationsList = new java.util.ArrayList<>();
        for (Zone zone : zones) {
            for (int i = 1; i <= 5; i++) {
                String code = zone.getCode() + "-" + String.format("%02d", i);
                locationsList.add(createLocationIfNotExists(code, zone.getName() + " - " + i, zone));
            }
        }
        return locationsList.toArray(new Location[0]);
    }

    private Product[] createExtensiveProducts(Category[] categories) {
        return new Product[]{
            createProductIfNotExists("LAP001", "Laptop Dell XPS 13", "Ultrabook", new BigDecimal("4999.99"), "SZT", categories[0], new BigDecimal("1.2")),
            createProductIfNotExists("PHN001", "iPhone 14 Pro", "Smartfon", new BigDecimal("5999.99"), "SZT", categories[0], new BigDecimal("0.2"))
        };
    }

    private void createExtensiveInventory(Product[] products, Location[] locations) {
        java.util.Random random = new java.util.Random();
        for (Product product : products) {
            for (int i = 0; i < 2; i++) {
                Location location = locations[random.nextInt(locations.length)];
                int quantity = 10 + random.nextInt(91);
                String qrCode = product.getSku() + "-" + (i + 1);
                createInventoryIfNotExists(product, location, quantity, InventoryStatus.AVAILABLE, qrCode);
            }
        }
    }

    private Category createCategoryIfNotExists(String code, String name, String description) {
        return categoryRepository.findByCode(code).orElseGet(() -> {
            Category category = new Category();
            category.setCode(code);
            category.setName(name);
            category.setDescription(description);
            category.setActive(true);
            return categoryRepository.save(category);
        });
    }

    private Zone createZoneIfNotExists(String code, String name, String description, ZoneType type) {
        return zoneRepository.findByCode(code).orElseGet(() -> {
            Zone zone = new Zone();
            zone.setCode(code);
            zone.setName(name);
            zone.setDescription(description);
            zone.setType(type);
            return zoneRepository.save(zone);
        });
    }

    private Location createLocationIfNotExists(String code, String name, Zone zone) {
        return locationRepository.findByCode(code).orElseGet(() -> {
            Location location = new Location();
            location.setCode(code);
            location.setName(name);
            location.setZone(zone);
            return locationRepository.save(location);
        });
    }

    private Product createProductIfNotExists(String sku, String name, String description, BigDecimal price, String unit, Category category, BigDecimal weight) {
        return productRepository.findBySku(sku).orElseGet(() -> {
            Product product = new Product();
            product.setSku(sku);
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setUnitOfMeasure(unit);
            product.setCategory(category);
            product.setWeight(weight);
            product.setActive(true);
            product.setMinimumStock(5);
            return productRepository.save(product);
        });
    }

    private InventoryItem createInventoryIfNotExists(Product product, Location location, int quantity, InventoryStatus status, String qrCode) {
        return inventoryItemRepository.findByQrCode(qrCode).orElseGet(() -> {
            InventoryItem item = new InventoryItem();
            item.setProduct(product);
            item.setLocation(location);
            item.setQuantity(quantity);
            item.setReservedQuantity(0);
            item.setAvailableQuantity(quantity);
            item.setStatus(status);
            item.setQrCode(qrCode);
            item.setReceivedDate(LocalDate.now());
            return inventoryItemRepository.save(item);
        });
    }

    private void createSampleOrders() {
        if (orderRepository.count() > 0) return;
        try {
            User admin = userRepository.findByUsername("admin").orElseThrow();
            Product product1 = productRepository.findBySku("PHN001").orElseThrow();
            Location location1 = locationRepository.findAll().get(0);
            Order order1 = new Order("ORD-001", OrderType.INBOUND, admin);
            orderRepository.save(order1);
            orderItemRepository.save(new OrderItem(order1, 1, product1, 50));
        } catch (Exception e) {
            logger.error("❌ Error creating sample orders: {}", e.getMessage(), e);
        }
    }

    private void createTestOrderForAdmin() {
        if (orderRepository.existsByOrderNumber("TEST-ADMIN-ORDER")) {
            logger.info("📦 Test order for admin already exists, skipping.");
            return;
        }

        logger.info("🚀 Creating a special test order for the admin user...");

        try {
            User admin = userRepository.findByUsername("admin").orElseThrow(() -> new RuntimeException("Admin user not found."));
            Category testCategory = createCategoryIfNotExists("TEST", "Test Category", "For testing");
            Product testProduct = createProductIfNotExists("TEST-PROD-001", "Test Product", "For testing", new BigDecimal("99.99"), "SZT", testCategory, new BigDecimal("0.5"));
            Zone testZone = createZoneIfNotExists("TEST-ZONE", "Test Zone", "For testing", ZoneType.STORAGE);
            Location testLocation = createLocationIfNotExists("TEST-LOC-01", "Test Location 1", testZone);

            InventoryItem testInventoryItem = new InventoryItem();
            testInventoryItem.setProduct(testProduct);
            testInventoryItem.setLocation(testLocation);
            testInventoryItem.setQuantity(100);
            testInventoryItem.setAvailableQuantity(100);
            testInventoryItem.setReservedQuantity(0);
            testInventoryItem.setStatus(InventoryStatus.AVAILABLE);
            testInventoryItem.setReceivedDate(LocalDate.now());
            testInventoryItem.setQrCode("TO_BE_GENERATED");
            
            inventoryItemRepository.save(testInventoryItem);
            
            Order testOrder = new Order("TEST-ADMIN-ORDER", OrderType.PICK, admin);
            testOrder.setDescription("Testowe zamówienie dla admina do skanowania QR");
            testOrder.setAssignedTo(admin);
            testOrder.setSourceLocation(testLocation);
            testOrder.setPriority(OrderPriority.HIGH);
            orderRepository.save(testOrder);

            OrderItem testOrderItem = new OrderItem(testOrder, 1, testProduct, 5);
            testOrderItem.setRequiresExactInventory(true);
            orderItemRepository.save(testOrderItem);

            testOrder.updateProgress();
            orderRepository.save(testOrder);

            logger.info("✅ Successfully created test order 'TEST-ADMIN-ORDER' for user 'admin'.");
            logger.info("   - MANUAL ACTION REQUIRED: Generate a QR code for InventoryItem with ID: {}", testInventoryItem.getId());

        } catch (Exception e) {
            logger.error("❌ Failed to create the special test order for admin: {}", e.getMessage(), e);
        }
    }
}