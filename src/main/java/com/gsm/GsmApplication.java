package com.gsm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the Spring Boot application.
 * The @SpringBootApplication annotation enables auto-configuration, component scanning,
 * and other core Spring Boot features.
 */
@SpringBootApplication
public class GsmApplication {

    /**
     * The main method which uses Spring Boot's SpringApplication.run()
     * to launch the application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(GsmApplication.class, args);
    }

}
