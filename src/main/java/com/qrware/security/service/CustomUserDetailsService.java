package com.qrware.security.service;

import com.qrware.domain.user.User;
import com.qrware.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Custom UserDetailsService implementation for Spring Security
 * Loads user-specific data and handles user authentication state
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by username for authentication
     * Supports both username and email as login identifiers
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Loading user by username/email: {}", usernameOrEmail);

        try {
            // Find user by username or email
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with username or email: " + usernameOrEmail));

            // Log user loading for audit (without sensitive information)
            logger.debug("User found: id={}, username={}, active={}, emailVerified={}", 
                user.getId(), user.getUsername(), user.getActive(), user.getEmailVerified());

            // Check if user account is active
            if (!user.getActive()) {
                logger.warn("Attempt to authenticate inactive user: {}", user.getUsername());
                throw new UsernameNotFoundException("User account is inactive: " + usernameOrEmail);
            }

            // Check if user email is verified (if required)
            if (!user.getEmailVerified()) {
                logger.warn("Attempt to authenticate user with unverified email: {}", user.getUsername());
                // Note: You might want to allow login but restrict certain operations
                // For now, we'll allow login but this can be configured
            }

            // Check if user account is locked
            if (!user.isAccountNonLocked()) {
                logger.warn("Attempt to authenticate locked user: {}, locked until: {}", 
                    user.getUsername(), user.getLockedUntil());
                throw new UsernameNotFoundException("User account is locked: " + usernameOrEmail);
            }

            // Load user roles and permissions (already done by @ManyToMany with EAGER fetch)
            logger.debug("User {} has {} roles with total permissions", 
                user.getUsername(), user.getRoles().size());

            return user;

        } catch (UsernameNotFoundException ex) {
            logger.warn("User not found: {}", usernameOrEmail);
            throw ex;
        } catch (Exception ex) {
            logger.error("Error loading user {}: {}", usernameOrEmail, ex.getMessage(), ex);
            throw new UsernameNotFoundException("Error loading user: " + usernameOrEmail, ex);
        }
    }

    /**
     * Load user by ID (useful for token-based authentication)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        logger.debug("Loading user by ID: {}", id);

        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

            // Same validation as loadUserByUsername
            if (!user.getActive()) {
                throw new UsernameNotFoundException("User account is inactive: " + id);
            }

            if (!user.isAccountNonLocked()) {
                throw new UsernameNotFoundException("User account is locked: " + id);
            }

            logger.debug("User loaded by ID: username={}, active={}", user.getUsername(), user.getActive());
            return user;

        } catch (UsernameNotFoundException ex) {
            logger.warn("User not found by ID: {}", id);
            throw ex;
        } catch (Exception ex) {
            logger.error("Error loading user by ID {}: {}", id, ex.getMessage(), ex);
            throw new UsernameNotFoundException("Error loading user by ID: " + id, ex);
        }
    }

    /**
     * Update user last login time (called after successful authentication)
     */
    @Transactional
    public void updateLastLogin(String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElse(null);
            
            if (user != null) {
                user.updateLastLogin();
                user.resetFailedAttempts();
                userRepository.save(user);
                
                logger.debug("Updated last login for user: {}", username);
            }
        } catch (Exception ex) {
            logger.error("Error updating last login for user {}: {}", username, ex.getMessage(), ex);
            // Don't throw exception here as it's not critical for authentication
        }
    }

    /**
     * Handle failed login attempt
     */
    @Transactional
    public void handleFailedLogin(String usernameOrEmail) {
        try {
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElse(null);
            
            if (user != null) {
                user.incrementFailedAttempts();
                
                // Lock account after 5 failed attempts
                if (user.getFailedLoginAttempts() >= 5) {
                    user.lockAccount(30); // Lock for 30 minutes
                    logger.warn("User account locked due to failed login attempts: {}", user.getUsername());
                }
                
                userRepository.save(user);
                
                logger.debug("Recorded failed login attempt for user: {}, total attempts: {}", 
                    user.getUsername(), user.getFailedLoginAttempts());
            }
        } catch (Exception ex) {
            logger.error("Error handling failed login for user {}: {}", usernameOrEmail, ex.getMessage(), ex);
        }
    }

    /**
     * Check if user exists by username or email
     */
    @Transactional(readOnly = true)
    public boolean userExists(String usernameOrEmail) {
        try {
            return userRepository.findByUsernameOrEmail(usernameOrEmail).isPresent();
        } catch (Exception ex) {
            logger.error("Error checking user existence for {}: {}", usernameOrEmail, ex.getMessage());
            return false;
        }
    }

    /**
     * Get user by username (without password for security)
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElse(null);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElse(null);
    }

    /**
     * Unlock user account manually
     */
    @Transactional
    public boolean unlockUserAccount(String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElse(null);
            
            if (user != null && !user.isAccountNonLocked()) {
                user.unlockAccount();
                userRepository.save(user);
                
                logger.info("User account unlocked manually: {}", username);
                return true;
            }
            return false;
        } catch (Exception ex) {
            logger.error("Error unlocking user account {}: {}", username, ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Reset failed login attempts for user
     */
    @Transactional
    public void resetFailedAttempts(String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElse(null);
            
            if (user != null) {
                user.resetFailedAttempts();
                userRepository.save(user);
                
                logger.debug("Reset failed login attempts for user: {}", username);
            }
        } catch (Exception ex) {
            logger.error("Error resetting failed attempts for user {}: {}", username, ex.getMessage(), ex);
        }
    }

    /**
     * Check if user has specific role
     */
    @Transactional(readOnly = true)
    public boolean userHasRole(String username, String roleName) {
        try {
            User user = userRepository.findByUsername(username)
                .orElse(null);
            
            return user != null && user.hasRole(roleName);
        } catch (Exception ex) {
            logger.error("Error checking role for user {}: {}", username, ex.getMessage());
            return false;
        }
    }

    /**
     * Check if user has specific permission
     */
    @Transactional(readOnly = true)
    public boolean userHasPermission(String username, String permissionName) {
        try {
            User user = userRepository.findByUsername(username)
                .orElse(null);
            
            return user != null && user.hasPermission(permissionName);
        } catch (Exception ex) {
            logger.error("Error checking permission for user {}: {}", username, ex.getMessage());
            return false;
        }
    }

    /**
     * Validate user credentials without full authentication
     */
    @Transactional(readOnly = true)
    public boolean validateUserCredentials(String usernameOrEmail, String rawPassword) {
        try {
            UserDetails userDetails = loadUserByUsername(usernameOrEmail);
            // Note: This would need a PasswordEncoder to compare properly
            // For now, we just check if user exists and is valid
            return userDetails.isEnabled() && userDetails.isAccountNonLocked();
        } catch (UsernameNotFoundException ex) {
            return false;
        } catch (Exception ex) {
            logger.error("Error validating credentials for user {}: {}", usernameOrEmail, ex.getMessage());
            return false;
        }
    }

    /**
     * Get user authentication statistics
     */
    @Transactional(readOnly = true)
    public UserAuthStats getUserAuthStats(String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElse(null);
            
            if (user == null) {
                return null;
            }
            
            return new UserAuthStats(
                user.getUsername(),
                user.getLastLogin(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.isAccountNonLocked(),
                user.getActive(),
                user.getEmailVerified()
            );
        } catch (Exception ex) {
            logger.error("Error getting auth stats for user {}: {}", username, ex.getMessage());
            return null;
        }
    }

    /**
     * User authentication statistics class
     */
    public static class UserAuthStats {
        private final String username;
        private final LocalDateTime lastLogin;
        private final Integer failedLoginAttempts;
        private final LocalDateTime lockedUntil;
        private final boolean accountNonLocked;
        private final boolean active;
        private final boolean emailVerified;

        public UserAuthStats(String username, LocalDateTime lastLogin, Integer failedLoginAttempts,
                           LocalDateTime lockedUntil, boolean accountNonLocked, boolean active, boolean emailVerified) {
            this.username = username;
            this.lastLogin = lastLogin;
            this.failedLoginAttempts = failedLoginAttempts;
            this.lockedUntil = lockedUntil;
            this.accountNonLocked = accountNonLocked;
            this.active = active;
            this.emailVerified = emailVerified;
        }

        // Getters
        public String getUsername() { return username; }
        public LocalDateTime getLastLogin() { return lastLogin; }
        public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
        public LocalDateTime getLockedUntil() { return lockedUntil; }
        public boolean isAccountNonLocked() { return accountNonLocked; }
        public boolean isActive() { return active; }
        public boolean isEmailVerified() { return emailVerified; }
    }
}