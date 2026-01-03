package com.qrware.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.application.name:QRWare}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String version;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "QRWare system is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", applicationName);
        response.put("version", version);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", applicationName);
        response.put("version", version);
        response.put("timestamp", LocalDateTime.now());
        response.put("uptime", getUptime());
        
        Map<String, Object> system = new HashMap<>();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("javaVendor", System.getProperty("java.vendor"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("springProfile", System.getProperty("spring.profiles.active", "default"));
        response.put("system", system);
        
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemory", formatBytes(runtime.totalMemory()));
        memory.put("freeMemory", formatBytes(runtime.freeMemory()));
        memory.put("usedMemory", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
        memory.put("maxMemory", formatBytes(runtime.maxMemory()));
        response.put("memory", memory);
        
        response.put("database", getDatabaseStatus());
        
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/database")
    public ResponseEntity<?> databaseHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        Map<String, Object> dbStatus = getDatabaseStatus();
        response.putAll(dbStatus);
        
        if ("UP".equals(dbStatus.get("status"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/version")
    public ResponseEntity<?> version() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", applicationName);
        response.put("version", version);
        response.put("buildTime", "2024-01-01T00:00:00");
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("springBootVersion", getSpringBootVersion());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/environment")
    public ResponseEntity<?> environment() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("profile", System.getProperty("spring.profiles.active", "default"));
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("osName", System.getProperty("os.name"));
        response.put("timeZone", System.getProperty("user.timezone"));
        response.put("encoding", System.getProperty("file.encoding"));
        
        Map<String, Object> server = new HashMap<>();
        server.put("port", System.getProperty("server.port", "8080"));
        server.put("contextPath", System.getProperty("server.servlet.context-path", "/"));
        response.put("server", server);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<?> ready() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        Map<String, Object> dbStatus = getDatabaseStatus();
        boolean dbReady = "UP".equals(dbStatus.get("status"));
        
        if (dbReady) {
            response.put("status", "READY");
            response.put("message", "Application is ready to serve requests");
            response.put("database", dbStatus);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "NOT_READY");
            response.put("message", "Application is not ready");
            response.put("database", dbStatus);
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/live")
    public ResponseEntity<?> live() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ALIVE");
        response.put("message", "Application is alive");
        response.put("timestamp", LocalDateTime.now());
        response.put("uptime", getUptime());
        
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> getDatabaseStatus() {
        Map<String, Object> dbStatus = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            
            dbStatus.put("status", isValid ? "UP" : "DOWN");
            dbStatus.put("url", connection.getMetaData().getURL());
            dbStatus.put("driverName", connection.getMetaData().getDriverName());
            dbStatus.put("driverVersion", connection.getMetaData().getDriverVersion());
            dbStatus.put("productName", connection.getMetaData().getDatabaseProductName());
            dbStatus.put("productVersion", connection.getMetaData().getDatabaseProductVersion());
            
            if (isValid) {
                dbStatus.put("message", "Database connection is healthy");
            } else {
                dbStatus.put("message", "Database connection validation failed");
            }
            
        } catch (Exception ex) {
            dbStatus.put("status", "DOWN");
            dbStatus.put("message", "Database connection failed: " + ex.getMessage());
            dbStatus.put("error", ex.getClass().getSimpleName());
        }
        
        return dbStatus;
    }

    private String getUptime() {
        long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        return String.format("%d days, %d hours, %d minutes, %d seconds", 
            days, hours % 24, minutes % 60, seconds % 60);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private String getSpringBootVersion() {
        try {
            Package pkg = org.springframework.boot.SpringBootVersion.class.getPackage();
            return pkg != null ? pkg.getImplementationVersion() : "unknown";
        } catch (Exception ex) {
            return "unknown";
        }
    }
}