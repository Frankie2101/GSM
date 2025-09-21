package com.gsm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple REST controller to provide a health check endpoint for monitoring purposes.
 */
@RestController
public class HealthCheckController {

    /**
     * Responds to GET requests on "/health".
     * This is commonly used by load balancers or container orchestrators (like Kubernetes)
     * to verify that the application instance is running and responsive.
     * @return An HTTP 200 OK response with the string "OK" as the body.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}