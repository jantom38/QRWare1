package com.qrware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Main application class for QRWare Warehouse Management System
 * 
 * Features:
 * - QR Code based inventory management
 * - Role-based access control
 * - Audit logging
 * - Asynchronous processing
 */
@SpringBootApplication
@EnableAsync
@EnableMethodSecurity(prePostEnabled = true)
public class WarehouseQRSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseQRSystemApplication.class, args);
    }
}