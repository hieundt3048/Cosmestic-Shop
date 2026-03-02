package com.cosmeticshop.cosmetic.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

/**
 * Xử lý xác thực phone number uniqueness
 */
@Component
public class PhoneValidationHandler extends AbstractValidationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(PhoneValidationHandler.class);
    private final UserRepository userRepository;
    
    public PhoneValidationHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public void validate(CreateUserRequest request) {

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            logger.debug("Phone validation skipped: phone is empty");
            validateNext(request);
            return;
        }
        
        logger.debug("Validating phone: {}", request.getPhone());
        
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            logger.warn("Phone validation failed: '{}' already exists", request.getPhone());
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        
        logger.debug("Phone validation passed: {}", request.getPhone());
        validateNext(request);
    }
}
