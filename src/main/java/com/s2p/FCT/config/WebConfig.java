package com.s2p.FCT.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                          // Apply to all endpoints
                .allowedOrigins(
                    "http://localhost:3000", 
                    "https://fictilecore.com", 
                    "https://www.fictilecore.com"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")  // ← PATCH added
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);   // Cache preflight for 1 hour (optional but recommended)
    }
}