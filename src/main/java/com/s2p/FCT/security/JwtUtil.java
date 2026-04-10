package com.s2p.FCT.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Use a strong secret, at least 256-bit (32+ characters)
    private final String SECRET = "your-very-secure-256-bit-secret-key-change-this";
    private final long EXPIRATION = 1000 * 60 * 60 * 24; // 1 day

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /**
     * Generate JWT token with email as subject and role as claim
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)               // store email
                .claim("role", role)             // store role
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate token and return claims
     */
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return (String) validateToken(token).get("role");
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        return validateToken(token).getExpiration().before(new Date());
    }
}