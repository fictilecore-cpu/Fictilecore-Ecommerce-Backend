package com.s2p.FCT.controllers;

import com.s2p.FCT.entity.Customers;
import com.s2p.FCT.model.AuthResponse;
import com.s2p.FCT.model.LoginModel;
import com.s2p.FCT.security.JwtUtil;
import com.s2p.FCT.services.CustomerService;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class LogInController {

    private static final Logger logger = LoggerFactory.getLogger(LogInController.class);

    private final CustomerService customerService;
    private final JwtUtil jwtUtil;

    @Autowired
    public LogInController(CustomerService customerService, JwtUtil jwtUtil) {
        this.customerService = customerService;
        this.jwtUtil = jwtUtil;
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> logIn(@RequestBody LoginModel loginModel, HttpServletResponse response) {
        try {
            AuthResponse customer = customerService.logIn(
                    loginModel.getEmail(),
                    loginModel.getPassword()
            );

            if (customer == null) {
                logger.warn("Invalid login attempt for email: {}", loginModel.getEmail());
                return ResponseEntity.status(401).body("Invalid credentials");
            }

            String token = jwtUtil.generateToken(customer.getEmail(), customer.getRole());
            logger.debug("JWT token generated for email: {}", customer.getEmail());

            ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(86400)  // 1 day
                    .sameSite("Lax")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            logger.info("User logged in: {}", customer.getEmail());

            return ResponseEntity.ok(customer);

        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        logger.info("User logged out");

        return ResponseEntity.ok("Logged out successfully");
    }

    // ================= PROFILE =================
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Step 1: Validate Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header");
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }

            // Step 2: Extract token
            String token = authHeader.substring(7);
            logger.debug("Extracted JWT token: {}", token);

            // Step 3: Extract email from JWT
            String email;
            try {
                email = jwtUtil.extractEmail(token);
                logger.debug("Email extracted from JWT: {}", email);
            } catch (Exception ex) {
                logger.error("Failed to extract email from JWT", ex);
                return ResponseEntity.status(401).body("Invalid JWT token: " + ex.getMessage());
            }

            // Step 4: Fetch customer from DB
            Customers customer = customerService.getCustomerByEmail(email);
            if (customer == null) {
                logger.warn("No customer found with email: {}", email);
                return ResponseEntity.status(404).body("Customer not found");
            }

            // Step 5: Convert to AuthResponse
            AuthResponse model = customerService.toAuthResponse(customer, token);
            logger.debug("AuthResponse created for customer: {}", email);

            // Step 6: Log Spring Security context
            var auth = SecurityContextHolder.getContext().getAuthentication();
            logger.debug("SecurityContext Authentication: {}", auth);

            return ResponseEntity.ok(model);

        } catch (Exception e) {
            logger.error("Unexpected error in /profile", e);
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }
}