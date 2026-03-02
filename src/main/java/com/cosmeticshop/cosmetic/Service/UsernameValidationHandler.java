package com.cosmeticshop.cosmetic.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

/**
 * Xử lý xác thực username duy nhất
 */
@Component
public class UsernameValidationHandler extends AbstractValidationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(UsernameValidationHandler.class);
    private final UserRepository userRepository;
    
    public UsernameValidationHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public void validate(CreateUserRequest request) {
        logger.debug("Validating username: {}", request.getUsername());
        
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Username validation failed: '{}' already exists", request.getUsername());
            throw new RuntimeException("Username đã tồn tại");
        }
        
        logger.debug("Username validation passed: {}", request.getUsername());
        validateNext(request);
    }
}
