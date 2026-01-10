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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> configureSecurityHeaders(headers))
            .authorizeHttpRequests(authz -> configureAuthorization(authz))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptions -> configureExceptionHandling(exceptions));

        return http.build();
    }

    private void configureAuthorization(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        authz
            .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/refresh", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/health", "/api/status").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/test/public").permitAll()
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .requestMatchers("/actuator/**").hasRole("ADMIN")
            .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-resources/**").permitAll()
            .requestMatchers("/static/**", "/assets/**", "/favicon.ico").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
            .requestMatchers("/api/users/**").hasAuthority("ADMIN_FULL")
            .requestMatchers("/api/roles/**").hasAuthority("ADMIN_FULL")
            .requestMatchers("/api/permissions/**").hasAuthority("ADMIN_FULL")
            .requestMatchers("/api/products/**").authenticated()
            .requestMatchers("/api/orders/**").authenticated()
            .requestMatchers("/api/order-items/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/categories/**").authenticated()
            .requestMatchers("/api/locations/**").authenticated()
            .requestMatchers("/api/zone/**").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/inventory/**").authenticated()
            .requestMatchers("/api/qr/**").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/movements/**").hasAnyRole("USER", "WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers("/api/audit/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers("/api/reports/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated();
    }

    private void configureSecurityHeaders(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<HttpSecurity> headers) {
        headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            .contentTypeOptions(contentTypeOptions -> contentTypeOptions.and())
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true))
            .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            .and()
            .addHeaderWriter((request, response) -> {
                response.setHeader("Permissions-Policy", 
                    "geolocation=(), microphone=(), camera=()");
            });
    }

    private void configureExceptionHandling(org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer<HttpSecurity> exceptions) {
        exceptions
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "X-Auth-Token", 
            "X-Total-Count", 
            "X-Page-Number",
            "X-Page-Size"
        ));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
