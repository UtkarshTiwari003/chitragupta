package com.attendance;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Attendance Tracking System application.
 * 
 * This Spring Boot application manages student attendance, academic records,
 * fee management, and automated reporting for educational institutions.
 * 
 * Key Features:
 * - JWT-based authentication and role-based access control
 * - Real-time attendance tracking with automated notifications
 * - Comprehensive fee management with payment tracking
 * - Monthly aggregated reports with performance metrics
 * - Gamification through star ratings and leaderboards
 * - Email notifications for important events
 * 
 * Technology Stack:
 * - Spring Boot 3.x with Spring Security and Spring Data JPA
 * - PostgreSQL for persistent storage
 * - Redis for caching and session management
 * - SendGrid for email notifications
 * - OpenAPI 3.0 (Swagger) for API documentation
 * 
 * API Base URL: /api/v1
 * Documentation: /api/v1/swagger-ui.html
 * 
 * @author Attendance System Team
 * @version 1.0.0
 * @since 2026-06-15
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "Attendance Tracking System API",
        version = "1.0.0",
        description = "Comprehensive API for managing student attendance, academics, and fees in educational institutions",
        contact = @Contact(
            name = "Support Team",
            email = "support@attendance-system.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080/api/v1",
            description = "Development Server"
        ),
        @Server(
            url = "https://api.attendance-system.com/api/v1",
            description = "Production Server"
        )
    }
)
public class AttendanceSystemApplication {

    /**
     * Application entry point.
     * Starts the Spring Boot application and initializes all beans.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);
    }
}
