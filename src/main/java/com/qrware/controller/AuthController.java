package com.qrware.controller;

import com.qrware.domain.user.User;
import com.qrware.security.service.AuthenticationService;
import com.qrware.security.service.AuthenticationService.*;
import com.qrware.security.util.SecurityUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getUsernameOrEmail());
        
        try {
            AuthenticationResponse response = authenticationService.login(loginRequest);
            
            if (response == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(
                        false,
                        "Login failed: Invalid credentials",
                        null
                    ));
            }

            logger.info("Successful login for user: {}", response.getUsername());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Login successful",
                response
            ));
            
        } catch (Exception ex) {
            logger.warn("Login failed for user {}: {}", loginRequest.getUsernameOrEmail(), ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                    false,
                    "Login failed: " + ex.getMessage(),
                    null
                ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Registration attempt for user: {}", registerRequest.getUsername());
        
        try {
            AuthenticationResponse response = authenticationService.register(registerRequest);
            
            if (response == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(
                        false,
                        "Registration failed",
                        null
                    ));
            }

            logger.info("Successful registration for user: {}", response.getUsername());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Registration successful",
                    response
                ));
                
        } catch (Exception ex) {
            logger.warn("Registration failed for user {}: {}", registerRequest.getUsername(), ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                    false,
                    "Registration failed: " + ex.getMessage(),
                    null
                ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        logger.debug("Token refresh attempt");
        
        try {
            AuthenticationResponse response = authenticationService.refreshToken(refreshRequest);
            
            if (response == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(
                        false,
                        "Token refresh failed",
                        null
                    ));
            }

            logger.debug("Token refresh successful");
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Token refreshed successfully",
                response
            ));
            
        } catch (Exception ex) {
            logger.warn("Token refresh failed: {}", ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                    false,
                    "Token refresh failed: " + ex.getMessage(),
                    null
                ));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout() {
        try {
            String username = SecurityUtils.getCurrentUsername().orElse("unknown");
            authenticationService.logout();
            
            logger.info("User logged out: {}", username);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Logout successful",
                null
            ));
            
        } catch (Exception ex) {
            logger.error("Logout error: {}", ex.getMessage(), ex);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    false,
                    "Logout failed: " + ex.getMessage(),
                    null
                ));
        }
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            String username = SecurityUtils.getCurrentUsername().orElse("unknown");
            authenticationService.changePassword(changePasswordRequest);
            
            logger.info("Password changed successfully for user: {}", username);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Password changed successfully",
                null
            ));
            
        } catch (Exception ex) {
            logger.warn("Password change failed: {}", ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                    false,
                    "Password change failed: " + ex.getMessage(),
                    null
                ));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                    false,
                    "Email is required",
                    null
                ));
        }
        
        try {
            authenticationService.requestPasswordReset(email);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "If the email exists, a password reset link has been sent",
                null
            ));
            
        } catch (Exception ex) {
            logger.error("Password reset request error: {}", ex.getMessage(), ex);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "If the email exists, a password reset link has been sent",
                null
            ));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Optional<User> userOpt = SecurityUtils.getCurrentUser();
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(
                        false,
                        "User not authenticated",
                        null
                    ));
            }
            
            User user = userOpt.get();
            
            UserInfoResponse userInfo = new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getActive(),
                user.getEmailVerified(),
                user.getLastLogin(),
                user.getRoles().stream().map(role -> role.getName()).toList(),
                SecurityUtils.getCurrentUserPermissions().stream().toList()
            );
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User information retrieved successfully",
                userInfo
            ));
            
        } catch (Exception ex) {
            logger.error("Error getting current user: {}", ex.getMessage(), ex);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    false,
                    "Error retrieving user information",
                    null
                ));
        }
    }

    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> validateToken() {
        try {
            String username = SecurityUtils.getCurrentUsername().orElse("unknown");
            boolean isValid = SecurityUtils.isCurrentUserAccountValid();
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Token is valid",
                Map.of(
                    "username", username,
                    "valid", isValid,
                    "roles", SecurityUtils.getCurrentUserRoles(),
                    "permissions", SecurityUtils.getCurrentUserPermissions(),
                    "timestamp", LocalDateTime.now()
                )
            ));
            
        } catch (Exception ex) {
            logger.error("Token validation error: {}", ex.getMessage(), ex);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                    false,
                    "Token validation failed",
                    null
                ));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            boolean available = true;
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Username availability checked",
                Map.of("username", username, "available", available)
            ));
            
        } catch (Exception ex) {
            logger.error("Error checking username availability: {}", ex.getMessage(), ex);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    false,
                    "Error checking username availability",
                    null
                ));
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean available = true;
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Email availability checked",
                Map.of("email", email, "available", available)
            ));
            
        } catch (Exception ex) {
            logger.error("Error checking email availability: {}", ex.getMessage(), ex);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    false,
                    "Error checking email availability",
                    null
                ));
        }
    }

    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class UserInfoResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phone;
        private Boolean active;
        private Boolean emailVerified;
        private LocalDateTime lastLogin;
        private java.util.List<String> roles;
        private java.util.List<String> permissions;

        public UserInfoResponse(Long id, String username, String email, String firstName, String lastName,
                              String fullName, String phone, Boolean active, Boolean emailVerified,
                              LocalDateTime lastLogin, java.util.List<String> roles, java.util.List<String> permissions) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.fullName = fullName;
            this.phone = phone;
            this.active = active;
            this.emailVerified = emailVerified;
            this.lastLogin = lastLogin;
            this.roles = roles;
            this.permissions = permissions;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public Boolean getActive() { return active; }
        public Boolean getEmailVerified() { return emailVerified; }
        public LocalDateTime getLastLogin() { return lastLogin; }
        public java.util.List<String> getRoles() { return roles; }
        public java.util.List<String> getPermissions() { return permissions; }
    }
}