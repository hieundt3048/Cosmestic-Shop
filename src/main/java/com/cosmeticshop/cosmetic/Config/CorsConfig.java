package com.cosmeticshop.cosmetic.Config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS Configuration - Cho phép frontend gọi API từ domain khác
 * 
 * Production: Chỉ cho phép domain cụ thể của frontend
 * Development: Có thể mở rộng hơn
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép các origin (domain) nào gọi API
        // Development: cho phép localhost
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",      // React dev server
            "http://localhost:5173",      // Vite dev server
            "http://localhost:5174",
            "http://localhost:8080"       // Same origin
            // Production: thêm domain thật như "https://yourdomain.com"
        ));
        
        // Cho phép các HTTP methods nào
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Cho phép các headers nào
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",              // Quan trọng cho JWT
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Cho phép gửi credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Thời gian cache preflight request (giây)
        configuration.setMaxAge(3600L);
        
        // Áp dụng CORS config cho tất cả endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
