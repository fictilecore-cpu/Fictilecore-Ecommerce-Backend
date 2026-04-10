package com.s2p.FCT.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        logRequestDetails(request);  // <-- Log every request

        try {
            String token = extractToken(request);
            if (token == null) {
                logger.debug("[JWT FILTER] No token found in request");
            } else {
                logger.debug("[JWT FILTER] Token found: {}", token);

                Claims claims = jwtUtil.validateToken(token);
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                logger.debug("[JWT FILTER] Token valid. User: {}, Role: {}", userId, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("[JWT FILTER] SecurityContext updated with authentication");
            }
        } catch (Exception e) {
            logger.error("[JWT FILTER] Token validation failed: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Tries to extract JWT token from Authorization header first,
     * then from cookies if header is missing.
     */
    private String extractToken(HttpServletRequest request) {

        // Check Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Check cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Logs HTTP request details: method, URI, query params, and headers.
     */
    private void logRequestDetails(HttpServletRequest request) {
        logger.info("[REQUEST] {} {}", request.getMethod(), request.getRequestURI());

        if (request.getQueryString() != null) {
            logger.info("[QUERY] {}", request.getQueryString());
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                logger.info("[HEADER] {}: {}", header, request.getHeader(header));
            }
        }
    }
}