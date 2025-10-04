package com.qrware.security.config;

import com.qrware.domain.user.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Audit Configuration for automatic tracking of entity creation and modification
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /**
     * Auditor provider that gets current authenticated user for audit fields
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SecurityAuditorAware();
    }

    /**
     * Implementation of AuditorAware that extracts username from Security Context
     */
    public static class SecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            // If principal is User object
            if (authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                return Optional.of(user.getUsername());
            }

            // If principal is username string
            if (authentication.getPrincipal() instanceof String) {
                return Optional.of((String) authentication.getPrincipal());
            }

            // If authenticated but principal is not recognized
            String name = authentication.getName();
            if (name != null && !name.equals("anonymousUser")) {
                return Optional.of(name);
            }

            return Optional.of("system");
        }
    }
}