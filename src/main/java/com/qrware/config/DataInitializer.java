package com.qrware.config;

import com.qrware.domain.user.Permission;
import com.qrware.domain.user.Role;
import com.qrware.repository.user.PermissionRepository;
import com.qrware.repository.user.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import com.qrware.domain.user.User;
import com.qrware.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Collections;

import com.qrware.domain.product.Product;
import com.qrware.domain.product.Category;
import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.warehouse.ZoneType;
import com.qrware.domain.inventory.InventoryStatus;
import com.qrware.repository.product.ProductRepository;
import com.qrware.repository.product.CategoryRepository;
import com.qrware.repository.inventory.InventoryItemRepository;
import com.qrware.repository.warehouse.LocationRepository;
import com.qrware.repository.warehouse.ZoneRepository;
import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Data initializer that creates default roles and permissions on application startup
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Initializing default data...");

        createPermissions();
        createRoles();
        createUsers();
        createSampleData();

        logger.info("Default data initialization completed");
    }

    private void createPermissions() {
        logger.info("Creating default permissions...");

        // User permissions
        createPermissionIfNotExists("USER_READ", "Read user information", "USER", "READ");
        createPermissionIfNotExists("USER_UPDATE", "Update user profile", "USER", "UPDATE");

        // Product permissions
        createPermissionIfNotExists("PRODUCT_READ", "Read product information", "PRODUCT", "READ");
        createPermissionIfNotExists("PRODUCT_WRITE", "Create new products", "PRODUCT", "CREATE");
        createPermissionIfNotExists("PRODUCT_UPDATE", "Update product information", "PRODUCT", "UPDATE");
        createPermissionIfNotExists("PRODUCT_DELETE", "Delete products", "PRODUCT", "DELETE");

        // Inventory permissions
        createPermissionIfNotExists("INVENTORY_READ", "Read inventory information", "INVENTORY", "READ");
        createPermissionIfNotExists("INVENTORY_WRITE", "Create inventory items", "INVENTORY", "CREATE");
        createPermissionIfNotExists("INVENTORY_UPDATE", "Update inventory", "INVENTORY", "UPDATE");
        createPermissionIfNotExists("INVENTORY_DELETE", "Delete inventory items", "INVENTORY", "DELETE");
        createPermissionIfNotExists("LOCATION_READ", "Read Locations", "LOCATION","READ");
        createPermissionIfNotExists("LOCATION_WRITE", "Write Locations", "LOCATION","WRITE");
        createPermissionIfNotExists("ZONE_READ", "Write Locations", "ZONE","READ");
        createPermissionIfNotExists("ZONE_WRITE", "Write Locations", "ZONE","WRITE");


        // QR Code permissions
        createPermissionIfNotExists("QR_SCAN", "Scan QR codes", "QR", "SCAN");
        createPermissionIfNotExists("QR_GENERATE", "Generate QR codes", "QR", "GENERATE");

        // Admin permissions
        createPermissionIfNotExists("ADMIN_FULL", "Full administrative access", "ADMIN", "ALL");
    }

    private void createRoles() {
        logger.info("Creating default roles...");

        // Create USER role
        if (!roleRepository.existsByName("USER")) {
            Role userRole = new Role("USER", "Basic user with read access");
            Set<Permission> userPermissions = new HashSet<>();
            userPermissions.add(getPermission("USER_READ"));
            userPermissions.add(getPermission("USER_UPDATE"));
            userPermissions.add(getPermission("PRODUCT_READ"));
            userPermissions.add(getPermission("INVENTORY_READ"));
            userRole.setPermissions(userPermissions);
            roleRepository.save(userRole);
            logger.info("Created USER role");
        }

        // Create WAREHOUSE_WORKER role
        if (!roleRepository.existsByName("WAREHOUSE_WORKER")) {
            Role workerRole = new Role("WAREHOUSE_WORKER", "Warehouse worker with operational access");
            Set<Permission> workerPermissions = new HashSet<>();
            workerPermissions.add(getPermission("USER_READ"));
            workerPermissions.add(getPermission("USER_UPDATE"));
            workerPermissions.add(getPermission("PRODUCT_READ"));
            workerPermissions.add(getPermission("INVENTORY_READ"));
            workerPermissions.add(getPermission("INVENTORY_WRITE"));
            workerPermissions.add(getPermission("INVENTORY_UPDATE"));
            workerPermissions.add(getPermission("QR_SCAN"));
            workerRole.setPermissions(workerPermissions);
            roleRepository.save(workerRole);
            logger.info("Created WAREHOUSE_WORKER role");
        }

        // Create WAREHOUSE_MANAGER role
        if (!roleRepository.existsByName("WAREHOUSE_MANAGER")) {
            Role managerRole = new Role("WAREHOUSE_MANAGER", "Warehouse manager with full warehouse access");
            Set<Permission> managerPermissions = new HashSet<>();
            managerPermissions.add(getPermission("USER_READ"));
            managerPermissions.add(getPermission("USER_UPDATE"));
            managerPermissions.add(getPermission("PRODUCT_READ"));
            managerPermissions.add(getPermission("PRODUCT_WRITE"));
            managerPermissions.add(getPermission("PRODUCT_UPDATE"));
            managerPermissions.add(getPermission("INVENTORY_READ"));
            managerPermissions.add(getPermission("INVENTORY_WRITE"));
            managerPermissions.add(getPermission("INVENTORY_UPDATE"));
            managerPermissions.add(getPermission("INVENTORY_DELETE"));
            managerPermissions.add(getPermission("QR_SCAN"));
            managerPermissions.add(getPermission("QR_GENERATE"));
            managerRole.setPermissions(managerPermissions);
            roleRepository.save(managerRole);
            logger.info("Created WAREHOUSE_MANAGER role");
        }

        // Create ADMIN role
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role("ADMIN", "System administrator with full access");
            Set<Permission> adminPermissions = new HashSet<>();
            adminPermissions.add(getPermission("ADMIN_FULL"));
            adminPermissions.add(getPermission("USER_READ"));
            adminPermissions.add(getPermission("USER_UPDATE"));
            adminPermissions.add(getPermission("PRODUCT_READ"));
            adminPermissions.add(getPermission("PRODUCT_WRITE"));
            adminPermissions.add(getPermission("PRODUCT_UPDATE"));
            adminPermissions.add(getPermission("PRODUCT_DELETE"));
            adminPermissions.add(getPermission("INVENTORY_READ"));
            adminPermissions.add(getPermission("INVENTORY_WRITE"));
            adminPermissions.add(getPermission("INVENTORY_UPDATE"));
            adminPermissions.add(getPermission("INVENTORY_DELETE"));
            adminPermissions.add(getPermission("LOCATION_READ"));
            adminPermissions.add(getPermission("LOCATION_WRITE"));
            adminPermissions.add(getPermission("ZONE_READ"));
            adminPermissions.add(getPermission("ZONE_WRITE"));


            adminPermissions.add(getPermission("INVENTORY_DELETE"));
            adminPermissions.add(getPermission("QR_SCAN"));
            adminPermissions.add(getPermission("QR_GENERATE"));
            adminRole.setPermissions(adminPermissions);
            roleRepository.save(adminRole);
            logger.info("Created ADMIN role");
        }
    }

    // === POCZĄTEK ZMIAN ===
    // Metoda createUsers z poprawkami
    private void createUsers() {
        logger.info("Creating default users...");

        // Hasło jest zakodowane. Użyj tego samego hasła "password" dla wszystkich dla prostoty.
        String defaultPassword = passwordEncoder.encode("password");

        // Create Admin User
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(defaultPassword);
            adminUser.setEmail("admin@qrware.com");
            adminUser.setEmailVerified(true);
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRoles(Collections.singleton(getRole("ADMIN")));
            adminUser.setActive(true); // POPRAWKA: Użytkownik musi być włączony
            userRepository.save(adminUser);
            logger.info("Created ADMIN user (pass: password)");
        }

        // Create Manager User
        if (!userRepository.existsByUsername("manager")) {
            User managerUser = new User();
            managerUser.setUsername("manager");
            managerUser.setPassword(defaultPassword);
            managerUser.setEmail("manager@qrware.com");
            managerUser.setEmailVerified(true);

            managerUser.setFirstName("Manager");
            managerUser.setLastName("Test");
            managerUser.setRoles(Collections.singleton(getRole("WAREHOUSE_MANAGER")));
            managerUser.setActive(true); // POPRAWKA: Użytkownik musi być włączony
            userRepository.save(managerUser);
            logger.info("Created WAREHOUSE_MANAGER user (pass: password)");
        }

        // Create Worker User
        if (!userRepository.existsByUsername("worker")) {
            User workerUser = new User();
            workerUser.setUsername("worker");
            workerUser.setPassword(defaultPassword);
            workerUser.setEmail("worker@qrware.com");
            workerUser.setFirstName("Worker");
            workerUser.setEmailVerified(true);

            workerUser.setLastName("Test");
            workerUser.setRoles(Collections.singleton(getRole("WAREHOUSE_WORKER")));
            workerUser.setActive(true); // POPRAWKA: Użytkownik musi być włączony
            userRepository.save(workerUser);
            logger.info("Created WAREHOUSE_WORKER user (pass: password)");
        }

        // Create Basic User
        if (!userRepository.existsByUsername("user")) {
            User basicUser = new User();
            basicUser.setUsername("user");
            basicUser.setPassword(defaultPassword);
            basicUser.setEmail("user@qrware.com");
            basicUser.setFirstName("Basic");
            basicUser.setEmailVerified(true);

            basicUser.setLastName("User");
            basicUser.setRoles(Collections.singleton(getRole("USER")));
            basicUser.setActive(true); // POPRAWKA: Użytkownik musi być włączony
            userRepository.save(basicUser);
            logger.info("Created USER user (pass: password)");
        }
    }

    private void createPermissionIfNotExists(String name, String description, String resource, String action) {
        if (!permissionRepository.existsByName(name)) {
            Permission permission = new Permission(name, description, resource, action);
            permissionRepository.save(permission);
            logger.debug("Created permission: {}", name);
        }
    }

    private Permission getPermission(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + name));
    }

    private Role getRole(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }

    private void createSampleData() {
        logger.info("Creating sample products and inventory...");

        // Tworzenie przykładowych kategorii
        Category electronics = createCategoryIfNotExists("ELEC", "Elektronika", "Urządzenia elektroniczne");
        Category furniture = createCategoryIfNotExists("FURN", "Meble", "Meble biurowe i domowe");

        // Tworzenie stref i lokalizacji
        Zone storageZone = createZoneIfNotExists("A", "Strefa A", "Główna strefa magazynowa", ZoneType.STORAGE);
        Location loc1 = createLocationIfNotExists("A-01-01", "Regał A-01, Poziom 01", storageZone);
        Location loc2 = createLocationIfNotExists("A-01-02", "Regał A-01, Poziom 02", storageZone);

        // Tworzenie przykładowych produktów i inventory
        Product laptop = createProductIfNotExists("LAP001", "Laptop Dell XPS 13", "Ultrabook 13.3 cali", 
            new BigDecimal("4999.99"), "SZTUKA", electronics, new BigDecimal("1.2"));
        createInventoryIfNotExists(laptop, loc1, 15, InventoryStatus.AVAILABLE, "LAP001-001");

        Product chair = createProductIfNotExists("CHR001", "Krzesło biurowe", "Ergonomiczne krzesło", 
            new BigDecimal("899.99"), "SZTUKA", furniture, new BigDecimal("15.5"));
        createInventoryIfNotExists(chair, loc2, 8, InventoryStatus.AVAILABLE, "CHR001-001");

        logger.info("Sample data created successfully");
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
            location.setDescription("Automatycznie utworzona lokalizacja");
            location.setZone(zone);
            return locationRepository.save(location);
        });
    }

    private Product createProductIfNotExists(String sku, String name, String description, 
                                          BigDecimal price, String unit, Category category, BigDecimal weight) {
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

    private InventoryItem createInventoryIfNotExists(Product product, Location location, 
                                                   int quantity, InventoryStatus status, String qrCode) {
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
    }
}