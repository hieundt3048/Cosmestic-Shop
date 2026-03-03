package com.cosmeticshop.cosmetic.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateUserRequest;
import com.cosmeticshop.cosmetic.Dto.UserListItemResponse;
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
    public UserListItemResponse createEmployee(CreateUserRequest request) {
        request.setRole(User.Role.EMPLOYEE);
        User createdUser = createUser(request);
        return toUserListItemResponse(createdUser);
    }

    @Override
    public User updateUser(Long id, UpdateUserRequest request){
        User user = userRepository.findById(id)
        .orElseThrow(() -> {
                logger.warn("User with ID '{}' not found", id);
                return new RuntimeException("User không tồn tại với ID: " + id);
            });
        
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());

        
        User savedUser = userRepository.save(user);
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

    @Override
    public List<UserListItemResponse> getCustomers() {
        logger.debug("Fetching users with role CUSTOMER");
        return userRepository.findByRole(User.Role.CUSTOMER).stream()
                .map(this::toUserListItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserListItemResponse> getEmployees() {
        logger.debug("Fetching users with role EMPLOYEE");
        return userRepository.findByRole(User.Role.EMPLOYEE).stream()
                .map(this::toUserListItemResponse)
                .collect(Collectors.toList());
    }

    private UserListItemResponse toUserListItemResponse(User user) {
        int totalOrders = user.getOrders() == null ? 0 : user.getOrders().size();
        return new UserListItemResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                totalOrders);
    }
}
