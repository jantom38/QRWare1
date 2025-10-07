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

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Initializing default data...");
        
        createPermissions();
        createRoles();
        
        logger.info("Default data initialization completed");
    }

    private void createPermissions() {
        logger.info("Creating default permissions...");

        // User permissions
        createPermissionIfNotExists("USER_READ", "Read user information", "USER", "READ");
        createPermissionIfNotExists("USER_UPDATE", "Update user profile", "USER", "UPDATE");
        
        // Product permissions
        createPermissionIfNotExists("PRODUCT_READ", "Read product information", "PRODUCT", "READ");
        createPermissionIfNotExists("PRODUCT_CREATE", "Create new products", "PRODUCT", "CREATE");
        createPermissionIfNotExists("PRODUCT_UPDATE", "Update product information", "PRODUCT", "UPDATE");
        createPermissionIfNotExists("PRODUCT_DELETE", "Delete products", "PRODUCT", "DELETE");
        
        // Inventory permissions
        createPermissionIfNotExists("INVENTORY_READ", "Read inventory information", "INVENTORY", "READ");
        createPermissionIfNotExists("INVENTORY_CREATE", "Create inventory items", "INVENTORY", "CREATE");
        createPermissionIfNotExists("INVENTORY_UPDATE", "Update inventory", "INVENTORY", "UPDATE");
        createPermissionIfNotExists("INVENTORY_DELETE", "Delete inventory items", "INVENTORY", "DELETE");
        
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
            workerPermissions.add(getPermission("INVENTORY_CREATE"));
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
            managerPermissions.add(getPermission("PRODUCT_CREATE"));
            managerPermissions.add(getPermission("PRODUCT_UPDATE"));
            managerPermissions.add(getPermission("INVENTORY_READ"));
            managerPermissions.add(getPermission("INVENTORY_CREATE"));
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
            adminPermissions.add(getPermission("PRODUCT_CREATE"));
            adminPermissions.add(getPermission("PRODUCT_UPDATE"));
            adminPermissions.add(getPermission("PRODUCT_DELETE"));
            adminPermissions.add(getPermission("INVENTORY_READ"));
            adminPermissions.add(getPermission("INVENTORY_CREATE"));
            adminPermissions.add(getPermission("INVENTORY_UPDATE"));
            adminPermissions.add(getPermission("INVENTORY_DELETE"));
            adminPermissions.add(getPermission("QR_SCAN"));
            adminPermissions.add(getPermission("QR_GENERATE"));
            adminRole.setPermissions(adminPermissions);
            roleRepository.save(adminRole);
            logger.info("Created ADMIN role");
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
}