package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.LoginRequest;
import com.cosmeticshop.cosmetic.Entity.User;

/**
 * Single Responsibility: CHỈ xác thực user
 * Open/Closed Principle: Mở rộng authentication methods bằng cách thêm Strategy mới
 */
@Service
public class AuthenticationService implements IAuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final List<AuthenticationStrategy> strategies;
    
    public AuthenticationService(List<AuthenticationStrategy> strategies) {
        this.strategies = strategies;
        logger.info("AuthenticationService initialized with {} strategies", strategies.size());
    }
    
    @Override
    public User authenticate(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return authenticate("username_password", request);
    }
    
    @Override
    public User authenticate(String type, Object credentials) {
        logger.debug("Attempting authentication with type: {}", type);
        
        AuthenticationStrategy strategy = strategies.stream()
            .filter(s -> s.supports(type))
            .findFirst()
            .orElseThrow(() -> {
                logger.error("No authentication strategy found for type: {}", type);
                return new UnsupportedOperationException("Phương thức xác thực '" + type + "' không được hỗ trợ");
            });
        
        User user = strategy.authenticate(credentials);
        logger.info("User '{}' authenticated successfully using '{}' strategy", user.getUsername(), type);
        return user;
    }
}
