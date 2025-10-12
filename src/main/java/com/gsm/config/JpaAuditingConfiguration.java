package com.gsm.config;

import com.gsm.security.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;


/**
 * Configures JPA Auditing for the application.
 * The @EnableJpaAuditing annotation turns on auditing, allowing Spring Data JPA
 * to automatically populate fields like @CreatedBy, @LastModifiedBy, etc.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            // Get authentication information from Spring Security.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Check if a user is logged in. If not, return empty.
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }

            // Get the principal and cast it to our custom user details class.
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Return the user's ID, wrapped in an Optional.
            return Optional.of(userDetails.getUserId());
        };
    }
}