package com.qrware.controller;

import com.qrware.security.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Test Controller for verifying system functionality
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    /**
     * Public endpoint - no authentication required
     */
    @GetMapping("/public")
    public ResponseEntity<?> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Public endpoint working!");
        response.put("timestamp", LocalDateTime.now());
        response.put("authenticated", SecurityUtils.isAuthenticated());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Protected endpoint - authentication required
     */
    @GetMapping("/protected")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> protectedEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Protected endpoint working!");
        response.put("timestamp", LocalDateTime.now());
        response.put("user", SecurityUtils.getCurrentUsername().orElse("unknown"));
        response.put("userId", SecurityUtils.getCurrentUserId().orElse(null));
        response.put("roles", SecurityUtils.getCurrentUserRoles());
        response.put("permissions", SecurityUtils.getCurrentUserPermissions());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Admin only endpoint
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin endpoint working!");
        response.put("timestamp", LocalDateTime.now());
        response.put("user", SecurityUtils.getCurrentUsername().orElse("unknown"));
        response.put("isAdmin", SecurityUtils.isAdmin());
        response.put("securityContext", SecurityUtils.getSecurityContextInfo());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Warehouse worker endpoint
     */
    @GetMapping("/warehouse")
    @PreAuthorize("hasAnyRole('WAREHOUSE_WORKER', 'WAREHOUSE_MANAGER', 'ADMIN')")
    public ResponseEntity<?> warehouseEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Warehouse endpoint working!");
        response.put("timestamp", LocalDateTime.now());
        response.put("user", SecurityUtils.getCurrentUsername().orElse("unknown"));
        response.put("canPerformInventoryOps", SecurityUtils.canPerformInventoryOperations());
        response.put("canScanQR", SecurityUtils.canScanQRCodes());
        response.put("canGenerateQR", SecurityUtils.canGenerateQRCodes());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Manager endpoint
     */
    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
    public ResponseEntity<?> managerEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Manager endpoint working!");
        response.put("timestamp", LocalDateTime.now());
        response.put("user", SecurityUtils.getCurrentUsername().orElse("unknown"));
        response.put("canManageWarehouse", SecurityUtils.canManageWarehouseConfig());
        response.put("canViewReports", SecurityUtils.canViewReports());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test JWT token info
     */
    @GetMapping("/token-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> tokenInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Token information");
        response.put("timestamp", LocalDateTime.now());
        response.put("authenticated", SecurityUtils.isAuthenticated());
        response.put("username", SecurityUtils.getCurrentUsername().orElse(null));
        response.put("userId", SecurityUtils.getCurrentUserId().orElse(null));
        response.put("userEmail", SecurityUtils.getCurrentUserEmail().orElse(null));
        response.put("userFullName", SecurityUtils.getCurrentUserFullName().orElse(null));
        response.put("roles", SecurityUtils.getCurrentUserRoles());
        response.put("permissions", SecurityUtils.getCurrentUserPermissions());
        response.put("authorities", SecurityUtils.getCurrentUserAuthorities());
        response.put("accountValid", SecurityUtils.isCurrentUserAccountValid());
        response.put("emailVerified", SecurityUtils.isCurrentUserEmailVerified());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test role permissions
     */
    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> testPermissions() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Permission check results");
        response.put("timestamp", LocalDateTime.now());
        response.put("user", SecurityUtils.getCurrentUsername().orElse("unknown"));
        
        // Test various role checks
        Map<String, Boolean> roleChecks = new HashMap<>();
        roleChecks.put("hasRoleUser", SecurityUtils.hasRole("USER"));
        roleChecks.put("hasRoleWarehouseWorker", SecurityUtils.hasRole("WAREHOUSE_WORKER"));
        roleChecks.put("hasRoleWarehouseManager", SecurityUtils.hasRole("WAREHOUSE_MANAGER"));
        roleChecks.put("hasRoleAdmin", SecurityUtils.hasRole("ADMIN"));
        response.put("roleChecks", roleChecks);
        
        // Test capability checks
        Map<String, Boolean> capabilityChecks = new HashMap<>();
        capabilityChecks.put("isAdmin", SecurityUtils.isAdmin());
        capabilityChecks.put("isWarehouseManager", SecurityUtils.isWarehouseManager());
        capabilityChecks.put("isWarehouseWorker", SecurityUtils.isWarehouseWorker());
        capabilityChecks.put("canPerformInventoryOps", SecurityUtils.canPerformInventoryOperations());
        capabilityChecks.put("canPerformAdminOps", SecurityUtils.canPerformAdminOperations());
        capabilityChecks.put("canManageWarehouseConfig", SecurityUtils.canManageWarehouseConfig());
        capabilityChecks.put("canViewReports", SecurityUtils.canViewReports());
        capabilityChecks.put("canScanQRCodes", SecurityUtils.canScanQRCodes());
        capabilityChecks.put("canGenerateQRCodes", SecurityUtils.canGenerateQRCodes());
        response.put("capabilityChecks", capabilityChecks);
        
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    /**
     * Test CORS
     */
    @GetMapping("/cors")
    public ResponseEntity<?> testCors() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS test endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("authenticated", SecurityUtils.isAuthenticated());
        response.put("note", "If you can see this from a different origin, CORS is working");
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test POST endpoint
     */
    @PostMapping("/echo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> echo(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Echo endpoint working!");
        response.put("timestamp", LocalDateTime.now());
        response.put("user", SecurityUtils.getCurrentUsername().orElse("unknown"));
        response.put("receivedPayload", payload);
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test error handling
     */
    @GetMapping("/error")
    public ResponseEntity<?> testError() {
        throw new RuntimeException("Test error for error handling verification");
    }

    /**
     * Test unauthorized access
     */
    @GetMapping("/unauthorized")
    @PreAuthorize("hasRole('NONEXISTENT_ROLE')")
    public ResponseEntity<?> testUnauthorized() {
        return ResponseEntity.ok("This should never be reached");
    }

    /**
     * System information
     */
    @GetMapping("/system-info")
    public ResponseEntity<?> systemInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "QRWare System Information");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("springProfile", System.getProperty("spring.profiles.active", "default"));
        response.put("authenticated", SecurityUtils.isAuthenticated());
        
        if (SecurityUtils.isAuthenticated()) {
            response.put("currentUser", SecurityUtils.getCurrentUsername().orElse("unknown"));
            response.put("userRoles", SecurityUtils.getCurrentUserRoles());
        }
        
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}