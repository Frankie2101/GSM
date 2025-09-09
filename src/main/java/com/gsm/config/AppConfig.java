// File: src/main/java/com/gsm/config/AppConfig.java
package com.gsm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // THÊM IMPORT
import org.springframework.security.crypto.password.PasswordEncoder; // THÊM IMPORT
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // THÊM BEAN NÀY VÀO ĐÂY
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}