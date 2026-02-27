package com.cosmeticshop.cosmetic.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

/**
 * Single Responsibility: CHỈ xác thực user
 */
@Service
public class AuthenticationService implements IAuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public User authenticate(String username, String password) {
        logger.debug("Attempting to authenticate user: {}", username);
        
        // Tìm user theo username
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.warn("Authentication failed: username '{}' not found", username);
                return new RuntimeException("Tên đăng nhập không đúng");
            });
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Authentication failed: invalid password for user '{}'", username);
            throw new RuntimeException("Mật khẩu không đúng");
        }
        
        logger.info("User '{}' authenticated successfully", username);
        return user;
    }
}
