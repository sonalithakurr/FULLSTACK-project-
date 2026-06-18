package com.cineverse.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * CineVerse Auth Service Entry Point
 *
 * Responsibilities:
 *  - User registration and login
 *  - JWT token issuance and validation
 *  - Token refresh and blacklisting (Redis)
 *  - Publishing user events to RabbitMQ
 */
@SpringBootApplication
@EnableCaching   // Enables Spring's cache abstraction (backed by Redis)
@EnableAsync     // Allows async event publishing without blocking request threads
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
