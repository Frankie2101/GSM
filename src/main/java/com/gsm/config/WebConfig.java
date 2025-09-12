package com.gsm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Trong file WebConfig.java
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Chỉ áp dụng cho các đường dẫn API
                .allowedOrigins(
                        "https://zapps.zalo.me",
                        "https://h5.zalo.me",
                        "https://cdnd.zalo.me",
                        "https://h5.zdn.vn" // <-- THÊM DÒNG QUAN TRỌNG NÀY
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
