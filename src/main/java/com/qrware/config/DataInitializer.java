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
import java.util.List;
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
import com.qrware.domain.order.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Autowired
    private com.qrware.repository.order.OrderRepository orderRepository;

    @Autowired
    private com.qrware.repository.order.OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Initializing default data...");

        createPermissions();
        createRoles();
        createUsers();
        createSampleData();

        // Create sample orders
        createSampleOrders();
        
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

        // Order permissions
        createPermissionIfNotExists("ORDER_READ", "Read order information", "ORDER", "READ");
        createPermissionIfNotExists("ORDER_WRITE", "Create and update orders", "ORDER", "WRITE");
        createPermissionIfNotExists("ORDER_ASSIGN", "Assign orders to users", "ORDER", "ASSIGN");
        createPermissionIfNotExists("ORDER_DELETE", "Delete orders", "ORDER", "DELETE");

        // Movement permissions
        createPermissionIfNotExists("MOVEMENT_READ", "Read movement history", "MOVEMENT", "READ");
        createPermissionIfNotExists("MOVEMENT_WRITE", "Create movement entries", "MOVEMENT", "WRITE");

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
            workerPermissions.add(getPermission("ORDER_READ"));
            workerPermissions.add(getPermission("ORDER_WRITE"));
            workerPermissions.add(getPermission("MOVEMENT_READ"));
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
            managerPermissions.add(getPermission("ORDER_READ"));
            managerPermissions.add(getPermission("ORDER_WRITE"));
            managerPermissions.add(getPermission("ORDER_ASSIGN"));
            managerPermissions.add(getPermission("ORDER_DELETE"));
            managerPermissions.add(getPermission("MOVEMENT_READ"));
            managerPermissions.add(getPermission("MOVEMENT_WRITE"));
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
            adminPermissions.add(getPermission("QR_SCAN"));
            adminPermissions.add(getPermission("QR_GENERATE"));
            adminPermissions.add(getPermission("ORDER_READ"));
            adminPermissions.add(getPermission("ORDER_WRITE"));
            adminPermissions.add(getPermission("ORDER_ASSIGN"));
            adminPermissions.add(getPermission("ORDER_DELETE"));
            adminPermissions.add(getPermission("MOVEMENT_READ"));
            adminPermissions.add(getPermission("MOVEMENT_WRITE"));
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
        logger.info("Creating extensive sample data...");

        // 15 kategorii produktów
        Category electronics = createCategoryIfNotExists("ELEC", "Elektronika", "Urządzenia elektroniczne");
        Category furniture = createCategoryIfNotExists("FURN", "Meble", "Meble biurowe i domowe");
        Category automotive = createCategoryIfNotExists("AUTO", "Motoryzacja", "Części samochodowe");
        Category food = createCategoryIfNotExists("FOOD", "Żywność", "Produkty spożywcze");
        Category clothing = createCategoryIfNotExists("CLTH", "Odzież", "Ubrania i akcesoria");
        Category sports = createCategoryIfNotExists("SPRT", "Sport", "Artykuły sportowe");
        Category books = createCategoryIfNotExists("BOOK", "Książki", "Literatura i podręczniki");
        Category toys = createCategoryIfNotExists("TOYS", "Zabawki", "Zabawki dla dzieci");
        Category tools = createCategoryIfNotExists("TOOL", "Narzędzia", "Narzędzia i części");
        Category health = createCategoryIfNotExists("HLTH", "Zdrowie", "Produkty medyczne");
        Category garden = createCategoryIfNotExists("GARD", "Ogród", "Artykuły ogrodnicze");
        Category cosmetics = createCategoryIfNotExists("COSM", "Kosmetyki", "Produkty kosmetyczne");
        Category music = createCategoryIfNotExists("MUSC", "Muzyka", "Instrumenty muzyczne");
        Category pets = createCategoryIfNotExists("PETS", "Zwierzęta", "Produkty dla zwierząt");
        Category office = createCategoryIfNotExists("OFFC", "Biuro", "Artykuły biurowe");

        // 12 stref magazynowych
        Zone[] zones = createExtensiveZones();
        
        // 200+ lokalizacji
        Location[] locations = createExtensiveLocations(zones);

        // 150+ produktów
        Product[] products = createExtensiveProducts(new Category[]{
            electronics, furniture, automotive, food, clothing, sports, 
            books, toys, tools, health, garden, cosmetics, music, pets, office
        });

        // 300+ pozycji inwentarza
        createExtensiveInventory(products, locations);

        logger.info("Extensive sample data created successfully - 15 categories, 12 zones, 200+ locations, 150+ products, 300+ inventory items");
    }

    private Zone[] createExtensiveZones() {
        Zone[] zones = new Zone[12];
        zones[0] = createZoneIfNotExists("A-MAIN", "Główna strefa A", "Elektronika i wysokowartościowe", ZoneType.STORAGE);
        zones[1] = createZoneIfNotExists("B-BULK", "Strefa B - Bulk", "Duże gabaryty", ZoneType.STORAGE);
        zones[2] = createZoneIfNotExists("C-COLD", "Strefa chłodnicza", "Produkty wymagające chłodzenia", ZoneType.STORAGE);
        zones[3] = createZoneIfNotExists("D-HAZ", "Strefa niebezpieczna", "Materiały niebezpieczne", ZoneType.STORAGE);
        zones[4] = createZoneIfNotExists("R-NORTH", "Przyjęcie Północ", "Główny dock przyjęć", ZoneType.RECEIVING);
        zones[5] = createZoneIfNotExists("R-SOUTH", "Przyjęcie Południe", "Dock ekspresowy", ZoneType.RECEIVING);
        zones[6] = createZoneIfNotExists("S-EAST", "Wysyłka Wschód", "Główna wysyłka", ZoneType.SHIPPING);
        zones[7] = createZoneIfNotExists("S-WEST", "Wysyłka Zachód", "Wysyłka ekspresowa", ZoneType.SHIPPING);
        zones[8] = createZoneIfNotExists("P-PICK", "Strefa kompletacji", "Szybka kompletacja", ZoneType.PICKING);
        zones[9] = createZoneIfNotExists("Q-QC", "Kontrola jakości", "QC i inspekcja", ZoneType.QUALITY_CONTROL);
        zones[10] = createZoneIfNotExists("O-OFFICE", "Biuro", "Strefa administracyjna", ZoneType.OFFICE);
        zones[11] = createZoneIfNotExists("M-MAINT", "Konserwacja", "Warsztat napraw", ZoneType.MAINTENANCE);
        return zones;
    }

    private Location[] createExtensiveLocations(Zone[] zones) {
        java.util.List<Location> locationsList = new java.util.ArrayList<>();
        int counter = 1;

        for (Zone zone : zones) {
            int locCount = switch (zone.getType()) {
                case STORAGE -> 25;
                case RECEIVING, SHIPPING -> 8;
                case PICKING -> 15;
                default -> 5;
            };

            for (int i = 1; i <= locCount; i++) {
                String aisle = String.format("%02d", (i - 1) / 5 + 1);
                String rack = String.format("%02d", (i - 1) % 5 + 1);
                String shelf = String.format("%02d", ((i - 1) % 3) + 1);
                String bin = String.format("%02d", i % 4 + 1);
                
                Location location = createLocationIfNotExists(
                    zone.getCode() + "-" + aisle + "-" + rack + "-" + shelf + "-" + bin,
                    zone.getName() + " - " + aisle + "." + rack + "." + shelf,
                    zone
                );
                
                // Dodanie współrzędnych i pojemności
                location.setAisle(aisle);
                location.setRack(rack);
                location.setShelf(shelf);
                location.setBin(bin);
                location.setCapacityWeight(new BigDecimal(zone.getType() == ZoneType.STORAGE ? 1000 + (i * 20) : 500));
                location.setCapacityVolume(new BigDecimal(zone.getType() == ZoneType.STORAGE ? 50 + i : 25));
                locationRepository.save(location);
                
                locationsList.add(location);
                counter++;
            }
        }
        return locationsList.toArray(new Location[0]);
    }

    private Product[] createExtensiveProducts(Category[] categories) {
        java.util.List<Product> productsList = new java.util.ArrayList<>();
        
        String[][] productData = {
            // Electronics
            {"LAP001", "Laptop Dell XPS 13", "Ultrabook 13.3 cali", "4999.99", "1.2"},
            {"LAP002", "Laptop HP Spectre", "Premium ultrabook", "5499.99", "1.1"},
            {"PHN001", "iPhone 14 Pro", "Najnowszy iPhone", "5999.99", "0.2"},
            {"PHN002", "Samsung Galaxy S23", "Flagowy Android", "4799.99", "0.19"},
            {"TAB001", "iPad Pro 12.9", "Profesjonalny tablet", "4999.99", "0.6"},
            {"MON001", "Monitor Dell 27\"", "4K monitor", "1899.99", "6.5"},
            {"KEY001", "Klawiatura mechaniczna", "Gaming keyboard", "399.99", "1.2"},
            {"MOU001", "Mysz bezprzewodowa", "Ergonomiczna mysz", "199.99", "0.1"},
            {"HDD001", "Dysk twardy 1TB", "External HDD", "299.99", "0.8"},
            {"SSD001", "SSD 1TB NVMe", "Szybki dysk SSD", "499.99", "0.05"},
            
            // Meble
            {"CHR001", "Krzesło biurowe", "Ergonomiczne krzesło", "899.99", "15.5"},
            {"DSK001", "Biurko regulowane", "Standing desk", "1299.99", "45.0"},
            {"SHF001", "Regał biurowy", "5-półkowy regał", "599.99", "35.2"},
            {"ARM001", "Fotel wypoczynkowy", "Skórzany fotel", "2999.99", "42.0"},
            {"TAB002", "Stół konferencyjny", "Stół dla 8 osób", "3499.99", "85.0"},
            
            // Motoryzacja
            {"TIR001", "Opona letnia 205/55R16", "Opona samochodowa", "299.99", "8.5"},
            {"OIL001", "Olej silnikowy 5W-30", "Syntetyczny olej", "89.99", "4.2"},
            {"BAT001", "Akumulator 12V 60Ah", "Akumulator samochodowy", "399.99", "18.0"},
            {"BRK001", "Klocki hamulcowe", "Przednie klocki", "199.99", "2.1"},
            {"FLT001", "Filtr powietrza", "Filtr silnika", "49.99", "0.5"},
            
            // Żywność
            {"RIC001", "Ryż basmati 1kg", "Długoziarnisty ryż", "12.99", "1.0"},
            {"PAT001", "Makaron spaghetti", "Włoski makaron", "8.99", "0.5"},
            {"OLV001", "Oliwa z oliwek", "Extra virgin", "24.99", "0.5"},
            {"HON001", "Miód wielokwiatowy", "Naturalny miód", "19.99", "0.4"},
            {"COF001", "Kawa ziarnista", "Arabica 100%", "39.99", "1.0"},
            
            // Odzież
            {"TSH001", "T-shirt bawełniany", "100% bawełna", "49.99", "0.2"},
            {"JEA001", "Jeansy klasyczne", "Blue denim", "149.99", "0.6"},
            {"SWE001", "Sweter wełniany", "Merino wool", "199.99", "0.4"},
            {"SHO001", "Buty sportowe", "Running shoes", "299.99", "0.8"},
            {"JAC001", "Kurtka zimowa", "Puchowa kurtka", "399.99", "1.2"},
            
            // Sport
            {"BAL001", "Piłka nożna", "Skórzana piłka", "89.99", "0.4"},
            {"RAC001", "Rakieta tenisowa", "Professional racket", "299.99", "0.3"},
            {"MAT001", "Mata do jogi", "Antypoślizgowa mata", "79.99", "1.5"},
            {"DUM001", "Hantle 10kg para", "Żeliwne hantle", "199.99", "20.0"},
            {"TRE001", "Bieżnia elektryczna", "Home treadmill", "2999.99", "85.0"},
            
            // Książki
            {"BOO001", "Java Programming", "Programming guide", "89.99", "0.8"},
            {"BOO002", "Data Structures", "Computer science", "119.99", "1.0"},
            {"BOO003", "Web Development", "Full-stack guide", "99.99", "0.9"},
            {"BOO004", "Machine Learning", "AI handbook", "139.99", "1.1"},
            {"BOO005", "Database Design", "SQL and NoSQL", "109.99", "0.9"},
            
            // Zabawki
            {"TOY001", "Klocki LEGO", "Zestaw konstruktor", "199.99", "2.5"},
            {"TOY002", "Lalka Barbie", "Fashion doll", "79.99", "0.3"},
            {"TOY003", "Samochód RC", "Zdalnie sterowany", "299.99", "1.8"},
            {"TOY004", "Puzzle 1000 ele.", "Landscape puzzle", "39.99", "0.6"},
            {"TOY005", "Pluszowy miś", "Teddy bear", "59.99", "0.4"},
            
            // Narzędzia
            {"DRI001", "Wiertarka akumulatorowa", "18V drill", "299.99", "2.1"},
            {"HAM001", "Młotek stalowy", "500g hammer", "49.99", "0.5"},
            {"SAW001", "Piła spalinowa", "Chainsaw 45cm", "899.99", "6.8"},
            {"WRN001", "Klucze nasadowe", "Socket set 42pcs", "199.99", "3.2"},
            {"MEA001", "Taśma miernicza 5m", "Measuring tape", "29.99", "0.3"},
            
            // Zdrowie
            {"VIT001", "Witamina C 1000mg", "100 tabletek", "24.99", "0.1"},
            {"PRO001", "Białko serwatkowe", "Protein powder 2kg", "149.99", "2.0"},
            {"FIS001", "Omega-3", "Fish oil capsules", "39.99", "0.2"},
            {"CAL001", "Wapń + D3", "Bone health", "29.99", "0.15"},
            {"MAG001", "Magnez B6", "Muscle support", "19.99", "0.1"}
        };

        for (int i = 0; i < productData.length; i++) {
            String[] data = productData[i];
            Category category = categories[i % categories.length];
            
            Product product = createProductIfNotExists(
                data[0], data[1], data[2], 
                new BigDecimal(data[3]), "SZTUKA", 
                category, new BigDecimal(data[4])
            );
            productsList.add(product);
        }
        
        return productsList.toArray(new Product[0]);
    }

    private void createExtensiveInventory(Product[] products, Location[] locations) {
        java.util.Random random = new java.util.Random();
        
        for (Product product : products) {
            int itemCount = 2 + random.nextInt(4); // 2-5 pozycji inwentarza na produkt
            
            for (int i = 0; i < itemCount; i++) {
                Location location = locations[random.nextInt(locations.length)];
                int quantity = 5 + random.nextInt(96); // 5-100 sztuk
                InventoryStatus status = random.nextBoolean() ? InventoryStatus.AVAILABLE : 
                    (random.nextBoolean() ? InventoryStatus.RESERVED : InventoryStatus.DAMAGED);
                
                String qrCode = product.getSku() + "-" + String.format("%03d", i + 1);
                
                createInventoryIfNotExists(product, location, quantity, status, qrCode);
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
        if (orderRepository.count() > 0) {
            logger.info("📦 Orders already exist, skipping sample order creation");
            return;
        }

        try {
            logger.info("📦 Creating sample orders...");

            // Get sample data
            User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
            User manager = userRepository.findByUsername("manager")
                .orElseThrow(() -> new RuntimeException("Manager user not found"));
            User worker = userRepository.findByUsername("worker")
                .orElseThrow(() -> new RuntimeException("Worker user not found"));

            Product product1 = productRepository.findBySku("PHN001")
                .orElseThrow(() -> new RuntimeException("Phone product not found"));
            Product product2 = productRepository.findBySku("LAP001")
                .orElseThrow(() -> new RuntimeException("Laptop product not found"));

            List<Location> locations = locationRepository.findAll();
            if (locations.size() < 2) {
                logger.warn("Not enough locations for sample orders, skipping...");
                return;
            }
            Location location1 = locations.get(0);
            Location location2 = locations.get(1);

            Order order1 = new Order("ORD-001", OrderType.INBOUND, admin);
            order1.setDescription("Przyjęcie nowych telefonów - dostawa od dostawcy");
            order1.setPriority(OrderPriority.HIGH);
            order1.setAssignedTo(worker);
            order1.setDestinationLocation(location1);
            order1.setExpectedDate(LocalDateTime.now().plusDays(1));
            order1.setNotes("Sprawdzić stan opakowań przy odbiorze");
            order1.setExternalReference("SUPP-DEL-2024-001");
            order1 = orderRepository.save(order1);

            // Add order items to order 1
            OrderItem item1_1 = new OrderItem(order1, 1, product1, 50);
            item1_1.setDestinationLocation(location1);
            item1_1.setUnitPrice(new BigDecimal("2500.00"));
            item1_1.setNotes("Nowe iPhone 15 Pro - sprawdzić IMEI");
            orderItemRepository.save(item1_1);

            OrderItem item1_2 = new OrderItem(order1, 2, product2, 20);
            item1_2.setDestinationLocation(location1);
            item1_2.setUnitPrice(new BigDecimal("4200.00"));
            item1_2.setNotes("Laptopy Dell - sprawdzić licencje Windows");
            orderItemRepository.save(item1_2);

            // Update order totals
            order1.updateProgress();
            orderRepository.save(order1);

            // Sample Order 2 - OUTBOUND (Normal Priority, assigned to worker)
            Order order2 = new Order("ORD-002", OrderType.OUTBOUND, manager);
            order2.setDescription("Wysyłka zamówienia dla klienta VIP");
            order2.setPriority(OrderPriority.NORMAL);
            order2.setAssignedTo(worker);
            order2.setSourceLocation(location1);
            order2.setExpectedDate(LocalDateTime.now().plusHours(4));
            order2.setNotes("Klient VIP - priorytetowe opakowanie");
            order2.setExternalReference("CUST-ORDER-VIP-123");
            order2.setStatus(OrderStatus.IN_PROGRESS);
            order2.setStartedAt(LocalDateTime.now().minusHours(1));
            order2 = orderRepository.save(order2);

            // Add order item to order 2
            OrderItem item2_1 = new OrderItem(order2, 1, product1, 5);
            item2_1.setSourceLocation(location1);
            item2_1.setUnitPrice(new BigDecimal("2500.00"));
            item2_1.setCompletedQuantity(3); // Partially completed
            item2_1.setStatus(OrderItemStatus.PARTIALLY_COMPLETED);
            item2_1.setPickedAt(LocalDateTime.now().minusMinutes(30));
            item2_1.setNotes("3 sztuki już skompletowane, pozostałe 2 w trakcie");
            orderItemRepository.save(item2_1);

            // Update order totals
            order2.updateProgress();
            orderRepository.save(order2);

            // Sample Order 3 - TRANSFER (Urgent Priority, not assigned yet)
            Order order3 = new Order("ORD-003", OrderType.TRANSFER, admin);
            order3.setDescription("Transfer sprzętu między strefami magazynowymi");
            order3.setPriority(OrderPriority.URGENT);
            order3.setSourceLocation(location1);
            order3.setDestinationLocation(location2);
            order3.setExpectedDate(LocalDateTime.now().plusHours(2));
            order3.setNotes("Pilny transfer - reorganizacja magazynu");
            order3.setStatus(OrderStatus.CREATED);
            order3 = orderRepository.save(order3);

            // Add order item to order 3
            OrderItem item3_1 = new OrderItem(order3, 1, product2, 10);
            item3_1.setSourceLocation(location1);
            item3_1.setDestinationLocation(location2);
            item3_1.setUnitPrice(new BigDecimal("4200.00"));
            item3_1.setNotes("Przeniesienie laptopów do nowej strefy");
            orderItemRepository.save(item3_1);

            // Update order totals
            order3.updateProgress();
            orderRepository.save(order3);

            // Sample Order 4 - COMPLETED ORDER
            Order order4 = new Order("ORD-004", OrderType.PICK, manager);
            order4.setDescription("Kompletacja zamówienia e-commerce");
            order4.setPriority(OrderPriority.NORMAL);
            order4.setAssignedTo(worker);
            order4.setSourceLocation(location1);
            order4.setExpectedDate(LocalDateTime.now().minusHours(2));
            order4.setStartedAt(LocalDateTime.now().minusHours(3));
            order4.setCompletedAt(LocalDateTime.now().minusMinutes(30));
            order4.setStatus(OrderStatus.COMPLETED);
            order4.setNotes("Zamówienie zrealizowane zgodnie z planem");
            order4.setExternalReference("ECOM-2024-0456");
            order4 = orderRepository.save(order4);

            // Add completed order item
            OrderItem item4_1 = new OrderItem(order4, 1, product1, 2);
            item4_1.setSourceLocation(location1);
            item4_1.setCompletedQuantity(2);
            item4_1.setUnitPrice(new BigDecimal("2500.00"));
            item4_1.setStatus(OrderItemStatus.COMPLETED);
            item4_1.setPickedAt(LocalDateTime.now().minusHours(2));
            item4_1.setCompletedAt(LocalDateTime.now().minusMinutes(30));
            item4_1.setCompletionNotes("Zrealizowane bez problemów");
            orderItemRepository.save(item4_1);

            // Update order totals
            order4.updateProgress();
            orderRepository.save(order4);

            logger.info("✅ Created {} sample orders with order items", 4);

        } catch (Exception e) {
            logger.error("❌ Error creating sample orders: {}", e.getMessage(), e);
        }
    }
}