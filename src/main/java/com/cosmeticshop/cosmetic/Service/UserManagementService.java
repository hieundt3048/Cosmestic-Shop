package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

import jakarta.transaction.Transactional;

/**
 * Single Responsibility: CHỈ quản lý CRUD operations cho User
 */
@Service
public class UserManagementService implements IUserManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IUserValidationService validationService;
    
    public UserManagementService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        IUserValidationService validationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
    }
    
    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {
        logger.info("Creating new user: {}", request.getUsername());
        
        validationService.validateUserRegistration(request);
        
        // Chỉ lo việc tạo user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.CUSTOMER);
        
        User savedUser = userRepository.save(user);
        logger.info("User '{}' created successfully with ID: {}", savedUser.getUsername(), savedUser.getId());
        
        return savedUser;
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            logger.warn("User with ID {} not found", id);
            throw new RuntimeException("User không tồn tại với ID: " + id);
        }
        
        userRepository.deleteById(id);
        logger.info("User with ID {} deleted successfully", id);
    }
    
    @Override
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }
    
    @Override
    public User getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User with ID {} not found", id);
                return new RuntimeException("User không tồn tại với ID: " + id);
            });
    }
    
    @Override
    public User getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.warn("User with username '{}' not found", username);
                return new RuntimeException("User không tồn tại với username: " + username);
            });
    }
}
