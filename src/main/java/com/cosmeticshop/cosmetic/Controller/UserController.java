package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateUserRequest;
import com.cosmeticshop.cosmetic.Dto.UserListItemResponse;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Service.IUserManagementService;

import jakarta.validation.Valid;

/**
 * Responsibility: CHỈ handle các request liên quan đến user management
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final IUserManagementService userManagementService;
    
    public UserController(IUserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }
    
    /**
     * Lấy danh sách tất cả users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Lấy danh sách khách hàng
     * GET /api/users/customers
     */
    @GetMapping("/customers")
    public ResponseEntity<List<UserListItemResponse>> getCustomers() {
        logger.info("Fetching users with role CUSTOMER");
        return ResponseEntity.ok(userManagementService.getCustomers());
    }

    /**
     * Lấy danh sách nhân viên
     * GET /api/users/employees
     */
    @GetMapping("/employees")
    public ResponseEntity<List<UserListItemResponse>> getEmployees() {
        logger.info("Fetching users with role EMPLOYEE");
        return ResponseEntity.ok(userManagementService.getEmployees());
    }

    /**
     * Tạo nhân viên mới
     * POST /api/users/employees
     */
    @PostMapping("/employees")
    public ResponseEntity<UserListItemResponse> createEmployee(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Creating employee user: {}", request.getUsername());
        return ResponseEntity.status(201).body(userManagementService.createEmployee(request));
    }
    
    /**
     * Lấy user theo ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("Fetching user with ID: {}", id);
        User user = userManagementService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Xóa user theo ID
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        userManagementService.deleteUser(id);
        return ResponseEntity.ok("Xóa user thành công với ID: " + id);
    }

    /**
     * Chỉnh sửa thông tin User
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request){
        logger.info("Update user with ID: {}", id);
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }
}
