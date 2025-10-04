package com.qrware.security.jwt;

import com.qrware.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT Authentication Filter that processes JWT tokens from HTTP requests
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_AUTH_TOKEN_HEADER = "X-Auth-Token";

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/health",
        "/api/status",
        "/h2-console",
        "/actuator",
        "/swagger-ui",
        "/api-docs",
        "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Add security headers to all responses
        addSecurityHeaders(response);

        try {
            // Skip authentication for public endpoints
            if (isPublicEndpoint(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Extract JWT token from request
            String jwt = getJwtFromRequest(request);

            // Process token if present and valid
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                processAuthentication(request, jwt);
            } else if (StringUtils.hasText(jwt)) {
                // Token is present but invalid
                logger.warn("Invalid JWT token in request to: {}", request.getRequestURI());
                handleInvalidToken(response);
                return;
            }
            // If no token and not public endpoint, Spring Security will handle unauthorized access

        } catch (Exception ex) {
            logger.error("Cannot set user authentication: {}", ex.getMessage(), ex);
            handleAuthenticationException(response, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Process JWT authentication
     */
    private void processAuthentication(HttpServletRequest request, String jwt) {
        try {
            // Verify token type (should be ACCESS token)
            JwtTokenProvider.TokenType tokenType = tokenProvider.getTokenTypeFromToken(jwt);
            if (tokenType != JwtTokenProvider.TokenType.ACCESS) {
                logger.warn("Invalid token type for authentication: {}", tokenType);
                return;
            }

            // Get username from token
            String username = tokenProvider.getUsernameFromToken(jwt);

            // Load user details
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Verify user is still active and not locked
            if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                logger.warn("User account is disabled or locked: {}", username);
                return;
            }

            // Create authentication token
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Log successful authentication for audit
            logger.debug("Successfully authenticated user: {} for request: {}", 
                username, request.getRequestURI());

        } catch (Exception ex) {
            logger.error("Error processing JWT authentication: {}", ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Extract JWT token from HTTP request
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Try Authorization header first (standard approach)
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // Try custom X-Auth-Token header (alternative approach)
        String authToken = request.getHeader(X_AUTH_TOKEN_HEADER);
        if (StringUtils.hasText(authToken)) {
            return authToken;
        }

        // Try query parameter (for special cases like WebSocket or file downloads)
        String queryToken = request.getParameter("token");
        if (StringUtils.hasText(queryToken)) {
            return queryToken;
        }

        return null;
    }

    /**
     * Check if the request is for a public endpoint
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        
        return PUBLIC_ENDPOINTS.stream()
            .anyMatch(endpoint -> requestURI.startsWith(endpoint) || requestURI.contains(endpoint));
    }

    /**
     * Handle invalid token response
     */
    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("""
            {
                "error": "INVALID_TOKEN",
                "message": "Invalid or expired JWT token",
                "status": 401,
                "timestamp": "%s"
            }
            """.formatted(java.time.Instant.now()));
    }

    /**
     * Handle authentication exception response
     */
    private void handleAuthenticationException(HttpServletResponse response, Exception ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("""
            {
                "error": "AUTHENTICATION_ERROR",
                "message": "Authentication failed: %s",
                "status": 401,
                "timestamp": "%s"
            }
            """.formatted(ex.getMessage(), java.time.Instant.now()));
    }

    /**
     * Determine if filter should be applied to this request
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Apply filter to all requests except static resources
        String path = request.getRequestURI();
        
        return path.endsWith(".css") || 
               path.endsWith(".js") || 
               path.endsWith(".png") || 
               path.endsWith(".jpg") || 
               path.endsWith(".jpeg") || 
               path.endsWith(".gif") || 
               path.endsWith(".ico") ||
               path.startsWith("/static/") ||
               path.startsWith("/assets/");
    }


    /**
     * Add security headers to response
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        // Prevent caching of sensitive responses
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Security headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    /**
     * Extract additional token information for logging/audit
     */
    private void logTokenInfo(String jwt, HttpServletRequest request) {
        try {
            JwtTokenProvider.TokenInfo tokenInfo = tokenProvider.getTokenInfo(jwt);
            if (tokenInfo != null) {
                logger.debug("Token info - User: {}, Expires: {}, Remaining: {}s, Endpoint: {}", 
                    tokenInfo.getUsername(),
                    tokenInfo.getExpiresAt(),
                    tokenInfo.getRemainingValiditySeconds(),
                    request.getRequestURI());
            }
        } catch (Exception ex) {
            logger.debug("Could not extract token info: {}", ex.getMessage());
        }
    }

    /**
     * Check if token is about to expire and log warning
     */
    private void checkTokenExpiration(String jwt) {
        try {
            long remainingSeconds = tokenProvider.getRemainingValidityInSeconds(jwt);
            if (remainingSeconds < 300) { // Less than 5 minutes
                logger.warn("JWT token expiring soon: {} seconds remaining", remainingSeconds);
            }
        } catch (Exception ex) {
            logger.debug("Could not check token expiration: {}", ex.getMessage());
        }
    }
}