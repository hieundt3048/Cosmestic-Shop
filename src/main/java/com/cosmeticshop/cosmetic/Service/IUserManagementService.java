package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Entity.User;

/**
 * Responsibility: CHỈ quản lý CRUD operations cho User
 */
public interface IUserManagementService {
    
    /**
     * Tạo user mới
     * Validation được xử lý bởi ValidationService
     */
    User createUser(CreateUserRequest request);
    
    /**
     * Xóa user theo ID
     */
    void deleteUser(Long id);
    
    /**
     * Lấy danh sách tất cả users
     */
    List<User> getAllUsers();
    
    /**
     * Lấy user theo ID
     */
    User getUserById(Long id);
    
    /**
     * Lấy user theo username
     */
    User getUserByUsername(String username);
}
