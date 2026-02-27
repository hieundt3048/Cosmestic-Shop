package com.cosmeticshop.cosmetic.Service;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

/**
 * Tuân thủ SRP - Tách validation logic ra khỏi UserService
 */
@Service
public class UserValidationService implements IUserValidationService {
    
    private final UserRepository userRepository;
    
    public UserValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public void validateUserRegistration(CreateUserRequest request) {
        validateUsernameDuplicate(request.getUsername());
        validateEmailDuplicate(request.getEmail());
        validatePhoneDuplicate(request.getPhone());
        validatePasswordStrength(request.getPassword(), request.getUsername(), request.getEmail());
    }
    
    @Override
    public void validatePasswordStrength(String password, String username, String email) {
        String passwordLower = password.toLowerCase();
        String usernameLower = username.toLowerCase();
        String emailLocalPart = email.split("@")[0].toLowerCase();
        
        if (passwordLower.contains(usernameLower)) {
            throw new RuntimeException("Mật khẩu không được chứa tên đăng nhập");
        }
        
        if (passwordLower.contains(emailLocalPart)) {
            throw new RuntimeException("Mật khẩu không được chứa email");
        }
    }
    
    @Override
    public void validateUsernameDuplicate(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
    }
    
    @Override
    public void validateEmailDuplicate(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
    }
    
    @Override
    public void validatePhoneDuplicate(String phone) {
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
    }
}
