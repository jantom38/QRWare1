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

        addSecurityHeaders(response);

        try {
            if (isPublicEndpoint(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                processAuthentication(request, jwt);
            } else if (StringUtils.hasText(jwt)) {
                logger.warn("Invalid JWT token in request to: {}", request.getRequestURI());
                handleInvalidToken(response);
                return;
            }

        } catch (Exception ex) {
            logger.error("Cannot set user authentication: {}", ex.getMessage(), ex);
            handleAuthenticationException(response, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }


    private void processAuthentication(HttpServletRequest request, String jwt) {
        try {
            JwtTokenProvider.TokenType tokenType = tokenProvider.getTokenTypeFromToken(jwt);
            if (tokenType != JwtTokenProvider.TokenType.ACCESS) {
                logger.warn("Invalid token type for authentication: {}", tokenType);
                return;
            }

            String username = tokenProvider.getUsernameFromToken(jwt);

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                logger.warn("User account is disabled or locked: {}", username);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            logger.info("Setting SecurityContext for user: '{}'. Authorities: {}",
                    username, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("Successfully authenticated user: {} for request: {}",
                username, request.getRequestURI());

        } catch (Exception ex) {
            logger.error("Error processing JWT authentication: {}", ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }
    }


    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        String authToken = request.getHeader(X_AUTH_TOKEN_HEADER);
        if (StringUtils.hasText(authToken)) {
            return authToken;
        }

        String queryToken = request.getParameter("token");
        if (StringUtils.hasText(queryToken)) {
            return queryToken;
        }

        return null;
    }


    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        
        return PUBLIC_ENDPOINTS.stream()
            .anyMatch(endpoint -> requestURI.startsWith(endpoint) || requestURI.contains(endpoint));
    }


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


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        return path.endsWith(".css") ||
               path.endsWith(".js") || 

               path.endsWith(".gif") || 
               path.endsWith(".ico") ||
               path.startsWith("/static/") ||
               path.startsWith("/assets/");
    }



    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }


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


    private void checkTokenExpiration(String jwt) {
        try {
            long remainingSeconds = tokenProvider.getRemainingValidityInSeconds(jwt);
            if (remainingSeconds < 300) {
                logger.warn("JWT token expiring soon: {} seconds remaining", remainingSeconds);
            }
        } catch (Exception ex) {
            logger.debug("Could not check token expiration: {}", ex.getMessage());
        }
    }
}