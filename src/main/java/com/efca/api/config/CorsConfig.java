package com.efca.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

<<<<<<< HEAD
=======

>>>>>>> 1cbb7d1d45233d4757ce4dcf49b93b1ca8a135d4
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(@Value("${efca.cors.allowed-origins}") String allowedOriginsProp) {
        this.allowedOrigins = allowedOriginsProp.split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST")
            .allowedHeaders("Content-Type")
            .maxAge(3600);
    }
}
