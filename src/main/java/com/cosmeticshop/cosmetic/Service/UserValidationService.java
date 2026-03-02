package com.cosmeticshop.cosmetic.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

/**
 * Tuân thủ SRP - Tách validation logic ra khỏi UserService
 * Tuân thủ OCP - Dùng Chain of Responsibility để mở rộng validation rules
 */
@Service
public class UserValidationService implements IUserValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserValidationService.class);
    
    private final UserRepository userRepository;
    private final ValidationHandler validationChain;
    
    /**
     * Constructor with validation chain
     * @param userRepository User repository for database queries
     * @param validationChain First handler in the validation chain
     */
    public UserValidationService(
        UserRepository userRepository, 
        @Qualifier("validationChain") ValidationHandler validationChain
    ) {
        this.userRepository = userRepository;
        this.validationChain = validationChain;
        logger.info("UserValidationService initialized with validation chain");
    }
    
    /**
     * Validate user registration using Chain of Responsibility
     * New validation rules can be added by creating new handlers
     */
    @Override
    public void validateUserRegistration(CreateUserRequest request) {
        logger.debug("Starting validation chain for user: {}", request.getUsername());
        validationChain.validate(request);
        logger.debug("Validation chain completed successfully for user: {}", request.getUsername());
    }
    
    /**
     * Legacy method - kept for backward compatibility
     * Delegates to validation chain through validateUserRegistration
     */
    @Override
    public void validatePasswordStrength(String password, String username, String email) {
        CreateUserRequest request = new CreateUserRequest();
        request.setPassword(password);
        request.setUsername(username);
        request.setEmail(email);
        
        // Only run password strength validation (last in chain)
        PasswordStrengthValidationHandler handler = new PasswordStrengthValidationHandler();
        handler.validate(request);
    }
    
    /**
     * Legacy method - kept for backward compatibility
     */
    @Override
    public void validateUsernameDuplicate(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
    }
    
    /**
     * Legacy method - kept for backward compatibility
     */
    @Override
    public void validateEmailDuplicate(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
    }
    
    /**
     * Legacy method - kept for backward compatibility
     */
    @Override
    public void validatePhoneDuplicate(String phone) {
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
    }
}
