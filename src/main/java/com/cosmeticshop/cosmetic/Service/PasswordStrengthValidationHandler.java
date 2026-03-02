package com.cosmeticshop.cosmetic.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;

/**
 * Xử lý xác thực password strength
 */
@Component
public class PasswordStrengthValidationHandler extends AbstractValidationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordStrengthValidationHandler.class);
    
    @Override
    public void validate(CreateUserRequest request) {
        logger.debug("Validating password strength for user: {}", request.getUsername());
        
        String password = request.getPassword();
        String username = request.getUsername();
        String email = request.getEmail();
        
        if (password == null || username == null) {
            logger.error("Password or username is null");
            throw new RuntimeException("Mật khẩu và username không được để trống");
        }
        
        String passwordLower = password.toLowerCase();
        String usernameLower = username.toLowerCase();
        
        if (passwordLower.contains(usernameLower)) {
            logger.warn("Password strength validation failed: password contains username");
            throw new RuntimeException("Mật khẩu không được chứa tên đăng nhập");
        }
        
        if (email != null && !email.trim().isEmpty()) {
            String emailLower = email.toLowerCase();
            String emailUsername = emailLower.split("@")[0];
            
            if (passwordLower.contains(emailUsername)) {
                logger.warn("Password strength validation failed: password contains email");
                throw new RuntimeException("Mật khẩu không được chứa địa chỉ email");
            }
        }
        
        logger.debug("Password strength validation passed for user: {}", request.getUsername());
        validateNext(request);
    }
}
