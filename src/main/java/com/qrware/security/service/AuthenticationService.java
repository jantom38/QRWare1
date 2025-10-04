package com.qrware.security.service;

import com.qrware.domain.user.Role;
import com.qrware.domain.user.User;
import com.qrware.repository.user.RoleRepository;
import com.qrware.repository.user.UserRepository;
import com.qrware.security.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Authentication Service for handling login, registration, and token management
 */
@Service
@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Authenticate user and generate JWT tokens
     */
    public AuthenticationResponse login(LoginRequest loginRequest) {
        logger.info("Attempting login for user: {}", loginRequest.getUsernameOrEmail());

        try {
            // Validate input
            if (loginRequest.getUsernameOrEmail() == null || loginRequest.getUsernameOrEmail().trim().isEmpty()) {
                throw new BadCredentialsException("Username or email is required");
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                throw new BadCredentialsException("Password is required");
            }

            // Find user first to check account status
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

            // Check if user account is active
            if (!user.getActive()) {
                userDetailsService.handleFailedLogin(loginRequest.getUsernameOrEmail());
                throw new DisabledException("User account is disabled");
            }

            // Check if user account is locked
            if (!user.isAccountNonLocked()) {
                throw new LockedException("User account is locked until: " + user.getLockedUntil());
            }

            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
                )
            );

            // Generate tokens
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user);

            // Update user login information
            userDetailsService.updateLastLogin(user.getUsername());

            // Set security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("Successful login for user: {}", user.getUsername());

            return new AuthenticationResponse(
                accessToken,
                refreshToken,
                tokenProvider.getRemainingValidityInSeconds(accessToken),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles().stream().map(Role::getName).toList()
            );

        } catch (BadCredentialsException | DisabledException | LockedException ex) {
            logger.warn("Authentication failed for user {}: {}", loginRequest.getUsernameOrEmail(), ex.getMessage());
            userDetailsService.handleFailedLogin(loginRequest.getUsernameOrEmail());
            throw ex;
        } catch (AuthenticationException ex) {
            logger.warn("Authentication failed for user {}: {}", loginRequest.getUsernameOrEmail(), ex.getMessage());
            userDetailsService.handleFailedLogin(loginRequest.getUsernameOrEmail());
            throw new BadCredentialsException("Invalid username/email or password");
        } catch (Exception ex) {
            logger.error("Login error for user {}: {}", loginRequest.getUsernameOrEmail(), ex.getMessage(), ex);
            throw new RuntimeException("Login failed due to system error");
        }
    }

    /**
     * Register new user
     */
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        logger.info("Attempting registration for user: {}", registerRequest.getUsername());

        try {
            // Validate input
            validateRegisterRequest(registerRequest);

            // Check if username already exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                throw new IllegalArgumentException("Username is already taken");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new IllegalArgumentException("Email is already registered");
            }

            // Create new user
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setPhone(registerRequest.getPhone());
            user.setActive(true);
            user.setEmailVerified(false); // Require email verification

            // Assign default role
            Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(defaultRole);
            user.setRoles(roles);

            // Save user
            User savedUser = userRepository.save(user);

            logger.info("User registered successfully: {}", savedUser.getUsername());

            // Auto-login after registration
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsernameOrEmail(registerRequest.getUsername());
            loginRequest.setPassword(registerRequest.getPassword());

            return login(loginRequest);

        } catch (IllegalArgumentException ex) {
            logger.warn("Registration failed for user {}: {}", registerRequest.getUsername(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Registration error for user {}: {}", registerRequest.getUsername(), ex.getMessage(), ex);
            throw new RuntimeException("Registration failed due to system error");
        }
    }

    /**
     * Refresh JWT token
     */
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshRequest) {
        logger.debug("Attempting token refresh");

        try {
            String refreshToken = refreshRequest.getRefreshToken();

            // Validate refresh token
            if (!tokenProvider.validateToken(refreshToken)) {
                throw new BadCredentialsException("Invalid refresh token");
            }

            // Check if it's actually a refresh token
            if (tokenProvider.getTokenTypeFromToken(refreshToken) != JwtTokenProvider.TokenType.REFRESH) {
                throw new BadCredentialsException("Invalid token type for refresh");
            }

            // Get user from token
            String username = tokenProvider.getUsernameFromToken(refreshToken);
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

            // Check if user is still active
            if (!user.getActive() || !user.isAccountNonLocked()) {
                throw new DisabledException("User account is disabled or locked");
            }

            // Generate new tokens
            String newAccessToken = tokenProvider.generateTokenFromUser(user);
            String newRefreshToken = tokenProvider.generateRefreshToken(user);

            logger.debug("Token refreshed successfully for user: {}", username);

            return new AuthenticationResponse(
                newAccessToken,
                newRefreshToken,
                tokenProvider.getRemainingValidityInSeconds(newAccessToken),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles().stream().map(Role::getName).toList()
            );

        } catch (Exception ex) {
            logger.warn("Token refresh failed: {}", ex.getMessage());
            throw new BadCredentialsException("Token refresh failed");
        }
    }

    /**
     * Logout user (invalidate token on client side)
     */
    public void logout() {
        // Clear security context
        SecurityContextHolder.clearContext();
        logger.debug("User logged out successfully");
        
        // Note: For complete logout, you might want to maintain a blacklist of tokens
        // or use a token store like Redis to track valid tokens
    }

    /**
     * Change user password
     */
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to change password");
        }

        User user = (User) authentication.getPrincipal();
        
        try {
            // Verify current password
            if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
                throw new BadCredentialsException("Current password is incorrect");
            }

            // Validate new password
            validatePassword(changePasswordRequest.getNewPassword());

            // Update password
            user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            userRepository.save(user);

            logger.info("Password changed successfully for user: {}", user.getUsername());

        } catch (Exception ex) {
            logger.warn("Password change failed for user {}: {}", user.getUsername(), ex.getMessage());
            throw ex;
        }
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email) {
        logger.info("Password reset requested for email: {}", email);

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                // Don't reveal if email exists or not
                logger.warn("Password reset requested for non-existent email: {}", email);
                return;
            }

            User user = userOpt.get();
            
            // Generate reset token (in real implementation, store this and send via email)
            String resetToken = UUID.randomUUID().toString();
            
            // Here you would:
            // 1. Store the reset token with expiration
            // 2. Send email with reset link
            // For now, just log it
            
            logger.info("Password reset token generated for user {}: {}", user.getUsername(), resetToken);

        } catch (Exception ex) {
            logger.error("Error processing password reset request for email {}: {}", email, ex.getMessage(), ex);
        }
    }

    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }

        return null;
    }

    /**
     * Validate registration request
     */
    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        validatePassword(request.getPassword());
    }

    /**
     * Validate password strength
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }

    /**
     * Login request DTO
     */
    public static class LoginRequest {
        private String usernameOrEmail;
        private String password;

        // Getters and setters
        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * Registration request DTO
     */
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phone;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    /**
     * Refresh token request DTO
     */
    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    /**
     * Change password request DTO
     */
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    /**
     * Authentication response DTO
     */
    public static class AuthenticationResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private Long expiresIn;
        private Long userId;
        private String username;
        private String email;
        private String fullName;
        private java.util.List<String> roles;

        public AuthenticationResponse(String accessToken, String refreshToken, Long expiresIn,
                                    Long userId, String username, String email, String fullName,
                                    java.util.List<String> roles) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.roles = roles;
        }

        // Getters
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getTokenType() { return tokenType; }
        public Long getExpiresIn() { return expiresIn; }
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public java.util.List<String> getRoles() { return roles; }
    }
}