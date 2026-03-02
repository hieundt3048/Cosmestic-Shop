package com.cosmeticshop.cosmetic.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cosmeticshop.cosmetic.Service.EmailValidationHandler;
import com.cosmeticshop.cosmetic.Service.PasswordStrengthValidationHandler;
import com.cosmeticshop.cosmetic.Service.PhoneValidationHandler;
import com.cosmeticshop.cosmetic.Service.UsernameValidationHandler;
import com.cosmeticshop.cosmetic.Service.ValidationHandler;

/**
 * Configuration class for building the validation chain
 * Implements Chain of Responsibility pattern (OCP - Open/Closed Principle)
 * New validators can be added without modifying existing code
 */
@Configuration
public class ValidationConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationConfig.class);
    
    /**
     * Creates the validation chain bean
     * Chain order: Username -> Email -> Phone -> Password Strength
     * 
     * @param usernameHandler Handler for username validation
     * @param emailHandler Handler for email validation
     * @param phoneHandler Handler for phone validation
     * @param passwordHandler Handler for password strength validation
     * @return The first handler in the validation chain
     */
    @Bean(name = "validationChain")
    public ValidationHandler validationChain(
            UsernameValidationHandler usernameHandler,
            EmailValidationHandler emailHandler,
            PhoneValidationHandler phoneHandler,
            PasswordStrengthValidationHandler passwordHandler) {
        
        logger.info("Building validation chain");
        
        // Build the chain: username -> email -> phone -> password
        usernameHandler.setNext(emailHandler);
        emailHandler.setNext(phoneHandler);
        phoneHandler.setNext(passwordHandler);
        
        logger.info("Validation chain built successfully with 4 handlers");
        
        // Return the first handler in the chain
        return usernameHandler;
    }
}
