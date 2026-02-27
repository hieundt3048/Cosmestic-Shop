package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;

/**
 * Responsibility: CHỈ validate dữ liệu user
 */
public interface IUserValidationService {
    
    /**
     * Validate toàn bộ thông tin user khi đăng ký
     * Bao gồm: username, email, phone duplicate check và password strength
     */
    void validateUserRegistration(CreateUserRequest request);
    
    /**
     * Validate độ mạnh của password
     * Kiểm tra password không chứa username hoặc email
     */
    void validatePasswordStrength(String password, String username, String email);
    
    /**
     * Validate username không bị trùng
     */
    void validateUsernameDuplicate(String username);
    
    /**
     * Validate email không bị trùng
     */
    void validateEmailDuplicate(String email);
    
    /**
     * Validate phone không bị trùng
     */
    void validatePhoneDuplicate(String phone);
}
