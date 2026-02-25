package com.cosmeticshop.cosmetic.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF cho REST API
            .cors(cors -> cors.configure(http)) // Kích hoạt CORS
            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập công khai
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/brands/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                
                // Các endpoint khác yêu cầu xác thực
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}); // Sử dụng HTTP Basic Authentication tạm thời
            
        return http.build();
    }
}
