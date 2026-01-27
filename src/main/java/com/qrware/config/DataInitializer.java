package com.qrware.config;

import com.qrware.domain.inventory.*;
import com.qrware.domain.order.*;
import com.qrware.domain.product.Category;
import com.qrware.domain.product.Product;
import com.qrware.domain.user.Permission;
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
import com.qrware.repository.order.OrderStatusHistoryRepository;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private static final long DEMO_RANDOM_SEED = 20260113L;

    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;

    @Autowired private InventoryItemRepository inventoryItemRepository;
    @Autowired private MovementHistoryRepository movementHistoryRepository;

    @Autowired private LocationRepository locationRepository;
    @Autowired private ZoneRepository zoneRepository;

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    public void run(String... args) {
        logger.info("Initializing default/demo data...");

        createPermissions();
        createRoles();
        createUsers();

        logger.info("Seed status after security: users={}, roles={}, permissions={}",
                userRepository.count(), roleRepository.count(), permissionRepository.count());

        logger.info("Seed status before demo: users={}, roles={}, permissions={}, categories={}, products={}, locations={}, inventoryItems={}, orders={}",
                userRepository.count(), roleRepository.count(), permissionRepository.count(),
                categoryRepository.count(), productRepository.count(), locationRepository.count(),
                inventoryItemRepository.count(), orderRepository.count());

        if (productRepository.count() == 0) {
            try {
                Random random = new Random(DEMO_RANDOM_SEED);

                logger.info("Creating demo categories...");
                createDemoCategories();
                logger.info("After categories: {}", categoryRepository.count());

                logger.info("Creating demo zones and locations...");
                createDemoZonesAndLocations(random);
                logger.info("After zones/locations: zones={}, locations={}", zoneRepository.count(), locationRepository.count());

                logger.info("Creating demo products...");
                createDemoProducts(random);
                logger.info("After products: {}", productRepository.count());

                logger.info("Creating demo inventory...");
                createDemoInventory(random);
                logger.info("After inventory: inventoryItems={}", inventoryItemRepository.count());

                logger.info("Creating demo orders...");
                createDemoOrders(random);
                logger.info("After orders: orders={}, orderItems={}", orderRepository.count(), orderItemRepository.count());

            } catch (Exception e) {
                logger.error("Demo seed failed - data may be partially inserted (no global transaction). Root cause: {}", e.getMessage(), e);
            }
        } else {
            logger.info("Products already exist ({}). Skipping demo dataset.", productRepository.count());
        }

        logger.info("Default/demo data initialization completed");
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

    private Category upsertCategory(String code, String name, String description, Category parent,
                                   boolean active, Integer sortOrder, String icon, String color,
                                   boolean special, Integer tMin, Integer tMax, Integer hMin, Integer hMax) {
        Category c = categoryRepository.findByCode(code).orElseGet(Category::new);
        c.setCode(code);
        c.setName(name);
        c.setDescription(description);
        c.setParent(parent);
        c.setActive(active);
        c.setSortOrder(sortOrder);
        c.setIcon(icon);
        c.setColor(color);
        c.setRequiresSpecialHandling(special);
        c.setStorageTemperatureMin(tMin);
        c.setStorageTemperatureMax(tMax);
        c.setStorageHumidityMin(hMin);
        c.setStorageHumidityMax(hMax);
        return categoryRepository.save(c);
    }

    private Zone upsertZone(String code, String name, String description, ZoneType type,
                            boolean active, boolean tempCtrl, Integer tMin, Integer tMax,
                            boolean humCtrl, Integer hMin, Integer hMax,
                            int securityLevel, boolean hazardous, boolean fragile,
                            int pickingPriority, String manager, String contact, String color) {
        Zone z = zoneRepository.findByCode(code).orElseGet(Zone::new);
        z.setCode(code);
        z.setName(name);
        z.setDescription(description);
        z.setType(type);
        z.setActive(active);
        z.setTemperatureControlled(tempCtrl);
        z.setTemperatureMin(tMin);
        z.setTemperatureMax(tMax);
        z.setHumidityControlled(humCtrl);
        z.setHumidityMin(hMin);
        z.setHumidityMax(hMax);
        z.setSecurityLevel(securityLevel);
        z.setHazardousMaterials(hazardous);
        z.setFragileItems(fragile);
        z.setPickingPriority(pickingPriority);
        z.setManager(manager);
        z.setContactInfo(contact);
        z.setColor(color);
        return zoneRepository.save(z);
    }

    private Location upsertLocation(String code, String name, Zone zone, LocationType type,
                                    String aisle, String rack, String shelf, String bin,
                                    BigDecimal capVol, BigDecimal capWeight, Integer capItems,
                                    boolean tempCtrl, Integer tMin, Integer tMax,
                                    boolean humCtrl, Integer hMin, Integer hMax,
                                    boolean hazardous, boolean fragile, int securityLevel,
                                    boolean active, boolean pickable, boolean receivable,
                                    String qrCode, String barcode,
                                    BigDecimal x, BigDecimal y, BigDecimal z,
                                    String description) {
        Location l = locationRepository.findByCode(code).orElseGet(Location::new);
        l.setCode(code);
        l.setName(name);
        l.setZone(zone);
        l.setType(type);
        l.setAisle(aisle);
        l.setRack(rack);
        l.setShelf(shelf);
        l.setBin(bin);
        l.setCapacityVolume(capVol);
        l.setCapacityWeight(capWeight);
        l.setCapacityItems(capItems);
        l.setTemperatureControlled(tempCtrl);
        l.setTemperatureMin(tMin);
        l.setTemperatureMax(tMax);
        l.setHumidityControlled(humCtrl);
        l.setHumidityMin(hMin);
        l.setHumidityMax(hMax);
        l.setHazardousMaterials(hazardous);
        l.setFragileItems(fragile);
        l.setSecurityLevel(securityLevel);
        l.setActive(active);
        l.setPickable(pickable);
        l.setReceivable(receivable);
        l.setQrCode(qrCode);
        l.setBarcode(barcode);
        l.setxCoordinate(x);
        l.setyCoordinate(y);
        l.setzCoordinate(z);
        l.setDescription(description);
        return locationRepository.save(l);
    }

    private Product upsertProduct(String sku, String name, String description, String barcode, Category category,
                                  BigDecimal price, BigDecimal cost, BigDecimal weight,
                                  BigDecimal len, BigDecimal wid, BigDecimal hei,
                                  String uom, int minStock, Integer maxStock, Integer reorderPoint,
                                  boolean active, boolean perishable, boolean hazardous, boolean fragile,
                                  String manufacturer, String supplier, String storageConditions) {
        Product p = productRepository.findBySku(sku).orElseGet(Product::new);
        p.setSku(sku);
        p.setName(name);
        p.setDescription(description);
        p.setBarcode(barcode);
        p.setCategory(category);
        p.setPrice(price);
        p.setCost(cost);
        p.setWeight(weight);
        p.setDimensionsLength(len);
        p.setDimensionsWidth(wid);
        p.setDimensionsHeight(hei);
        p.setUnitOfMeasure(uom);
        p.setMinimumStock(minStock);
        p.setMaximumStock(maxStock);
        p.setReorderPoint(reorderPoint);
        p.setActive(active);
        p.setPerishable(perishable);
        p.setHazardous(hazardous);
        p.setFragile(fragile);
        p.setManufacturer(manufacturer);
        p.setSupplier(supplier);
        p.setStorageConditions(storageConditions);
        return productRepository.save(p);
    }

    private void createDemoCategories() {
        logger.info("Creating demo categories...");
        Category electronics = upsertCategory("ELEC", "Elektronika", "Urządzenia elektroniczne", null,
                true, 10, "bolt", "#1E88E5", false, null, null, null, null);
        upsertCategory("ELEC-PHN", "Telefony", "Smartfony i akcesoria", electronics,
                true, 20, "phone", "#1565C0", false, null, null, null, null);
        upsertCategory("ELEC-LAP", "Laptopy", "Komputery przenośne", electronics,
                true, 30, "laptop", "#0D47A1", false, null, null, null, null);

        Category food = upsertCategory("FOOD", "Żywność", "Artykuły spożywcze", null,
                true, 40, "food", "#43A047", true, 2, 8, 30, 70);
        upsertCategory("FOOD-DRY", "Produkty suche", "Ryż, makaron, przyprawy", food,
                true, 41, "grain", "#2E7D32", false, null, null, null, null);
        upsertCategory("FOOD-CHL", "Chłodnicze", "Produkty wymagające chłodzenia", food,
                true, 42, "snowflake", "#00ACC1", true, 2, 6, 30, 60);

        Category chemicals = upsertCategory("CHEM", "Chemia", "Środki chemiczne i czyszczące", null,
                true, 50, "flask", "#F4511E", true, 10, 25, 20, 60);
        upsertCategory("CHEM-HZ", "Niebezpieczne", "Substancje niebezpieczne", chemicals,
                true, 51, "hazard", "#D84315", true, 10, 25, 20, 60);

        Category apparel = upsertCategory("APP", "Odzież", "Odzież i akcesoria", null,
                true, 60, "shirt", "#8E24AA", false, null, null, null, null);
        upsertCategory("APP-SHO", "Obuwie", "Buty", apparel,
                true, 61, "shoe", "#6A1B9A", false, null, null, null, null);
    }

    private void createDemoZonesAndLocations(Random random) {
        logger.info("Creating demo zones and locations...");

        Zone receiving = upsertZone("Z-REC", "Strefa przyjęć", "Dock przyjęć", ZoneType.RECEIVING,
                true, false, null, null, false, null, null,
                1, false, false, 2, "manager", "receiving@qrware.com", "#607D8B");
        Zone shipping = upsertZone("Z-SHP", "Strefa wysyłki", "Dock wysyłek", ZoneType.SHIPPING,
                true, false, null, null, false, null, null,
                1, false, false, 2, "manager", "shipping@qrware.com", "#546E7A");
        Zone storage = upsertZone("Z-A", "Magazyn A", "Regały A", ZoneType.STORAGE,
                true, false, null, null, false, null, null,
                1, false, false, 3, "manager", "a@qrware.com", "#9E9D24");
        Zone cold = upsertZone("Z-COLD", "Chłodnia", "Magazyn chłodniczy", ZoneType.COLD_STORAGE,
                true, true, 2, 6, true, 30, 60,
                2, false, true, 2, "manager", "cold@qrware.com", "#00ACC1");
        Zone hazmat = upsertZone("Z-HZ", "Hazmat", "Strefa chemii", ZoneType.HAZMAT,
                true, false, null, null, false, null, null,
                3, true, false, 4, "manager", "hazmat@qrware.com", "#F4511E");

        upsertLocation("REC-01", "Dock A", receiving, LocationType.RECEIVING,
                "R", "01", null, null,
                new BigDecimal("50.000"), new BigDecimal("5000.000"), 100,
                false, null, null, false, null, null,
                false, false, 1,
                true, false, true,
                "LOC:REC-01", "BC_REC_01",
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "Stanowisko przyjęć");

        upsertLocation("SHP-01", "Dock Wysyłek", shipping, LocationType.SHIPPING,
                "S", "01", null, null,
                new BigDecimal("50.000"), new BigDecimal("5000.000"), 100,
                false, null, null, false, null, null,
                false, false, 1,
                true, true, false,
                "LOC:SHP-01", "BC_SHP_01",
                new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                "Stanowisko wysyłek");

        for (int aisle = 1; aisle <= 3; aisle++) {
            for (int rack = 1; rack <= 4; rack++) {
                for (int shelf = 1; shelf <= 2; shelf++) {
                    String code = String.format("A%02d-R%02d-S%02d", aisle, rack, shelf);
                    upsertLocation(code, "Regał " + code, storage, LocationType.RACK,
                            String.valueOf(aisle), String.valueOf(rack), String.valueOf(shelf), "01",
                            new BigDecimal("5.000"), new BigDecimal("500.000"), 50,
                            false, null, null, false, null, null,
                            false, false, 1,
                            true, true, true,
                            "LOC:" + code, "BC_" + code,
                            new BigDecimal(aisle * 10), new BigDecimal(rack * 2), new BigDecimal(shelf),
                            "Lokacja magazynowa");
                }
            }
        }

        for (int i = 1; i <= 6; i++) {
            String code = String.format("COLD-%02d", i);
            upsertLocation(code, "Chłodnia " + i, cold, LocationType.COLD_STORAGE,
                    "C", "01", String.valueOf(i), "01",
                    new BigDecimal("3.000"), new BigDecimal("300.000"), 30,
                    true, 2, 6, true, 30, 60,
                    false, true, 2,
                    true, true, true,
                    "LOC:" + code, "BC_" + code,
                    new BigDecimal("200.00"), new BigDecimal(i), BigDecimal.ZERO,
                    "Lokacja chłodnicza");
        }

        for (int i = 1; i <= 4; i++) {
            String code = String.format("HZ-%02d", i);
            upsertLocation(code, "Hazmat " + i, hazmat, LocationType.HAZMAT,
                    "H", "01", String.valueOf(i), "01",
                    new BigDecimal("4.000"), new BigDecimal("400.000"), 20,
                    false, null, null, false, null, null,
                    true, false, 4,
                    true, true, true,
                    "LOC:" + code, "BC_" + code,
                    new BigDecimal("300.00"), new BigDecimal(i), BigDecimal.ZERO,
                    "Lokacja niebezpieczna");
        }

    }

    private void createDemoProducts(Random random) {
        logger.info("Creating demo products...");

        Category phones = categoryRepository.findByCode("ELEC-PHN").orElseThrow();
        Category laptops = categoryRepository.findByCode("ELEC-LAP").orElseThrow();
        Category foodDry = categoryRepository.findByCode("FOOD-DRY").orElseThrow();
        Category foodChl = categoryRepository.findByCode("FOOD-CHL").orElseThrow();
        Category chemHz = categoryRepository.findByCode("CHEM-HZ").orElseThrow();
        Category shoes = categoryRepository.findByCode("APP-SHO").orElseThrow();

        upsertProduct("PHN-IPH14P", "iPhone 14 Pro", "Smartfon Apple", "5900000000011", phones,
                new BigDecimal("5999.99"), new BigDecimal("4200.00"), new BigDecimal("0.206"),
                new BigDecimal("0.15"), new BigDecimal("0.07"), new BigDecimal("0.008"),
                "PIECE", 5, 200, 20,
                true, false, false, true,
                "Apple", "Apple Distributor", "Temp. pokojowa");

        upsertProduct("LAP-DELL-XPS13", "Dell XPS 13", "Ultrabook", "5900000000101", laptops,
                new BigDecimal("4999.99"), new BigDecimal("3600.00"), new BigDecimal("1.200"),
                new BigDecimal("0.30"), new BigDecimal("0.20"), new BigDecimal("0.018"),
                "PIECE", 2, 80, 10,
                true, false, false, true,
                "Dell", "Dell Polska", "Chronić przed wstrząsami");

        upsertProduct("FOOD-RICE-5KG", "Ryż basmati 5kg", "Ryż", "5900000000201", foodDry,
                new BigDecimal("39.99"), new BigDecimal("22.00"), new BigDecimal("5.000"),
                new BigDecimal("0.20"), new BigDecimal("0.10"), new BigDecimal("0.30"),
                "PIECE", 20, 500, 50,
                true, true, false, true,
                "FoodCo", "Food Supplier", "Suche miejsce");

        upsertProduct("FOOD-MILK-1L", "Mleko 1L", "Mleko UHT", "5900000000202", foodChl,
                new BigDecimal("4.99"), new BigDecimal("3.10"), new BigDecimal("1.050"),
                new BigDecimal("0.07"), new BigDecimal("0.07"), new BigDecimal("0.24"),
                "PIECE", 50, 2000, 200,
                true, true, false, true,
                "DairyCo", "Dairy Supplier", "Chłodnia");

        upsertProduct("CHEM-ACID-1L", "Kwas (test) 1L", "Przykładowa substancja niebezpieczna", "5900000000301", chemHz,
                new BigDecimal("89.90"), new BigDecimal("55.00"), new BigDecimal("1.200"),
                new BigDecimal("0.08"), new BigDecimal("0.08"), new BigDecimal("0.30"),
                "PIECE", 5, 200, 20,
                true, false, true, true,
                "ChemCorp", "Chem Supplier", "Hazmat");

        upsertProduct("APP-SHOE-42", "Buty sportowe r.42", "Obuwie sportowe", "5900000000401", shoes,
                new BigDecimal("199.99"), new BigDecimal("110.00"), new BigDecimal("0.800"),
                new BigDecimal("0.33"), new BigDecimal("0.20"), new BigDecimal("0.12"),
                "PIECE", 10, 500, 30,
                true, false, false, true,
                "SportBrand", "Shoes Supplier", "Suche miejsce");

        List<Category> cats = List.of(phones, laptops, foodDry, shoes);
        while (productRepository.count() < 20) {
            int idx = (int) productRepository.count() + 1;
            Category cat = cats.get(idx % cats.size());
            String sku = String.format("DEMO-%03d", idx);
            upsertProduct(sku, "Produkt demo " + idx, "Opis produktu demo " + idx,
                    "590000009" + String.format("%04d", idx), cat,
                    new BigDecimal(10 + idx * 3 + ".99"), new BigDecimal(6 + idx * 2 + ".50"),
                    new BigDecimal("0.5"), new BigDecimal("0.10"), new BigDecimal("0.10"), new BigDecimal("0.10"),
                    "PIECE", 5, 500, 50,
                    true, idx % 5 == 0, idx % 7 == 0, idx % 4 == 0,
                    "DemoManu", "DemoSupplier", "Warunki standardowe");
        }

    }

    private Location pickLocationForProduct(List<Location> locations, Product p, Random random) {
        List<Location> candidates = locations.stream().filter(Location::getActive).toList();
        if (Boolean.TRUE.equals(p.getPerishable())) {
            List<Location> cold = candidates.stream().filter(l -> l.getZone().getType() == ZoneType.COLD_STORAGE).toList();
            if (!cold.isEmpty()) return cold.get(random.nextInt(cold.size()));
        }
        if (Boolean.TRUE.equals(p.getHazardous())) {
            List<Location> hz = candidates.stream().filter(l -> l.getZone().getType() == ZoneType.HAZMAT).toList();
            if (!hz.isEmpty()) return hz.get(random.nextInt(hz.size()));
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private void createDemoInventory(Random random) {
        logger.info("Creating demo inventory items, movement history and QR...");

        User worker = userRepository.findByUsername("worker").orElseThrow();
        User manager = userRepository.findByUsername("manager").orElseThrow();

        List<Location> locations = locationRepository.findAllWithZone();
        List<Product> products = productRepository.findAll();

        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0);
        for (Product p : products) {
            int items = 1 + (Math.abs(p.getSku().hashCode()) % 2);
            for (int i = 0; i < items; i++) {
                Location loc = pickLocationForProduct(locations, p, random);
                int qty = 5 + random.nextInt(80);

                final Product product = p;
                final int index = i;
                final Location location = loc;
                final int quantity = qty;

                InventoryItem item = inventoryItemRepository.findByProductAndLocation(product, location).orElseGet(() -> {
                    InventoryItem it = new InventoryItem();
                    it.setProduct(product);
                    it.setLocation(location);
                    it.setQuantity(quantity);
                    it.setReservedQuantity(random.nextInt(3));
                    it.setStatus(InventoryStatus.AVAILABLE);
                    it.setQrCode("PENDING:" + product.getSku() + ":" + location.getCode() + ":" + (100 + index));
                    it.setReceivedDate(LocalDate.now().minusDays(random.nextInt(30)));
                    it.setLotNumber("LOT-" + LocalDate.now().getYear() + "-" + String.format("%04d", counter.incrementAndGet()));
                    it.setBatchNumber("BATCH-" + product.getSku() + "-" + (100 + index));
                    it.setSerialNumber(Boolean.TRUE.equals(product.getFragile()) ? "SN-" + UUID.randomUUID().toString().substring(0, 12) : null);
                    it.setUnitCost(product.getCost());
                    it.setManufacturer(product.getManufacturer());
                    it.setSupplierReference("SUP-REF-" + product.getSku());
                    it.setPurchaseOrderNumber("PO-" + LocalDate.now().getYear() + "-" + (1000 + random.nextInt(9000)));
                    it.setNotes("Demo stock for " + product.getSku() + " @ " + location.getCode());

                    if (Boolean.TRUE.equals(product.getPerishable())) {
                        it.setManufactureDate(LocalDate.now().minusDays(10 + random.nextInt(60)));
                        it.setExpiryDate(LocalDate.now().plusDays(7 + random.nextInt(30)));
                    }
                    if (location.getTemperatureControlled()) {
                        it.setTemperature(location.getTemperatureMin() != null ? location.getTemperatureMin() + 1 : 4);
                    }
                    if (location.getHumidityControlled()) {
                        it.setHumidity(location.getHumidityMin() != null ? location.getHumidityMin() + 5 : 45);
                    }
                    it.setConditionRating(8 + random.nextInt(3));

                    boolean quarantine = Boolean.TRUE.equals(product.getHazardous()) && random.nextInt(20) == 0;
                    boolean hold = random.nextInt(30) == 0;
                    it.setQuarantine(quarantine);
                    it.setQuarantineReason(quarantine ? "Wymaga weryfikacji BHP" : null);
                    it.setHold(hold);
                    it.setHoldReason(hold ? "Wstrzymane do kontroli" : null);

                    it.setLastMovedDate(LocalDateTime.now().minusDays(random.nextInt(20)));
                    it.setLastCountedDate(LocalDateTime.now().minusDays(random.nextInt(20)));
                    return inventoryItemRepository.save(it);
                });

                if (movementHistoryRepository.findByInventoryItemId(item.getId()).isEmpty()) {
                    MovementHistory receipt = new MovementHistory();
                    receipt.setInventoryItem(item);
                    receipt.setMovementType(MovementType.RECEIPT);
                    receipt.setMovementDate(item.getReceivedDate().atStartOfDay());
                    receipt.setQuantityBefore(0);
                    receipt.setQuantityAfter(item.getQuantity());
                    receipt.setQuantityChanged(item.getQuantity());
                    receipt.setToLocation(item.getLocation());
                    receipt.setStatusAfter(item.getStatus());
                    receipt.setUnitCost(item.getUnitCost());
                    receipt.setTotalCost(item.getTotalCost());
                    receipt.setReferenceType("PO");
                    receipt.setReferenceNumber(item.getPurchaseOrderNumber());
                    receipt.setUserId(String.valueOf(manager.getId()));
                    receipt.setUserName(manager.getUsername());
                    receipt.setApproved(true);
                    receipt.setApprovedBy(String.valueOf(manager.getId()));
                    receipt.setApprovedDate(item.getReceivedDate().atStartOfDay().plusHours(1));
                    receipt.setSystemGenerated(true);
                    movementHistoryRepository.save(receipt);

                    if (random.nextInt(5) == 0) {
                        Location newLoc = locations.get(random.nextInt(locations.size()));
                        MovementHistory move = new MovementHistory();
                        move.setInventoryItem(item);
                        move.setMovementType(MovementType.MOVE);
                        move.setMovementDate(LocalDateTime.now().minusDays(random.nextInt(10)));
                        move.setFromLocation(item.getLocation());
                        move.setToLocation(newLoc);
                        move.setQuantityChanged(0);
                        move.setReason("Relokacja demo");
                        move.setUserId(String.valueOf(worker.getId()));
                        move.setUserName(worker.getUsername());
                        move.setApproved(true);
                        move.setApprovedBy(String.valueOf(manager.getId()));
                        move.setApprovedDate(LocalDateTime.now());
                        movementHistoryRepository.save(move);
                    }
                }
            }
        }
    }

    private void addStatusHistoryIfMissing(Order order, OrderStatus oldStatus, OrderStatus newStatus, User user, LocalDateTime changedAt, String reason) {
        boolean exists = orderStatusHistoryRepository.findByOrder(order).stream()
                .anyMatch(h -> h.getOldStatus() == oldStatus && h.getNewStatus() == newStatus);
        if (exists) return;
        OrderStatusHistory h = new OrderStatusHistory(order, oldStatus, newStatus, user, reason);
        h.setChangedAt(changedAt);
        orderStatusHistoryRepository.save(h);
    }

    private void createDemoOrders(Random random) {
        logger.info("Creating demo orders...");
        if (orderRepository.count() > 0) return;

        User admin = userRepository.findByUsername("admin").orElseThrow();
        User worker = userRepository.findByUsername("worker").orElseThrow();

        List<Location> locations = locationRepository.findAllWithZone();
        List<Product> products = productRepository.findAll();
        List<InventoryItem> inventory = inventoryItemRepository.findAllWithProductAndLocationZone();

        Order inbound = new Order("ORD-IN-001", OrderType.INBOUND, admin);
        inbound.setDescription("Demo przyjęcie towaru");
        inbound.setPriority(OrderPriority.NORMAL);
        inbound.setDestinationLocation(locations.get(0));
        inbound.setExpectedDate(LocalDateTime.now().plusDays(1));
        orderRepository.save(inbound);

        for (int i = 0; i < 3; i++) {
            Product p = products.get(i % products.size());
            OrderItem oi = new OrderItem(inbound, i + 1, p, 10 + random.nextInt(30));
            oi.setUnitPrice(p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO);
            oi.setNotes("Pozycja demo");
            oi.setRequiresExactInventory(false);
            orderItemRepository.save(oi);
        }
        inbound.updateProgress();
        orderRepository.save(inbound);
        addStatusHistoryIfMissing(inbound, null, OrderStatus.CREATED, admin, LocalDateTime.now().minusHours(4), "Utworzone");

        Order pick = new Order("ORD-PICK-001", OrderType.PICK, admin);
        pick.setDescription("Demo kompletacja");
        pick.assign(worker);
        pick.setSourceLocation(locations.stream().filter(Location::getPickable).findFirst().orElse(locations.get(0)));
        pick.setPriority(OrderPriority.HIGH);
        pick.setExpectedDate(LocalDateTime.now().plusHours(6));
        orderRepository.save(pick);

        for (int i = 0; i < 4; i++) {
            InventoryItem inv = inventory.get(random.nextInt(inventory.size()));
            OrderItem oi = new OrderItem(pick, i + 1, inv.getProduct(), 1 + random.nextInt(5));
            oi.setInventoryItem(inv);
            oi.setSourceLocation(inv.getLocation());
            oi.setUnitPrice(inv.getProduct().getPrice() != null ? inv.getProduct().getPrice() : BigDecimal.ZERO);
            oi.setBatchNumber(inv.getBatchNumber());
            oi.setSerialNumber(inv.getSerialNumber());
            oi.setNotes("Pobranie z " + inv.getLocation().getCode());
            oi.setRequiresExactInventory(true);
            orderItemRepository.save(oi);
        }
        pick.updateProgress();
        orderRepository.save(pick);
        addStatusHistoryIfMissing(pick, null, OrderStatus.CREATED, admin, LocalDateTime.now().minusHours(3), "Utworzone");
        addStatusHistoryIfMissing(pick, OrderStatus.CREATED, OrderStatus.ASSIGNED, admin, LocalDateTime.now().minusHours(2), "Przypisane");
        addStatusHistoryIfMissing(pick, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS, worker, LocalDateTime.now().minusHours(1), "Rozpoczęte");
    }

}