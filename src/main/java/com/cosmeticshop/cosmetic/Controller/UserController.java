package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Service.IUserManagementService;

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
}
