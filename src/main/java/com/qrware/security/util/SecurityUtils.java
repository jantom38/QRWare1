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

public final class SecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    private SecurityUtils() {
    }

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

    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    public static Optional<String> getCurrentUserFullName() {
        return getCurrentUser().map(User::getFullName);
    }

    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(User::getEmail);
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !authentication.getName().equals("anonymousUser");
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleToCheck = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals(roleToCheck));
    }

    public static boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAllRoles(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals(permission));
    }

    public static boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }

        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }

    public static Set<String> getCurrentUserRoles() {
        return getCurrentUserAuthorities().stream()
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(role -> role.substring(5)) 
            .collect(Collectors.toSet());
    }

    public static Set<String> getCurrentUserPermissions() {
        return getCurrentUserAuthorities().stream()
            .filter(authority -> !authority.startsWith("ROLE_"))
            .collect(Collectors.toSet());
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }

    public static boolean isWarehouseManager() {
        return hasRole("WAREHOUSE_MANAGER") || isAdmin();
    }

    public static boolean isWarehouseWorker() {
        return hasRole("WAREHOUSE_WORKER") || isWarehouseManager();
    }

    public static boolean canAccessUser(Long userId) {
        if (isAdmin()) {
            return true;
        }
        
        Optional<Long> currentUserId = getCurrentUserId();
        return currentUserId.isPresent() && currentUserId.get().equals(userId);
    }

    public static boolean canModifyUser(Long userId) {
        return canAccessUser(userId);
    }

    public static boolean canPerformInventoryOperations() {
        return hasAnyRole("WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN");
    }

    public static boolean canPerformAdminOperations() {
        return hasRole("ADMIN");
    }

    public static boolean canManageWarehouseConfig() {
        return hasAnyRole("WAREHOUSE_MANAGER", "ADMIN");
    }

    public static boolean canViewReports() {
        return hasAnyRole("WAREHOUSE_MANAGER", "ADMIN");
    }

    public static boolean canScanQRCodes() {
        return hasAnyRole("WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN");
    }

    public static boolean canGenerateQRCodes() {
        return hasAnyRole("WAREHOUSE_MANAGER", "ADMIN");
    }

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

    public static void logSecurityContext() {
        logger.debug("Security Context: {}", getSecurityContextInfo());
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        logger.debug("Security context cleared");
    }

    public static boolean isAuthenticatedRequest() {
        return isAuthenticated() && getCurrentUser().isPresent();
    }

    public static String getAuditUser() {
        return getCurrentUsername().orElse("system");
    }

    public static boolean isCurrentUserAccountValid() {
        return getCurrentUser()
            .map(user -> user.getActive() && user.isAccountNonLocked())
            .orElse(false);
    }

    public static int getCurrentUserFailedAttempts() {
        return getCurrentUser()
            .map(User::getFailedLoginAttempts)
            .orElse(0);
    }

    public static boolean isCurrentUserEmailVerified() {
        return getCurrentUser()
            .map(User::getEmailVerified)
            .orElse(false);
    }

    public static void requireAuthentication() {
        if (!isAuthenticated()) {
            throw new SecurityException("Authentication required");
        }
    }

    public static void requireRole(String role) {
        requireAuthentication();
        if (!hasRole(role)) {
            throw new SecurityException("Role required: " + role);
        }
    }

    public static void requireAnyRole(String... roles) {
        requireAuthentication();
        if (!hasAnyRole(roles)) {
            throw new SecurityException("One of the following roles required: " + String.join(", ", roles));
        }
    }

    public static void requirePermission(String permission) {
        requireAuthentication();
        if (!hasPermission(permission)) {
            throw new SecurityException("Permission required: " + permission);
        }
    }
}