package com.qrware.security.util;

import com.qrware.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for security-related operations
 */
public final class SecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Get the current authenticated user
     */
    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }

        return Optional.empty();
    }

    /**
     * Get the current authenticated username
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        } else {
            username = authentication.getName();
        }

        if (username != null && !username.equals("anonymousUser")) {
            return Optional.of(username);
        }

        return Optional.empty();
    }

    /**
     * Get the current authenticated user ID
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    /**
     * Get the current authenticated user's full name
     */
    public static Optional<String> getCurrentUserFullName() {
        return getCurrentUser().map(User::getFullName);
    }

    /**
     * Get the current authenticated user's email
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(User::getEmail);
    }

    /**
     * Check if current user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !authentication.getName().equals("anonymousUser");
    }

    /**
     * Check if current user has a specific role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Ensure role has ROLE_ prefix
        String roleToCheck = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals(roleToCheck));
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has all specified roles
     */
    public static boolean hasAllRoles(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if current user has a specific permission
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals(permission));
    }

    /**
     * Check if current user has any of the specified permissions
     */
    public static boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all authorities/permissions of current user
     */
    public static Set<String> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }

        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }

    /**
     * Get all roles of current user
     */
    public static Set<String> getCurrentUserRoles() {
        return getCurrentUserAuthorities().stream()
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(role -> role.substring(5)) // Remove "ROLE_" prefix
            .collect(Collectors.toSet());
    }

    /**
     * Get all permissions of current user (non-role authorities)
     */
    public static Set<String> getCurrentUserPermissions() {
        return getCurrentUserAuthorities().stream()
            .filter(authority -> !authority.startsWith("ROLE_"))
            .collect(Collectors.toSet());
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }

    /**
     * Check if current user is warehouse manager
     */
    public static boolean isWarehouseManager() {
        return hasRole("WAREHOUSE_MANAGER") || isAdmin();
    }

    /**
     * Check if current user is warehouse worker
     */
    public static boolean isWarehouseWorker() {
        return hasRole("WAREHOUSE_WORKER") || isWarehouseManager();
    }

    /**
     * Check if current user can access user with given ID
     * Users can access their own data, admins can access all
     */
    public static boolean canAccessUser(Long userId) {
        if (isAdmin()) {
            return true;
        }
        
        Optional<Long> currentUserId = getCurrentUserId();
        return currentUserId.isPresent() && currentUserId.get().equals(userId);
    }

    /**
     * Check if current user can modify user with given ID
     * Similar to canAccessUser but might have different rules
     */
    public static boolean canModifyUser(Long userId) {
        return canAccessUser(userId);
    }

    /**
     * Check if current user can perform inventory operations
     */
    public static boolean canPerformInventoryOperations() {
        return hasAnyRole("WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN");
    }

    /**
     * Check if current user can perform admin operations
     */
    public static boolean canPerformAdminOperations() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user can manage warehouse configuration
     */
    public static boolean canManageWarehouseConfig() {
        return hasAnyRole("WAREHOUSE_MANAGER", "ADMIN");
    }

    /**
     * Check if current user can view reports
     */
    public static boolean canViewReports() {
        return hasAnyRole("WAREHOUSE_MANAGER", "ADMIN");
    }

    /**
     * Check if current user can scan QR codes
     */
    public static boolean canScanQRCodes() {
        return hasAnyRole("WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN");
    }

    /**
     * Check if current user can generate QR codes
     */
    public static boolean canGenerateQRCodes() {
        return hasAnyRole("WAREHOUSE_MANAGER", "ADMIN");
    }

    /**
     * Get security context information for logging/debugging
     */
    public static String getSecurityContextInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return "No authentication";
        }

        StringBuilder info = new StringBuilder();
        info.append("Principal: ").append(authentication.getPrincipal().getClass().getSimpleName());
        info.append(", Name: ").append(authentication.getName());
        info.append(", Authenticated: ").append(authentication.isAuthenticated());
        info.append(", Authorities: ").append(
            authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "))
        );

        return info.toString();
    }

    /**
     * Log current security context (for debugging)
     */
    public static void logSecurityContext() {
        logger.debug("Security Context: {}", getSecurityContextInfo());
    }

    /**
     * Clear security context
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        logger.debug("Security context cleared");
    }

    /**
     * Check if current request is from an authenticated source
     */
    public static boolean isAuthenticatedRequest() {
        return isAuthenticated() && getCurrentUser().isPresent();
    }

    /**
     * Get user identifier for audit purposes
     */
    public static String getAuditUser() {
        return getCurrentUsername().orElse("system");
    }

    /**
     * Check if current user account is active and not locked
     */
    public static boolean isCurrentUserAccountValid() {
        return getCurrentUser()
            .map(user -> user.getActive() && user.isAccountNonLocked())
            .orElse(false);
    }

    /**
     * Get current user's failed login attempts
     */
    public static int getCurrentUserFailedAttempts() {
        return getCurrentUser()
            .map(User::getFailedLoginAttempts)
            .orElse(0);
    }

    /**
     * Check if current user's email is verified
     */
    public static boolean isCurrentUserEmailVerified() {
        return getCurrentUser()
            .map(User::getEmailVerified)
            .orElse(false);
    }

    /**
     * Require authentication - throw exception if not authenticated
     */
    public static void requireAuthentication() {
        if (!isAuthenticated()) {
            throw new SecurityException("Authentication required");
        }
    }

    /**
     * Require specific role - throw exception if user doesn't have role
     */
    public static void requireRole(String role) {
        requireAuthentication();
        if (!hasRole(role)) {
            throw new SecurityException("Role required: " + role);
        }
    }

    /**
     * Require any of specified roles
     */
    public static void requireAnyRole(String... roles) {
        requireAuthentication();
        if (!hasAnyRole(roles)) {
            throw new SecurityException("One of the following roles required: " + String.join(", ", roles));
        }
    }

    /**
     * Require specific permission
     */
    public static void requirePermission(String permission) {
        requireAuthentication();
        if (!hasPermission(permission)) {
            throw new SecurityException("Permission required: " + permission);
        }
    }
}