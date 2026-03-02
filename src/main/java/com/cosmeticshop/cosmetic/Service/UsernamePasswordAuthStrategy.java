package com.cosmeticshop.cosmetic.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.LoginRequest;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

/**
 * Username/Password authentication strategy
 */
@Component
public class UsernamePasswordAuthStrategy implements AuthenticationStrategy {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UsernamePasswordAuthStrategy(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public User authenticate(Object credentials) {
        if (!(credentials instanceof LoginRequest)) {
            throw new IllegalArgumentException("Invalid credentials type for username/password authentication");
        }
        
        LoginRequest request = (LoginRequest) credentials;
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }
        
        return user;
    }
    
    @Override
    public boolean supports(String type) {
        return "username_password".equalsIgnoreCase(type);
    }
}
