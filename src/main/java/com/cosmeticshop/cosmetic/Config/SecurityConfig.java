package com.cosmeticshop.cosmetic.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * SecurityConfig - Cấu hình bảo mật cho ứng dụng
 * 
 * JWT Security Flow:
 * 1. Client gửi request với JWT token trong header
 * 2. JwtAuthenticationFilter intercept và validate token
 * 3. Nếu valid → set authentication vào SecurityContext
 * 4. Request đến controller với user đã được authenticate
 * 
 * EnableMethodSecurity: Cho phép sử dụng @PreAuthorize, @Secured, etc.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter, 
            UserDetailsService userDetailsService,
            CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * AuthenticationProvider - Provider xác thực user
     * Sử dụng UserDetailsService để load user và PasswordEncoder để verify password
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager - Manager chính để xác thực
     * Sử dụng trong AuthController để xác thực login
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * SecurityFilterChain - Cấu hình security cho HTTP requests
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì sử dụng JWT (stateless)
            .csrf(csrf -> csrf.disable())
            
            // Kích hoạt CORS với configuration từ CorsConfig
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Cấu hình authorization cho các endpoints
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - không cần authentication
                .requestMatchers("/api/auth/**").permitAll()           // Login, Register
                .requestMatchers("/api/brands/**").permitAll()         // Xem brands
                .requestMatchers("/api/categories/**").permitAll()     // Xem categories
                .requestMatchers("/api/products/**").permitAll()       // Xem products
                .requestMatchers("/api/seed/**").permitAll()          // Seed data
                
                // Protected endpoints - cần authentication
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                
                .anyRequest().authenticated()  // Các endpoint khác yêu cầu xác thực
            )
            
            // Session management - STATELESS (không lưu session)
            // Mỗi request phải có JWT token, không dựa vào session/cookie
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Set authentication provider
            .authenticationProvider(authenticationProvider())
            
            // Thêm JWT filter TRƯỚC UsernamePasswordAuthenticationFilter
            // JWT filter sẽ chạy đầu tiên để validate token
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
