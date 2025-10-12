package com.gsm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

/**
 * A general-purpose configuration class for defining application-wide Spring Beans.
 * These beans are managed by the Spring container and can be injected where needed.
 */
@Configuration
public class AppConfig {

    /**
     * Creates a RestTemplate bean for making synchronous, client-side HTTP requests.
     * @return A singleton RestTemplate instance.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates a PasswordEncoder bean using the BCrypt hashing algorithm.
     * This is the standard for securely hashing user passwords before storage.
     * @return A singleton BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}