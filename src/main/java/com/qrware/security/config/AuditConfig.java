package com.qrware.security.config;

import com.qrware.domain.user.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;


@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {


    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SecurityAuditorAware();
    }


    public static class SecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            if (authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                return Optional.of(user.getUsername());
            }

            if (authentication.getPrincipal() instanceof String) {
                return Optional.of((String) authentication.getPrincipal());
            }

            String name = authentication.getName();
            if (name != null && !name.equals("anonymousUser")) {
                return Optional.of(name);
            }

            return Optional.of("system");
        }
    }
}