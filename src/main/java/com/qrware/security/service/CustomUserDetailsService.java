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

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Loading user by username/email: {}", usernameOrEmail);

        try {
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with username or email: " + usernameOrEmail));

            logger.debug("User found: id={}, username={}, active={}, emailVerified={}", 
                user.getId(), user.getUsername(), user.getActive(), user.getEmailVerified());

            if (!user.getActive()) {
                logger.warn("Attempt to authenticate inactive user: {}", user.getUsername());
                throw new UsernameNotFoundException("User account is inactive: " + usernameOrEmail);
            }

            if (!user.getEmailVerified()) {
                logger.warn("Attempt to authenticate user with unverified email: {}", user.getUsername());
            }

            if (!user.isAccountNonLocked()) {
                logger.warn("Attempt to authenticate locked user: {}, locked until: {}", 
                    user.getUsername(), user.getLockedUntil());
                throw new UsernameNotFoundException("User account is locked: " + usernameOrEmail);
            }

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

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        logger.debug("Loading user by ID: {}", id);

        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

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
        }
    }

    @Transactional
    public void handleFailedLogin(String usernameOrEmail) {
        try {
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElse(null);
            
            if (user != null) {
                user.incrementFailedAttempts();
                
                if (user.getFailedLoginAttempts() >= 5) {
                    user.lockAccount(30); 
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

    @Transactional(readOnly = true)
    public boolean userExists(String usernameOrEmail) {
        try {
            return userRepository.findByUsernameOrEmail(usernameOrEmail).isPresent();
        } catch (Exception ex) {
            logger.error("Error checking user existence for {}: {}", usernameOrEmail, ex.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElse(null);
    }

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

    @Transactional(readOnly = true)
    public boolean validateUserCredentials(String usernameOrEmail, String rawPassword) {
        try {
            UserDetails userDetails = loadUserByUsername(usernameOrEmail);
            return userDetails.isEnabled() && userDetails.isAccountNonLocked();
        } catch (UsernameNotFoundException ex) {
            return false;
        } catch (Exception ex) {
            logger.error("Error validating credentials for user {}: {}", usernameOrEmail, ex.getMessage());
            return false;
        }
    }

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

        public String getUsername() { return username; }
        public LocalDateTime getLastLogin() { return lastLogin; }
        public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
        public LocalDateTime getLockedUntil() { return lockedUntil; }
        public boolean isAccountNonLocked() { return accountNonLocked; }
        public boolean isActive() { return active; }
        public boolean isEmailVerified() { return emailVerified; }
    }
}