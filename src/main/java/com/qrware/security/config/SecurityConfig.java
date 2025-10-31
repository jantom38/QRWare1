package com.qrware.security.config;

import com.qrware.security.jwt.JwtAuthenticationFilter;
import com.qrware.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Main Security Configuration for QRWare system
 * Configures JWT-based authentication, authorization, CORS, and security headers
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    /**
     * Main Security Filter Chain Configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API (using JWT tokens)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management (stateless for JWT)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure security headers
            .headers(headers -> configureSecurityHeaders(headers))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> configureAuthorization(authz))
            
            // Configure authentication provider
            .authenticationProvider(authenticationProvider())
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configure exception handling
            .exceptionHandling(exceptions -> configureExceptionHandling(exceptions));

        return http.build();
    }

    /**
     * Configure authorization rules
     */
    private void configureAuthorization(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        authz
            // Public endpoints - no authentication required
            .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/refresh", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/health", "/api/status").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/test/public").permitAll()
                // H2 Console (development only)
            .requestMatchers("/h2-console/**").permitAll()

            // Actuator endpoints
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .requestMatchers("/actuator/**").hasRole("ADMIN")
            
            // Swagger/OpenAPI documentation
            .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-resources/**").permitAll()
            
            // Static resources
            .requestMatchers("/static/**", "/assets/**", "/favicon.ico").permitAll()
            
            // Authentication endpoints
            .requestMatchers("/api/auth/**").permitAll()
            
            // User management - only admins can manage users
            .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                .requestMatchers("/api/users/**").hasAuthority("ADMIN_FULL")
            .requestMatchers("/api/roles/**").hasAuthority("ADMIN_FULL")
            .requestMatchers("/api/permissions/**").hasAuthority("ADMIN_FULL")
            
            // Product management
            .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("USER", "WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
            
            // Category management
            .requestMatchers(HttpMethod.GET, "/api/categories/**").hasAnyRole("USER", "WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/categories/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
            
            // Warehouse management
            .requestMatchers(HttpMethod.GET, "/api/zones/**", "/api/locations/**").hasAnyRole("USER", "WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/zones/**", "/api/locations/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/zones/**", "/api/locations/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/zones/**", "/api/locations/**").hasRole("ADMIN")
            
            // Inventory management
          //  .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole("USER", "WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAuthority("ADMIN_FULL")

           //     .requestMatchers(HttpMethod.POST, "/api/inventory/receive").hasAnyRole("WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN")
           // .requestMatchers(HttpMethod.PUT, "/api/inventory/move").hasAnyRole("WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN")
            //.requestMatchers(HttpMethod.PUT, "/api/inventory/adjust").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            //.requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("ADMIN")
            
            // QR Code operations
            .requestMatchers(HttpMethod.GET, "/api/qr/**").hasAnyRole("USER", "WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/qr/scan").hasAnyRole("WAREHOUSE_WORKER", "WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/qr/generate").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            
            // Movement history and audit
            .requestMatchers(HttpMethod.GET, "/api/movements/**").hasAnyRole("USER", "WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers("/api/audit/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            
            // Reports
            .requestMatchers("/api/reports/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            
            // Administrative functions
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            
            // All other requests require authentication
            .anyRequest().authenticated();
    }

    /**
     * Configure security headers
     */
    private void configureSecurityHeaders(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<HttpSecurity> headers) {
        headers
            // Frame options
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            // Content type options
            .contentTypeOptions(contentTypeOptions -> contentTypeOptions.and())
            
            // XSS protection
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true))
            
            // Referrer policy
            .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            
            // Permissions policy
            .and()
            .addHeaderWriter((request, response) -> {
                response.setHeader("Permissions-Policy", 
                    "geolocation=(), microphone=(), camera=()");
            });
    }

    /**
     * Configure exception handling
     */
    private void configureExceptionHandling(org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer<HttpSecurity> exceptions) {
        exceptions
            // Handle authentication failures
            .authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                        "error": "UNAUTHORIZED",
                        "message": "Authentication required",
                        "status": 401,
                        "timestamp": "%s",
                        "path": "%s"
                    }
                    """.formatted(java.time.Instant.now(), request.getRequestURI()));
            })
            
            // Handle access denied
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                        "error": "FORBIDDEN",
                        "message": "Access denied: %s",
                        "status": 403,
                        "timestamp": "%s",
                        "path": "%s"
                    }
                    """.formatted(accessDeniedException.getMessage(), 
                                 java.time.Instant.now(), 
                                 request.getRequestURI()));
            });
    }

    /**
     * CORS Configuration
     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins from application properties
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);
        
        // Parse allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        
        // Parse allowed headers
        if ("*".equals(allowedHeaders)) {
            configuration.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            configuration.setAllowedHeaders(headers);
        }
        
        // Expose headers that clients can access
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "X-Auth-Token", 
            "X-Total-Count", 
            "X-Page-Number",
            "X-Page-Size"
        ));
        
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L); // Cache preflight response for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Password Encoder Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 for good security
    }

    /**
     * Authentication Provider Bean
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); // For better error messages
        return authProvider;
    }

    /**
     * Authentication Manager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}