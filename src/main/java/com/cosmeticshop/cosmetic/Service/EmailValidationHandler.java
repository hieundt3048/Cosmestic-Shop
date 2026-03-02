package com.cosmeticshop.cosmetic.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

/**
 * Bộ xử lý để xác thực email duy nhất
 */
@Component
public class EmailValidationHandler extends AbstractValidationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailValidationHandler.class);
    private final UserRepository userRepository;
    
    public EmailValidationHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public void validate(CreateUserRequest request) {
        logger.debug("Validating email: {}", request.getEmail());
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Email validation failed: '{}' already exists", request.getEmail());
            throw new RuntimeException("Email đã tồn tại");
        }
        
        logger.debug("Email validation passed: {}", request.getEmail());
        validateNext(request);
    }
}
