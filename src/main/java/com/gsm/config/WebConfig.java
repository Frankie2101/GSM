package com.gsm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for customizing Spring Web MVC.
 * This class implements WebMvcConfigurer to provide callback methods for customization.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     * This is necessary to allow the Zalo Mini App, running on Zalo's domains,
     * to make API calls to this server.
     * @param registry The CORS registry to which mappings are added.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") //
                .allowedOrigins( // List the specific Zalo domains that are allowed to make requests
                        "https://zapps.zalo.me",
                        "https://h5.zalo.me",
                        "https://cdnd.zalo.me",
                        "https://h5.zdn.vn"  // Allows resources from Zalo's CDN
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true); // Allow cookies to be sent with cross-origin requests
    }
}
